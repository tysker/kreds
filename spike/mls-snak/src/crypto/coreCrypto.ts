import {initWasmModule, version} from '@wireapp/core-crypto'

let loading: Promise<string> | null = null

/**
 * Loads the core-crypto WebAssembly module once and returns the library version.
 */
export function loadCoreCrypto(): Promise<string> {
    loading ??= initWasmModule('/core-crypto/index_bg.wasm').then(() => version())
    return loading
}