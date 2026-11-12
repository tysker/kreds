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

| Question                                  | Result                                                                                                                                                                  |
|-------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Encrypted 1:1 message between two devices | ✅ Works both ways                                                                                                                                                       |
| Tampered message                          | ✅ Rejected                                                                                                                                                              |
| Message length hidden                     | ✅ Padded (short messages are all 190 bytes)                                                                                                                             |
| What the server can see                   | ⚠ The header is readable: group id and epoch (metadata, by design in MLS); content is not                                                                               |
| API ceremony                              | ⚠ Wire-shaped `ClientId` (UUID + device id + domain), a mandatory `MlsTransport` callback, everything inside transactions                                               |
| Error messages                            | ⚠ Vague: a tampered message gives only `CoreCryptoError.Mls`                                                                                                            |
| Key storage                               | ⚠ Spike uses `Database.inMemory()`; persistent storage (`Database.open` + a 32-byte key) raises the question of where that key lives, which ties to our recovery design |

## Findings – ts-mls

| Question                                  | Result                                                                                                                                  |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Encrypted 1:1 message between two devices | ✅ Works both ways                                                                                                                       |
| Tampered message                          | ✅ Rejected (`CryptoError: OperationError`)                                                                                              |
| Message length hidden                     | ✅ Padded (short messages are all 334 bytes)                                                                                             |
| Size                                      | ✅ ~131 kB JS, ~36 kB gzip, no WASM (core-crypto: ~300 kB JS + 8.1 MB WASM, ~3 MB gzip)                                                  |
| Packaging                                 | ⚠ 1.6.4 imports `@noble/hashes` without declaring it; we must install it ourselves                                                      |
| API style                                 | ✅ Plain functions and objects, readable; ⚠ we must keep the new state after every call and wipe used keys ourselves (easy to get wrong) |
| Key storage                               | ⚠ Nothing built in; `encodeGroupState` exists, but storing it encrypted (e.g. in IndexedDB) is our job                                  |
| Audited                                   | ⚠ No (the README says so itself); had a high-severity advisory fixed in 1.6.4                                                           |
| Licence                                   | ✅ MIT                                                                                                                                   |

