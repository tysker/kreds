import {useEffect, useState} from 'react'
import {loadCoreCrypto} from './crypto/coreCrypto'

type Status =
    | { state: 'loading' }
    | { state: 'ready'; version: string }
    | { state: 'failed'; message: string }

function App() {
    const [status, setStatus] = useState<Status>({state: 'loading'})

    useEffect(() => {
        loadCoreCrypto()
            .then((version) => setStatus({state: 'ready', version}))
            .catch((error: unknown) => setStatus({state: 'failed', message: String(error)}))
    }, [])

    return (
        <main>
            <h1>Kreds – MLS spike</h1>
            {status.state === 'loading' && <p>Loading core-crypto…</p>}
            {status.state === 'ready' && <p>core-crypto {status.version} loaded ✓</p>}
            {status.state === 'failed' && <p>core-crypto failed to load: {status.message}</p>}
        </main>
    )
}

export default App