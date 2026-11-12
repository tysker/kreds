/**
 * What the spike UI needs from an MLS library. Both candidates implement it,
 * so App.tsx stays the same and the comparison is fair.
 */
export interface SnakDevice {
    /** A public "invitation key" that lets someone else add this device to a group. */
    createKeyPackage(): Promise<Uint8Array>

    /**
     * Creates a new snak and adds the owner of the key package to it.
     * @returns the Welcome message the other person needs to join
     */
    startSnakWith(otherKeyPackage: Uint8Array): Promise<Uint8Array>

    /** Joins the snak someone else created for us. */
    joinSnak(welcome: Uint8Array): Promise<void>

    encrypt(text: string): Promise<Uint8Array>

    decrypt(ciphertext: Uint8Array): Promise<string>
}

/** The MLS libraries we compare. Chosen per tab with ?lib=… (both tabs must use the same one). */
export type MlsLibrary = 'core-crypto' | 'ts-mls'