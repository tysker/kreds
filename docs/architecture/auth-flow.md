# Login and identity flow

How Kreds uses **Keycloak** for login and **Postgres** for everything the app itself knows about
users and their *kredse* – and how the two are connected.

> **Status:** design for Phase 2. The split (Keycloak = identity, Postgres = *kredse* and membership)
> is agreed and will be recorded as ADR-0002. Details marked **(proposal)** are not decided yet.

---

## 1. Who owns what

The one-sentence version: **Keycloak knows *who you are*. Postgres knows *which kredse you belong to*.
Neither knows *what you wrote*** – that is end-to-end encrypted (see [encryption-flow.md](encryption-flow.md)).

```mermaid
flowchart LR
    pwa["kreds-web<br/>React PWA"]

    subgraph kc["Keycloak – identity only"]
        realm["Realm: kreds<br/>accounts, passwords, passkeys,<br/>sessions, app-wide roles"]
    end

    subgraph app["Spring Boot (kreds-app)"]
        api["kreds-api<br/>OAuth2 resource server"]
        core["kreds-core<br/>kredse, membership, rules"]
        api --> core
    end

    subgraph pg["Postgres 17"]
        kcdb[("database: keycloak<br/>owned by Keycloak")]
        kredsdb[("database: kreds<br/>owned by Kreds, Flyway")]
    end

    pwa -- "(1) login (Authorization Code + PKCE)" --> realm
    realm -- "(2) access token (JWT)" --> pwa
    pwa -- "(3) API call + Bearer JWT" --> api
    api -. "(4) fetch public signing keys (JWKS)" .-> realm
    realm --> kcdb
    core --> kredsdb
```

| | Keycloak | Postgres `kreds` database |
|---|---|---|
| **Holds** | Email, password hash, passkeys, sessions, login history, app-wide roles (e.g. operator admin) | Users (only the Keycloak ID), *kredse*, membership + role, invitations (*bifrost*), metadata, encrypted blobs |
| **Written by** | Keycloak only | `kreds-core` only, schema via Flyway |
| **Read by Spring Boot** | Never directly – only through the JWT | On every request |

### The link between the two: `sub`

Every Keycloak user has a stable, random ID – the `sub` claim in the JWT
(e.g. `3f2c9a1e-…`). Kreds uses it as the user ID in Postgres. **Nothing else is copied**:
no email, no password, no name from Keycloak lands in the `kreds` database.

```mermaid
erDiagram
    KEYCLOAK_USER ||--|| APP_USER : "sub = id"
    APP_USER ||--o{ CIRCLE_MEMBER : "is member"
    CIRCLE ||--o{ CIRCLE_MEMBER : "has"
    CIRCLE ||--o{ INVITATION : "bifrost links"

    KEYCLOAK_USER {
        uuid id "the sub claim"
        string email
        string credentials
    }
    APP_USER {
        uuid id PK "= Keycloak sub"
        timestamp created_at
    }
    CIRCLE {
        uuid id PK
        bytes ciphertext "name is encrypted"
        timestamp created_at
    }
    CIRCLE_MEMBER {
        uuid circle_id FK
        uuid user_id FK
        string role "owner or member"
        timestamp joined_at
    }
    INVITATION {
        uuid id PK
        uuid circle_id FK
        uuid invited_by FK
        string token_hash "never the token itself"
        timestamp expires_at
        timestamp used_at
    }
```

`KEYCLOAK_USER` lives in the `keycloak` database and is shown only to explain the link – Kreds never
queries it. Table and column names are a **(proposal)** for the Flyway migrations in Phase 2.

---

## 2. Logging in (Authorization Code + PKCE)

The PWA is a public client: it cannot keep a secret. PKCE proves that the app which started the
login is the same app that picks up the token.

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant W as kreds-web
    participant K as Keycloak
    participant A as kreds-api
    participant P as Postgres (kreds)

    U->>W: open Kreds
    W->>W: create code_verifier + code_challenge
    W->>K: redirect to login page (code_challenge)
    U->>K: password or passkey
    K->>K: check credentials (stored in keycloak database)
    K-->>W: redirect back with authorization code
    W->>K: exchange code + code_verifier
    K-->>W: access token (JWT, short-lived) + refresh token
    W->>A: GET /api/... with Authorization Bearer JWT
    A->>A: validate signature (JWKS), issuer, audience, expiry
    A->>P: find app_user by sub (create on first visit)
    P-->>A: user row
    A-->>W: response
```

Spring Boot validates the JWT **locally** with Keycloak's public keys (fetched once and cached).
It does not call Keycloak on every request.

---

## 3. Every request: token for identity, Postgres for membership

```mermaid
sequenceDiagram
    autonumber
    participant W as kreds-web
    participant A as kreds-api
    participant C as kreds-core
    participant P as Postgres (kreds)

    W->>A: GET /api/circles/{circleId}/glimt + JWT
    A->>A: JWT valid? → user id = sub
    A->>C: list glimt (circleId, sub)
    C->>P: is sub a member of circleId?
    alt member
        P-->>C: yes, role = member
        C->>P: load glimt metadata + ciphertext
        C-->>A: encrypted glimt
        A-->>W: 200 + ciphertext (decrypted on the device)
    else not a member (or removed a minute ago)
        P-->>C: no
        C-->>A: access denied
        A-->>W: 404 (does not reveal that the kreds exists)
    end
```

**Why membership is not in the token:** a JWT is valid until it expires. If *kredse* were Keycloak
groups inside the token, a removed member could keep reading until then – and the token would carry
*kreds* names, which must stay encrypted. Reading membership from Postgres on every request makes
removal take effect immediately.

Returning `404` instead of `403` for a foreign *kreds* is a **(proposal)**.

---

## 4. Joining through a *bifrost* link (invite-only)

Kreds has no public sign-up. A new person can only get an account through a *bifrost* link from a
member. **(proposal)** – the exact mechanism is decided in ADR-0002. The flow below uses Keycloak's
Admin API with a dedicated, least-privilege service account.

```mermaid
sequenceDiagram
    autonumber
    actor M as Member (Anna)
    actor N as New person (Erik)
    participant W as kreds-web
    participant A as kreds-api + kreds-core
    participant P as Postgres (kreds)
    participant K as Keycloak

    M->>W: invite to "Family"
    W->>A: POST /api/circles/{id}/invitations
    A->>P: Anna is a member? store hash(token), expires_at
    A-->>W: bifrost link with token (shown once)
    M-->>N: sends the link (email, message, QR)

    N->>W: opens bifrost link
    W->>A: check token
    A->>P: hash matches, not expired, not used?
    A->>K: create user (service account, Admin API)
    K-->>A: new user id (sub)
    A->>P: insert app_user(sub), circle_member, mark invitation used
    A-->>W: continue to set up login
    W->>K: Erik sets passkey or password
    Note over N,K: From now on: normal login (section 2)
```

Afterwards Erik's devices still have to join the *kreds*' MLS group (KeyPackage → Welcome), as in
[encryption-flow.md](encryption-flow.md). Login gives him an account, not the keys.

---

## 5. Postgres: one server, two databases

**(proposal)** Locally, Keycloak and Kreds share the existing Postgres 17 container, but each gets its
**own database and its own database user**:

| Database | User | Who creates the schema | Who may connect |
|---|---|---|---|
| `kreds` | `kreds` | Flyway (Spring Boot) | Spring Boot only |
| `keycloak` | `keycloak` | Keycloak itself, on start | Keycloak only |

Why separate:
- **Least privilege** – a bug in Kreds cannot read password hashes, and Keycloak cannot touch *kreds* data.
- Keycloak manages and migrates its own schema on upgrades; Flyway must never see it.
- Backups and restores can be done per database.
- Later they can move to two Postgres servers without changing any code.

---

## 6. Deleting an account

A user lives in two places, so deletion must cover both (`VISION.md` § 10):

1. `kreds-core` removes the user's rows in the `kreds` database (membership, own content, invitations).
2. `kreds-api` deletes the Keycloak user through the Admin API.
3. The audit log records *that* an account was deleted – never its content.

Order and retry on partial failure are **(proposal)** – details in Phase 2.

---

## 7. Rules

- Keycloak is **identity only**. No *kredse*, no membership, no content in Keycloak.
- The `sub` claim is the only link. Nothing else is copied from Keycloak to Postgres.
- Every request checks membership in Postgres, never from the token.
- Spring Boot never connects to the `keycloak` database.
- Keycloak is self-hosted (no US identity services). Its version is pinned, and Dependabot updates
  are merged promptly – only the latest release gets security fixes.
- Tokens, passwords and email addresses never appear in logs.

## Related

- ADR-0002 (planned) – Keycloak for identity only; *kreds* membership in Postgres
- [encryption-flow.md](encryption-flow.md) – what happens after login: MLS groups and encrypted messages
- [RFC 7636 – PKCE](https://www.rfc-editor.org/rfc/rfc7636)
