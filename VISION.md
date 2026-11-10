# Kreds – Vision

> **A calm, private place for the people you actually know. Made in the Nordics.**

Kreds (Danish for *circle*) is a private social app for family and friends: share moments, chat, plan get-togethers
and keep memories – without ads, without strangers, without AI and without an algorithm deciding what you see.

It is meant as a Nordic alternative to Instagram, Facebook, Snapchat and WhatsApp.

**The promise:** *Only the people in your kreds can see what you share. Not us, not anyone else.*

---

## 1. Who it is for

1. **First: family and friends.** Invite-only. The goal is to learn what works and what doesn't, from real users.
2. **Later, maybe: the public in the Nordic countries** (and later perhaps Iceland and the Faroe Islands).
   Kreds is designed so that opening up is a decision, not a rewrite (see [Going public later](#10-going-public-later)).

---

## 2. Principles

| Principle               | What it means                                                                                                          |
|-------------------------|------------------------------------------------------------------------------------------------------------------------|
| **Private by design**   | Content is **end-to-end encrypted**. Only the members of a kreds can read what is shared in it.                        |
| **Calm by design**      | Chronological feed, no streaks, no endless scroll tricks, few and bundled notifications, a button for peace (*hygge*). |
| **No AI**               | No AI reads your photos or messages. A deliberate choice against the tide.                                             |
| **Made in the Nordics** | Nordic design, Nordic words, data stored in the Nordics. Europe is acceptable, nothing outside Europe.                 |
| **Built to grow**       | Everything belongs to a kreds. Never assume "there is only one family".                                                |
| **Listen to the users** | In-app feedback from day one.                                                                                          |

---

## 3. The words

The app is in **English**, with Nordic words for its core concepts. The first time a user sees a word,
a short explanation is shown, e.g. *"glimt – Danish for glimpse"*.

| In the app                        | Word                 | Language            | Meaning                                                   |
|-----------------------------------|----------------------|---------------------|-----------------------------------------------------------|
| App / a group                     | **Kreds** / *kredse* | Danish              | circle                                                    |
| Home feed                         | **Stuen**            | Danish              | the living room                                           |
| A post                            | **glimt**            | Danish              | a glimpse                                                 |
| A 1:1 chat                        | **snak**             | Danish              | a chat, a talk                                            |
| A group chat                      | **prat**             | Swedish / Norwegian | a chat, a talk                                            |
| A comment                         | **svar**             | Danish              | a reply                                                   |
| The like                          | **koselig**          | Norwegian           | cosy                                                      |
| Event album                       | **hetki**            | Finnish             | a moment                                                  |
| Trip album (on the map)           | **tur**              | Norwegian           | a trip, an outing                                         |
| "On this day"                     | **minder**           | Danish              | memories                                                  |
| Notifications                     | **nyt**              | Danish              | news, something new                                       |
| Search                            | **find**             | Danish / English    | find                                                      |
| Do not disturb                    | **hygge**            | Danish              | cosiness, peace                                           |
| Plan a get-together               | **fika**             | Swedish             | coffee and cake together                                  |
| Thank the host / the photographer | **tak**              | Danish              | thanks ("tak for i dag" – thanks for today)               |
| Notification setting              | **lagom**            | Swedish             | just the right amount                                     |
| A quick wave                      | **moi**              | Finnish             | hi!                                                       |
| Weekly summary (Sunday)           | **ting**             | Old Norse / Nordic  | the assembly – and in Danish also "things" ("Ugens ting") |
| Month / year in review            | **saga**             | Old Norse / Nordic  | a story ("Your saga 2026")                                |
| Invitation link                   | **bifrost**          | Norse mythology     | the rainbow bridge between the worlds                     |
| Family tree (later)               | **Yggdrasil**        | Norse mythology     | the world tree                                            |

**Rule:** the code uses **English names only** (`Circle`, `Post`, `Comment`, `Album`, `Reaction`, `Chat`).
The Nordic words exist **only as UI labels**. That keeps them easy to change and makes more languages possible later.

### How some words work

- **Stuen** – one combined feed of all your *kredse*, with a filter to show just one.
- **koselig** – the one reaction on a *glimt*: "this made me feel cosy".
- **tak** – for people, not posts. After a *fika*, guests get a "Send tak" button that sends the host
  *"Tak for i dag!"*. Also a one-tap answer when someone adds photos of you to a *hetki*.
- **fika** – a get-together inside a *kreds*: what, when, where. Members answer *I'm coming*,
  *Maybe* or *Can't come*. Every *fika* has its own *svar* thread, so planning the julefrokost
  doesn't get mixed up with everything else. "Add to my calendar" creates a calendar file on the device.
- **hygge** – a button. Pressed: no notifications at all, until pressed again. Messages still arrive;
  everything is waiting in *Stuen* and *nyt* afterwards. **Optional per user:** others may see a small
  hygge symbol next to your name.
- **lagom** – decides *how many* notifications you get in general (e.g. bundled). *Hygge* means *none, right now*.
- **tak for sidst** – when the same people are about to meet again (e.g. the next *fika*), Kreds offers a gentle
  *"Say tak for sidst to Anna?"* ("thanks for last time").
- **ting** – once a week, on Sunday, a calm overview: *"This week's ting: 12 new glimt in Family, 2 fika coming up"*.
  Counts only – the content is shown when the app is opened.
- **saga** – a month or year in review, from metadata: *"Your saga 2026"*.
- **bifrost** – the invitation link that brings someone into your *kreds*.

---

## 4. Features

### Before anything else: the encryption spike

A small prototype to prove end-to-end encryption works in the browser: **two browsers, one 1:1 snak**,
encrypted with an established library. If it works, the rest is designed around it.

### First version (MVP)

1. Invite-only login via *bifrost* links; *kredse* (e.g. "Family", "Friends")
2. *Glimt*: text + one photo, shared with a *kreds* – end-to-end encrypted.
   Photos are resized on the device to max 2048 px on the long side (sharp on any phone, ~0.3–1 MB),
   plus a small thumbnail for the feed
3. *Stuen*: chronological feed of the *kredse* you belong to
4. *Svar* (comments) and *koselig* (reactions)
5. *Find*: search in posts and comments – **on the device**
6. *Nyt*: notifications ("new glimt in Family") – never with content
7. *Hygge* button
8. In-app feedback
9. React PWA – installable on the phone from a link, no app store

### Later

- *Fika* – plan get-togethers (date, place, who's coming), each with its own *svar* thread, + *tak*.
  **First feature after the MVP.**
- *Snak* (1:1) and *prat* (group) – live chat
- *Hetki* – shared event albums
- *Tur* – trip albums and a map of places
- *Minder* – "on this day"
- *Moi* – a quick wave
- *Lagom* notification settings
- *Ting* – the Sunday summary
- *Saga* – month / year in review (from metadata)
- *Tak for sidst*
- Videos – max 60 seconds, max 1080p, MP4 (H.264 + AAC), roughly 5 Mbit/s → at most ~40 MB;
  conversion and thumbnails on the device, before encrypting
- *Yggdrasil* – a family tree

---

## 5. Privacy and security

### End-to-end encryption

Kreds works like WhatsApp and Signal: **keys live only on the devices**, and the server passes on encrypted
packages it cannot open.

- Every device has its own key pair. Only the **public** key is uploaded to the server.
- A *kreds* is an encrypted group. Adding or removing a member changes the group keys.
- Photos and videos are encrypted on the device with a fresh key, uploaded encrypted to object storage;
  the key travels inside the encrypted message.
- **Never our own crypto.** We use **MLS** (Messaging Layer Security, an open standard made for encrypted groups) –
  almost everything in Kreds is a group. The spike checks the library's maturity in the browser and its licence.
  Fallback: the Signal Protocol.
- **Honest caveat:** in a web app, the server delivers the code that encrypts. Native apps reduce that risk later.

### Recovery – two layers

1. **Link a new device from an old one** (main way): the old phone shows a QR code, the new phone scans it,
   the keys are passed over directly and encrypted. No secret is stored anywhere.
2. **Recovery key** (backup, when the old phone is lost): a long random key, created once by the app,
   shown as words or a code. The user stores it in a password manager or prints it. Lost key = lost history –
   said clearly.

**No short PIN:** without special security hardware on the server, a PIN could simply be guessed by whoever
holds the encrypted backup – including the server operator.

### Data classification

**Rule: when in doubt, encrypt.** Data the server has seen cannot be "unseen". Every server-visible field needs a
reason.

| Level                                                                | What                                                                                                                                                                                                                   | Why                                                                                          |
|----------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| **End-to-end encrypted**                                             | Text of *glimt*, *svar*, *snak*, *prat*; photos and videos; *fika* details and answers (who's coming); names of *kredse*, *hetki* and *tur*; locations                                                                 | The actual private life                                                                      |
| **Server-visible, protected** (encrypted at rest, access-controlled) | Email, display name, kreds membership, IDs, timestamps, file sizes, reactions (*koselig*: who and how many), *lagom*/*hygge* settings, public keys, devices, audit log, date and time of a *fika* (no title, no place) | Needed for invites, delivery, notifications, reminders, *ting*, *minder*, *saga*, statistics |
| **Not personal**                                                     | OpenStreetMap places, word explanations, app texts                                                                                                                                                                     | Public anyway                                                                                |

**In the code:** every entity has its **metadata columns** plus **one encrypted blob** (`ciphertext`) for all content.
A test proves that no content fields exist in the database schema or the OpenSearch mappings.

The app tells users openly what the server knows (metadata) and what it cannot see (content).

### Further rules

1. **Invite-only.** No public sign-up.
2. **Every request checks membership** – even for encrypted data.
3. **Private storage.** Buckets are never public; files are served through short-lived signed links.
4. **Location data is optional** – removed from photos and videos on the device, unless the user wants it (e.g. for a
   *tur*).
5. **Minimal push notifications.** Push travels through the browser makers' services (Google, Apple, Mozilla) –
   unavoidable on phones. The payload is encrypted and only says something like *"You have nyt"*.
6. **Security audit log** of logins, invites, failed access attempts – users can see their own login history.

---

## 6. Where data lives

**Rule: Nordic first. Europe is acceptable. Nothing outside Europe.**
This applies to every service, including the ones that sneak in unnoticed.

| What                               | Choice                                                                                                            |
|------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| Servers                            | A Nordic provider (planned: **UpCloud**, Helsinki)                                                                |
| Object storage – development       | **MEGA S4**, European endpoint (Amsterdam or Luxembourg), **test data only** (MEGA is a non-European company)     |
| Object storage – before real users | **UpCloud** Managed Object Storage, Helsinki                                                                      |
| Object storage – local             | **Garage** in Docker (S3-compatible, open source, made in Europe) – replaces MinIO, which is no longer maintained |
| Fonts                              | Self-hosted, no font CDN                                                                                          |
| Maps                               | OpenStreetMap-based tiles from a European provider                                                                |
| Email (invites)                    | A European email provider                                                                                         |
| Analytics                          | None, or self-hosted                                                                                              |
| Push notifications                 | Browser push services (exception, see above)                                                                      |

The code only talks the **S3 API**, so switching storage providers is configuration, not code.

---

## 7. Design

- **Nordic look:** off-white and light grey backgrounds, a few muted colours (fjord blue, moss green, birch),
  lots of white space, one clean sans-serif font, soft rounded shapes.
- **Dark mode** – for the long winters.
- **Calm:** no red badges everywhere, no infinite scroll tricks, chronological order.
- **Mobile first**, but works on desktop.
- **App symbol: a simple, modern Yggdrasil tree** – e.g. inside a circle (the *kreds*). An original design,
  clean lines, no traditional or copied depiction.
- **No runes and no Viking symbols** anywhere in the design. Some of them are also used by extremist groups;
  Kreds should never be mistaken for that. Nordic words like *ting* and *saga* are everyday words and fine.

---

## 8. Tech stack

| Part               | Choice                                                                    |
|--------------------|---------------------------------------------------------------------------|
| Backend            | Java 25, Spring Boot 4, Maven multi-module, Lombok                        |
| Database           | Postgres + Flyway                                                         |
| Search & analytics | OpenSearch 3.x, via REST + JSON (own DTOs, no high-level client)          |
| Login              | Keycloak (invite-only, *kredse* as groups)                                |
| Events             | RabbitMQ                                                                  |
| Files              | S3 API (Garage locally)                                                   |
| Real-time          | WebSockets                                                                |
| Encryption         | MLS library (verified in the spike; fallback: Signal Protocol)            |
| Frontend           | React + Vite + TypeScript, as a PWA                                       |
| Tests              | JUnit 5, Mockito, `@DataJpaTest`, Testcontainers – see `TESTING-NOTES.md` |

### Modules

| Module         | Responsibility                                              | Comes from         |
|----------------|-------------------------------------------------------------|--------------------|
| `kreds-app`    | Main class, configuration                                   | `notifyhub-app`    |
| `kreds-api`    | REST + WebSocket endpoints                                  | new                |
| `kreds-core`   | Circles, posts, members, keys, rules                        | new                |
| `kreds-search` | OpenSearch: mappings, index setup, logs, places, statistics | `notifyhub-search` |
| `kreds-notify` | Notifications (push, email)                                 | `notifyhub-core`   |
| `kreds-web`    | React PWA, incl. encryption and on-device search            | new                |

---

## 9. OpenSearch in Kreds

OpenSearch **never** holds private content. It has three jobs:

| Job                                                                         | OpenSearch topics                                                                                             |
|-----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| **Logs, metrics and security audit**                                        | Time-based indices, rollover + ISM, index templates, aliases, dashboards, alerts, raw data in object storage  |
| **Places search** for *fika* and *tur* (OpenStreetMap data for the Nordics) | Search-as-you-type, `geo_point`, geo distance, analyzers for Nordic spelling (*Århus* = *Aarhus*, *ø* = *oe*) |
| **Statistics on metadata** (year in review, Sunday summary counts)          | Aggregations and sub-aggregations, date histograms                                                            |

Place searches for *fika* and *tur* reach the server (public OpenStreetMap data), but the queries are
never stored or logged. The chosen place is part of the encrypted *fika*.

**Memory:** a family-sized Kreds runs fine with a small heap (512 MB–1 GB). Rule of thumb: at most half of the server's
RAM as heap.

---

## 10. Going public later

**Prepared from the start:**

- Everything belongs to a kreds, and every request is checked against membership.
- Sign-up through Keycloak – open registration later is mostly configuration.
- English code, Nordic words only as UI labels.

**Needed before going public:**

- Storage quotas per user, rate limiting, scaling.
- **Legal:** privacy policy, data export and deletion, agreements with every service used,
  handling reports of illegal content. Needs proper legal advice.

---

## 11. Licence

The repository is **public for now, without a licence** – that means *all rights reserved*: anyone can look,
nobody may use the code. It may become private later.

**Direction: open source with copyleft (e.g. AGPL-3.0)** before going public:

- **Trust:** for an end-to-end encrypted app, anyone can check that the encryption does what we promise.
- **Protection:** whoever runs a modified version as a service must publish their changes – big companies
  cannot simply take Kreds and keep their improvements closed.
- Note: copyleft does not *forbid* others from using the code – it forces them to share. The licences of the
  libraries we use must be compatible. To be checked properly before going public.

---

## 12. Open questions

- Which MLS library? (decided in the spike)
- Photo and video limits: the values above are a starting point – adjust after feedback from the family.
- More Swedish and Finnish words as features appear; Icelandic and Faroese when new features come,
  or when an existing word is swapped.
- The Yggdrasil app symbol: first draft exists (`kreds-symbol-draft.svg`) – refine later, maybe with a designer.
- A *kreds* inside a *kreds* (e.g. a work *kreds* planning julefrokost, summer party, ...): not decided.
  Options: *fika* threads only · grouping *kredse* on the device · real nested *kredse* (large: own
  encrypted group and keys). Revisit when real users ask for it.