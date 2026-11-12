# Kreds

> A calm, private place for the people you actually know. Made in the Nordics.

Kreds (Danish for *circle*) is a private, end-to-end encrypted social app for family and friends –
a Nordic alternative to Instagram, Facebook, Snapchat and WhatsApp. No ads, no strangers, no AI.

See [VISION.md](VISION.md) for the idea, the principles and all decisions.

**Status:** early prototype – Phase 0 (setup). Family and friends only.

## Stack

Java 25 · Spring Boot 4 · Postgres · OpenSearch · Keycloak · RabbitMQ · S3 (Garage locally) · React PWA

## Getting started

Requires Java 25, Docker and Docker Compose.

```
cp .env.example .env          # then change the passwords in .env
docker compose up -d          # Postgres, OpenSearch, Dashboards, Garage.
./mvnw verify                 # build all modules and run all tests
```

| Service               | Address               |
|-----------------------|-----------------------|
| Postgres              | `localhost:5432`      |
| OpenSearch            | http://localhost:9200 |
| OpenSearch Dashboards | http://localhost:5601 |
| Garage (S3)           | http://localhost:3900 |

First start? See [docs/local-setup.md](docs/local-setup.md)

## Documentation

- `docs/` – developer documentation (coming)

## Licence

Not yet licensed – all rights reserved. Planned: open source (AGPL-3.0) before going public.
