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