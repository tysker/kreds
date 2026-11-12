import {
    type CiphersuiteImpl,
    type ClientState,
    createApplicationMessage,
    createCommit,
    createGroup,
    type Credential,
    decodeMlsMessage,
    defaultCapabilities,
    defaultLifetime,
    emptyPskIndex,
    encodeMlsMessage,
    generateKeyPackage,
    getCiphersuiteFromName,
    getCiphersuiteImpl,
    joinGroup,
    type KeyPackage,
    type PrivateKeyPackage,
    processPrivateMessage,
    zeroOutUint8Array,
} from 'ts-mls'
import type {SnakDevice} from './snakDevice'

/** The same suite core-crypto uses by default (X25519, AES-128-GCM, Ed25519), so the comparison is fair. */
const CIPHER_SUITE_NAME = 'MLS_128_DHKEMX25519_AES128GCM_SHA256_Ed25519'

/**
 * A spike device built on ts-mls (pure TypeScript, no WebAssembly).
 *
 * ts-mls is "functional": every operation returns a NEW group state, and we must keep it.
 * Nothing is stored for us – the state below lives only in this object (in memory).
 */
export class TsMlsSnakDevice implements SnakDevice {
    private readonly cipherSuite: CiphersuiteImpl
    private readonly keyPackage: KeyPackage
    private readonly privateKeyPackage: PrivateKeyPackage
    private group: ClientState | null = null

    private constructor(cipherSuite: CiphersuiteImpl, keyPackage: KeyPackage, privateKeyPackage: PrivateKeyPackage) {
        this.cipherSuite = cipherSuite
        this.keyPackage = keyPackage
        this.privateKeyPackage = privateKeyPackage
    }

    static async create(): Promise<TsMlsSnakDevice> {
        const cipherSuite = await getCiphersuiteImpl(getCiphersuiteFromName(CIPHER_SUITE_NAME))
        const credential: Credential = {
            credentialType: 'basic',
            identity: new TextEncoder().encode(crypto.randomUUID()),
        }
        // Real clients publish many one-time key packages; the spike makes one per device.
        const {publicPackage, privatePackage} = await generateKeyPackage(
            credential,
            defaultCapabilities(),
            defaultLifetime,
            [],
            cipherSuite,
        )
        return new TsMlsSnakDevice(cipherSuite, publicPackage, privatePackage)
    }

    async createKeyPackage(): Promise<Uint8Array> {
        return encodeMlsMessage({keyPackage: this.keyPackage, wireformat: 'mls_key_package', version: 'mls10'})
    }

    async startSnakWith(otherKeyPackage: Uint8Array): Promise<Uint8Array> {
        const decoded = decodeMlsMessage(otherKeyPackage, 0)?.[0]
        if (decoded?.wireformat !== 'mls_key_package') throw new Error('Expected a key package')

        const groupId = crypto.getRandomValues(new Uint8Array(16))
        const group = await createGroup(groupId, this.keyPackage, this.privateKeyPackage, [], this.cipherSuite)
        const commit = await createCommit(
            {state: group, cipherSuite: this.cipherSuite},
            {
                extraProposals: [{proposalType: 'add', add: {keyPackage: decoded.keyPackage}}],
                // Put the group's public tree into the Welcome, so the new member needs nothing else.
                ratchetTreeExtension: true,
            },
        )
        // Wipe key material we no longer need (forward secrecy). core-crypto does this for us.
        commit.consumed.forEach(zeroOutUint8Array)
        if (!commit.welcome) throw new Error('ts-mls produced no Welcome')

        this.group = commit.newState
        return encodeMlsMessage({welcome: commit.welcome, wireformat: 'mls_welcome', version: 'mls10'})
    }

    async joinSnak(welcome: Uint8Array): Promise<void> {
        const decoded = decodeMlsMessage(welcome, 0)?.[0]
        if (decoded?.wireformat !== 'mls_welcome') throw new Error('Expected a Welcome')
        this.group = await joinGroup(
            decoded.welcome,
            this.keyPackage,
            this.privateKeyPackage,
            emptyPskIndex,
            this.cipherSuite,
        )
    }

    async encrypt(text: string): Promise<Uint8Array> {
        const result = await createApplicationMessage(
            this.requireGroup(),
            new TextEncoder().encode(text),
            this.cipherSuite,
        )
        this.group = result.newState
        result.consumed.forEach(zeroOutUint8Array)
        return encodeMlsMessage({
            privateMessage: result.privateMessage,
            wireformat: 'mls_private_message',
            version: 'mls10',
        })
    }

    async decrypt(ciphertext: Uint8Array): Promise<string> {
        const decoded = decodeMlsMessage(ciphertext, 0)?.[0]
        if (decoded?.wireformat !== 'mls_private_message') throw new Error('Expected a private message')

        const result = await processPrivateMessage(
            this.requireGroup(),
            decoded.privateMessage,
            emptyPskIndex,
            this.cipherSuite,
        )
        this.group = result.newState
        result.consumed.forEach(zeroOutUint8Array)
        if (result.kind !== 'applicationMessage') throw new Error(`Expected a chat message, got ${result.kind}`)
        return new TextDecoder().decode(result.message)
    }

    private requireGroup(): ClientState {
        if (!this.group) throw new Error('No snak yet')
        return this.group
    }
}