# Kreds – context

Everything a new chat needs besides `VISION.md`: where we are, how we work, and what the user already knows.

---

## 1. Where we are

|                        |                                                                                       |
|------------------------|---------------------------------------------------------------------------------------|
| Repository             | https://github.com/tysker/kreds (public for now, will become private)                 |
| Licence                | None yet (all rights reserved). Direction: AGPL-3.0 before going public               |
| Base package / groupId | `dk.nordics.kreds` / `dk.nordics` – main class `KredsApplication` in the base package |
| Current phase          | **Phase 0 – Setup**, Step 4 (Garage object storage)                                   |

### Phase 0 – progress

| Step | What                                                                                                                          | Status |
|------|-------------------------------------------------------------------------------------------------------------------------------|--------|
| 1    | Repository: `VISION.md`, `README.md`, `.gitignore`, pull request template                                                     | ✅ done |
| 2    | Maven skeleton: `kreds-core`, `kreds-api`, `kreds-app`; Java 25; `KredsApplicationIT` (context loads)                         | ✅ done |
| 3    | Docker Compose: Postgres 17, OpenSearch 3.9.0 + Dashboards; `.env` / `.env.example`; ports bound to `127.0.0.1`; healthchecks | ✅ done |
| 4    | Garage (S3-compatible object storage) in Docker Compose                                                                       | next   |

### Phases

| Phase  | What                                                                                                                                                                                                                   |
|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **0**  | Repository, Maven multi-module skeleton, Docker Compose (Postgres, OpenSearch + Dashboards, Garage)                                                                                                                    |
| **0b** | **Mockup** (parallel to Phase 0/1): the MVP screens + *fika*, light and dark – first draft done as a Claude Design canvas ("Kreds – first mockup"). Refine on paper / in Penpot; becomes the reference for `kreds-web` |
| **1**  | **Encryption spike:** two browsers, one encrypted 1:1 *snak*, with an MLS library. Criteria: works in the browser, maintained, licence fits copyleft, understandable docs. Fallback: Signal Protocol                   |
| **2**  | Keycloak: invite-only login (*bifrost* links), *kredse*                                                                                                                                                                |
| **3**  | *Glimt*: encrypted text + photo, *Stuen*                                                                                                                                                                               |
| **4**  | ***Fika***: get-togethers with answers (coming / maybe / can't), own *svar* thread, calendar file, *tak*                                                                                                               |
| later  | See "Features → Later" in `VISION.md`; OpenSearch jobs (logs/audit, places, metadata statistics); Kubernetes                                                                                                           |

---

## 2. How we work

- **One step at a time.** Each step: what, why, exact files, expected result. The user says when it's done.
- More code from Claude than in a pure learning project, to reach a prototype – but always explained.
- **Tests:** Claude says what to test and why; the user writes them. Hints before solutions.
- Code in **English**; Nordic words only as UI labels.
- **Doc updates:** Claude updates the project's markdown files directly; the user copies them into the repo.

### The step checklist

Every step / phase follows this checklist. It also lives in the repo as the pull request template.

1. **Understand** – what are we doing, and why?
2. **Branch** – from an up-to-date `main`: `git switch main && git pull && git switch -c <type>/<short-name>`
   (e.g. `chore/maven-skeleton`, `docs/adr-encryption`, `spike/mls-snak`)
3. **Test first** – write the test (or, for non-code steps, the documentation) that describes the goal. See it fail.
4. **Code** – until the test is green.
5. **Run everything** – `./mvnw verify` (unit + integration tests).
6. **Break check** – break the code on purpose; the test must fail. Then restore.
7. **Document** – README, `docs/`, diagrams, an ADR for decisions, `VISION.md` if something changed.
8. **Commit** – small commits, **one message per commit**, Conventional Commits (Claude suggests the message).
9. **Push + pull request** – review your own diff on GitHub; Claude reviews the branch.
10. **Squash and merge** (one clean commit on `main`), delete the branch, `git switch main && git pull`, tick the
    roadmap.

### Commit messages – Conventional Commits

```
<type>(<scope>): <what, in the imperative, lower case>
```

| Type       | For                                  |
|------------|--------------------------------------|
| `feat`     | A new feature                        |
| `fix`      | A bug fix                            |
| `test`     | Adding or changing tests             |
| `docs`     | Documentation only                   |
| `refactor` | Code change without new behaviour    |
| `chore`    | Build, dependencies, config, tooling |
| `spike`    | Throw-away experiments (Phase 1)     |

Examples: `feat(core): add Circle entity and repository` · `test(search): add security filter tests` ·
`docs: add ADR for end-to-end encryption` · `chore: add docker compose for postgres and opensearch`

---

## 3. Documentation plan

| Audience       | Where                                                                    | What                                                                                                 |
|----------------|--------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| **Developers** | `docs/` in the repo (Markdown + Mermaid), later published as a docs site | Architecture, flows, database overview (ERD), class diagrams, REST API (generated OpenAPI), **ADRs** |
| **Users**      | A separate simple website, later                                         | What Kreds is, the words, the privacy promise, the story; later short explainer videos               |

**ADRs (Architecture Decision Records):** one short file per decision in `docs/adr/` – context, decision, consequences.
First candidates: end-to-end encryption with MLS, no AI, Nordic hosting, data classification, recovery in two layers,
Garage instead of MinIO.

---

## 4. Scaling and Kubernetes

Not now. Docker Compose for development and the family prototype on one server. But build so that moving to
Kubernetes later is easy:

- The app is **stateless** – no data on the app server's disk; everything in Postgres, object storage, OpenSearch.
- **Configuration via environment variables** (`.env`, never committed; `.env.example` documents the variables).
- **Health endpoints** (Spring Boot Actuator) for liveness/readiness.
- Everything runs as **containers**.
- WebSockets need attention when scaling to several app instances (shared message broker) – later.

Kubernetes becomes its own learning phase when Kreds outgrows one server (Nordic providers offer managed Kubernetes).

---

## 5. What the user already knows (from the notifyhub project)

- Spring Boot 4 multi-module Maven, Java 21 (Kreds uses 25), Lombok, `@ConfigurationProperties`, `ApplicationRunner`,
  `@Order`
- `RestClient`, Jackson 3 (`ObjectMapper`, `JsonNode`, `ObjectNode`), central error handling (`defaultStatusHandler`)
- OpenSearch: index, mapping (`text` vs `keyword`, multi-fields, `dynamic: strict`), CRUD, `_bulk`, `match` vs `term`,
  `terms`, `_analyze`, aliases, `_reindex`, atomic alias switch, `_update_by_query`, `nested` vs `object`,
  sub-aggregations (used at work); rollover and ISM are used at work
- Mapping as Java code (`MappingBuilder`, incl. `nested` with a `Consumer`), index setup on startup, idempotency
- Postgres as source of truth for the index setup: Flyway, JPA entity, `ddl-auto=validate`, JSONB
- Tests: JUnit 5, Mockito, `@DataJpaTest` + Testcontainers, Surefire vs Failsafe (`*Test` vs `*IT`) – see
  `TESTING-NOTES.md`
- At work: OpenSearch with REST + JSON and own DTOs (Spring Boot 2.x, no high-level client), RabbitMQ, S3 for raw data
- React (frontend experience)