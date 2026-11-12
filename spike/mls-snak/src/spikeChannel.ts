/**
 * Stand-in for the Kreds server: a BroadcastChannel delivers messages between tabs
 * of the same origin. Like the real server, it only ever sees public keys and ciphertext.
 */
export type SpikeMessage =
    | { type: 'key-package-request'; from: string }
    | { type: 'key-package'; from: string; bytes: Uint8Array }
    | { type: 'welcome'; from: string; to: string; bytes: Uint8Array }
    | { type: 'snak'; from: string; ciphertext: Uint8Array }

const channel = new BroadcastChannel('kreds-spike')

export function send(message: SpikeMessage): void {
    channel.postMessage(message)
}

export function onMessage(handler: (message: SpikeMessage) => void): () => void {
    const listener = (event: MessageEvent<SpikeMessage>) => handler(event.data)
    channel.addEventListener('message', listener)
    return () => channel.removeEventListener('message', listener)
}

/**
 * Size and last bytes as hex, so we can see what "the server" sees.
 * (The first bytes are the readable MLS header: version, group id and epoch, the same for every message.)
 */
export function toHexPreview(bytes: Uint8Array, length = 16): string {
    const tail = Array.from(bytes.slice(-length), (b) => b.toString(16).padStart(2, '0')).join(' ')
    return `${bytes.length} bytes … ${tail}`
}