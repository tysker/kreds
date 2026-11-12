import {type FormEvent, useEffect, useRef, useState} from 'react'
import type {MlsLibrary, SnakDevice} from './crypto/snakDevice'
import {onMessage, send, toHexPreview} from './spikeChannel'
import {TsMlsSnakDevice} from "./crypto/tsMls.ts";
import {CoreCryptoSnakDevice} from "./crypto/coreCrypto.ts";

/** Each tab plays one person: ?user=anna or ?user=bjorn */
const params = new URLSearchParams(window.location.search)
const me = params.get('user') ?? 'anna'

/** ?lib=ts-mls picks the comparison library; core-crypto is the default. */
const library: MlsLibrary = params.get('lib') === 'ts-mls' ? 'ts-mls' : 'core-crypto'
const createDevice = (): Promise<SnakDevice> =>
    library === 'ts-mls' ? TsMlsSnakDevice.create() : CoreCryptoSnakDevice.create()

type LogLine = { id: number; kind: 'sent' | 'received' | 'info' | 'error'; text: string; wire?: string }

function App() {
    const deviceRef = useRef<SnakDevice | null>(null)
    const [isReady, setIsReady] = useState(false)
    const [hasSnak, setHasSnak] = useState(false)
    const [otherKeyPackage, setOtherKeyPackage] = useState<{ from: string; bytes: Uint8Array } | null>(null)
    const [log, setLog] = useState<LogLine[]>([])
    const [draft, setDraft] = useState('')

    const addLog = (line: Omit<LogLine, 'id'>) => setLog((lines) => [...lines, {...line, id: lines.length}])

    useEffect(() => {
        let isCancelled = false

        // Messages are handled one after another; core-crypto must not run two of them at once.
        let queue = Promise.resolve()
        const stopListening = onMessage((message) => {
            queue = queue.then(async () => {
                const device = deviceRef.current
                if (!device || message.from === me) return
                try {
                    switch (message.type) {
                        case 'key-package-request':
                            send({type: 'key-package', from: me, bytes: await device.createKeyPackage()})
                            break
                        case 'key-package':
                            setOtherKeyPackage({from: message.from, bytes: message.bytes})
                            break
                        case 'welcome':
                            if (message.to !== me) return
                            await device.joinSnak(message.bytes)
                            setHasSnak(true)
                            addLog({kind: 'info', text: `${message.from} added you to a snak`})
                            break
                        case 'snak':
                            addLog({
                                kind: 'received',
                                text: `${message.from}: ${await device.decrypt(message.ciphertext)}`,
                                wire: toHexPreview(message.ciphertext),
                            })
                            break
                    }
                } catch (error) {
                    addLog({kind: 'error', text: `Could not handle ${message.type}: ${String(error)}`})
                }
            })
        })

        createDevice()
            .then(async (device) => {
                if (isCancelled) return
                deviceRef.current = device
                setIsReady(true)
                // Announce ourselves and ask whoever is already open for their key package.
                send({type: 'key-package', from: me, bytes: await device.createKeyPackage()})
                send({type: 'key-package-request', from: me})
            })
            .catch((error: unknown) => addLog({kind: 'error', text: `${library} failed: ${String(error)}`}))

        return () => {
            isCancelled = true
            stopListening()
        }
    }, [])

    async function startSnak() {
        const device = deviceRef.current
        if (!device || !otherKeyPackage) return
        try {
            const welcome = await device.startSnakWith(otherKeyPackage.bytes)
            send({type: 'welcome', from: me, to: otherKeyPackage.from, bytes: welcome})
            setHasSnak(true)
            addLog({kind: 'info', text: `Snak with ${otherKeyPackage.from} started`})
        } catch (error) {
            addLog({kind: 'error', text: `Could not start snak: ${String(error)}`})
        }
    }

    async function sendSnak(event: FormEvent) {
        event.preventDefault()
        const device = deviceRef.current
        if (!device || draft.trim() === '') return
        try {
            const ciphertext = await device.encrypt(draft)
            send({type: 'snak', from: me, ciphertext})
            addLog({kind: 'sent', text: `${me}: ${draft}`, wire: toHexPreview(ciphertext)})
            setDraft('')
        } catch (error) {
            addLog({kind: 'error', text: `Could not send: ${String(error)}`})
        }
    }

    return (
        <main>
            <h1>Kreds – MLS spike ({me}, {library})</h1>
            <p id="status">{isReady ? 'device ready ✓' : 'creating device…'}</p>

            {isReady && !hasSnak && (
                <button id="start" onClick={startSnak} disabled={!otherKeyPackage}>
                    {otherKeyPackage ? `Start snak with ${otherKeyPackage.from}` : 'Waiting for another tab…'}
                </button>
            )}

            {hasSnak && (
                <form onSubmit={sendSnak}>
                    <input id="draft" value={draft} onChange={(e) => setDraft(e.target.value)}/>
                    <button id="send" type="submit">Send</button>
                </form>
            )}

            <ul id="log">
                {log.map((line) => (
                    <li key={line.id} className={line.kind}>
                        {line.text}
                        {line.wire && <code> wire: {line.wire}</code>}
                    </li>
                ))}
            </ul>
        </main>
    )
}

export default App