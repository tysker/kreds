import {
    cipherSuiteDefault,
    ClientId,
    ConversationId,
    CoreCrypto,
    Credential,
    type CredentialRef,
    Database,
    DecryptedMessage,
    DeviceId,
    initWasmModule,
    KeyPackage,
    type MlsTransport,
    Uuid,
    version,
    Welcome,
} from '@wireapp/core-crypto'

let loading: Promise<string> | null = null

/** Loads the core-crypto WebAssembly module once and returns the library version. */
export function loadCoreCrypto(): Promise<string> {
    loading ??= initWasmModule('/core-crypto/index_bg.wasm').then(() => version())
    return loading
}

/** core-crypto expects a domain in every client id (a Wire concept); any fixed value works for the spike. */
const SPIKE_DOMAIN = 'spike.kreds.local'

/**
 * One person's device in the spike: its own key store, its own MLS identity
 * and at most one 1:1 conversation (a snak).
 */
export class SnakDevice {
    /** The Welcome produced by the last commit; read once by {@link startSnakWith}. */
    private pendingWelcome: Uint8Array | null = null
    private conversationId: ConversationId | null = null
    private readonly coreCrypto: CoreCrypto
    private readonly credentialRef: CredentialRef

    private constructor(coreCrypto: CoreCrypto, credentialRef: CredentialRef) {
        this.coreCrypto = coreCrypto
        this.credentialRef = credentialRef
    }

    /**
     * Creates a fresh device with an in-memory key store.
     * In-memory means: reload the tab and the keys are gone (fine for a spike, not for Kreds).
     */
    static async create(): Promise<SnakDevice> {
        await loadCoreCrypto()
        const coreCrypto = CoreCrypto.new(Database.inMemory())
        const clientId = new ClientId(new Uuid(crypto.randomUUID()), new DeviceId(1n), SPIKE_DOMAIN)

        // core-crypto hands every commit to this transport and waits for the "server" to accept it.
        // There is no server in the spike, so we accept everything and keep the Welcome for later.
        let device: SnakDevice | null = null
        const transport: MlsTransport = {
            sendCommitBundle: async (bundle) => {
                if (device && bundle.welcome) device.pendingWelcome = bundle.welcome.serialize()
            },
            prepareForTransport: async () => new Uint8Array(),
        }

        const credentialRef = await coreCrypto.transaction(async (ctx) => {
            await ctx.mlsInit(clientId, transport)
            return ctx.addCredential(Credential.basic(cipherSuiteDefault(), clientId))
        })
        device = new SnakDevice(coreCrypto, credentialRef)
        return device
    }

    /** A one-time public "invitation key" that lets someone else add this device to a group. */
    async createKeyPackage(): Promise<Uint8Array> {
        const keyPackage = await this.coreCrypto.transaction((ctx) => ctx.generateKeyPackage(this.credentialRef))
        return keyPackage.serialize()
    }

    /**
     * Creates a new snak and adds the owner of the key package to it.
     * @returns the Welcome message the other person needs to join
     */
    async startSnakWith(otherKeyPackage: Uint8Array): Promise<Uint8Array> {
        const conversationId = new ConversationId(crypto.getRandomValues(new Uint8Array(16)))
        await this.coreCrypto.transaction(async (ctx) => {
            await ctx.createConversation(conversationId, this.credentialRef)
            await ctx.addClientsToConversation(conversationId, [new KeyPackage(otherKeyPackage)])
        })
        const welcome = this.pendingWelcome
        this.pendingWelcome = null
        if (!welcome) throw new Error('core-crypto produced no Welcome')
        this.conversationId = conversationId
        return welcome
    }

    /** Joins the snak someone else created for us. */
    async joinSnak(welcome: Uint8Array): Promise<void> {
        this.conversationId = await this.coreCrypto.transaction((ctx) =>
            ctx.processWelcomeMessage(new Welcome(welcome)),
        )
    }

    async encrypt(text: string): Promise<Uint8Array> {
        const conversationId = this.requireSnak()
        const plaintext = new TextEncoder().encode(text)
        return this.coreCrypto.transaction((ctx) => ctx.encryptMessage(conversationId, plaintext))
    }

    async decrypt(ciphertext: Uint8Array): Promise<string> {
        const conversationId = this.requireSnak()
        const message = await this.coreCrypto.transaction((ctx) => ctx.decryptMessage(conversationId, ciphertext))
        if (!DecryptedMessage.ApplicationMessage.instanceOf(message)) {
            throw new Error(`Expected a chat message, got ${message.tag}`)
        }
        return new TextDecoder().decode(message.inner.plaintext)
    }

    private requireSnak(): ConversationId {
        if (!this.conversationId) throw new Error('No snak yet')
        return this.conversationId
    }
}