# Spike: MLS in the browser

Question: can Kreds do MLS end-to-end encryption in the browser – safely, and maintainably?

## Findings – @wireapp/core-crypto

| Criterion            | Finding                                                                                                                                     |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| Works in the browser | ✅ 11.0.0 loads in Chrome (Vite + React)                                                                                                     |
| Size                 | ⚠ WASM file 8.1 MB, ~3 MB compressed – a lot for older phones                                                                               |
| API stability        | ⚠ 3 major versions in 13 months (9.0 Sep 2025, 10.0 Jun 2026, 11.0 Oct 2026); Wire's own playground (9.3) no longer matches the current API |
| Licence              | GPL-3.0                                                                                                                                     |
| Audited              | OpenMLS core: yes (SRLabs, 2026)                                                                                                            |

## Findings – ts-mls

(Step 3b)

| Question                                  | Result                                                                                                                                                                  |
|-------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Encrypted 1:1 message between two devices | ✅ Works both ways                                                                                                                                                       |
| Tampered message                          | ✅ Rejected                                                                                                                                                              |
| Message length hidden                     | ✅ Padded (short messages are all 190 bytes)                                                                                                                             |
| What the server can see                   | ⚠ The header is readable: group id and epoch (metadata, by design in MLS); content is not                                                                               |
| API ceremony                              | ⚠ Wire-shaped `ClientId` (UUID + device id + domain), a mandatory `MlsTransport` callback, everything inside transactions                                               |
| Error messages                            | ⚠ Vague: a tampered message gives only `CoreCryptoError.Mls`                                                                                                            |
| Key storage                               | ⚠ Spike uses `Database.inMemory()`; persistent storage (`Database.open` + a 32-byte key) raises the question of where that key lives, which ties to our recovery design |
