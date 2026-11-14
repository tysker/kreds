# Local setup

## Garage (S3 object storage) – one-time setup

Garage is a cluster by design. Even a single local node must be told its role once.
The setup is stored in the Docker volume, so it is only needed after the first start
(or after `docker compose down -v`).

```
# 1. Find the node id
docker exec kreds-garage /garage status

# 2. Give the node a role: zone "dc1", 1 GB capacity – then apply
# <node-id> = the ID column from "garage status"
docker exec kreds-garage /garage layout assign -z dc1 -c 1G <node-id>
docker exec kreds-garage /garage layout apply --version 1

# 3. Create the bucket and the app's access key, and allow the key on the bucket
docker exec kreds-garage /garage bucket create kreds-media
docker exec kreds-garage /garage key create kreds-app
docker exec kreds-garage /garage bucket allow --read --write --owner kreds-media --key kreds-app
```

Copy the key id and secret from `key create` into `.env`
(`GARAGE_ACCESS_KEY_ID`, `GARAGE_SECRET_ACCESS_KEY`).

## Postgres – databases and roles

On the first start (empty volume), `postgres/init/10-create-databases.sh` creates:

| Database   | Owner role | Used by              |
|------------|------------|----------------------|
| `kreds`    | `kreds`    | Spring Boot (Flyway) |
| `keycloak` | `keycloak` | Keycloak             |

Neither role is a superuser, and neither can connect to the other's database.
The `postgres` superuser is for maintenance only.

The script runs **only when the volume is empty**. After changing it, recreate the volume
(this deletes all local Postgres data):

    docker compose down
    docker volume rm kreds_postgres-data
    docker compose up -d postgres

## Keycloak

Runs in development mode (`start-dev`) on http://localhost:8180, with its data in the `keycloak` database.

- Admin console: http://localhost:8180/admin – log in with `KEYCLOAK_ADMIN_USER` / `KEYCLOAK_ADMIN_PASSWORD`.
- This bootstrap admin is created on the **first start only** and is meant to be temporary. Changing the
  values in `.env` later has no effect.
- The `master` realm is only for administering Keycloak. Kreds uses its own realm `kreds`.
- Health: port 9000 inside the container (used by the Docker health check, not published).

### Realm `kreds` (as code)

The realm lives in `keycloak/realm/kreds-realm.json` and is imported on startup (`--import-realm`).

- **Imported only if the realm does not exist yet.** After changing the file, delete the realm in the
  admin console (switch to realm `kreds` → *Realm settings* → *Action* → *Delete*) and restart Keycloak:
  `docker compose restart keycloak`.
- Two realms: `master` (only for administering Keycloak, where `KEYCLOAK_ADMIN_USER` lives) and `kreds`
  (Kreds users). An admin account cannot log in to `kreds`, and that is intended.
- The `kreds` realm has **no users in Git**. Create local test users by hand: realm `kreds` → *Users* → *Add user*,
  then set a password on the *Credentials* tab (turn *Temporary* off).
- Client `kreds-web`: a public client (no secret – it runs in the browser) using Authorization Code with
  PKCE (Proof Key for Code Exchange, `S256`). Allowed redirect: `http://localhost:5173/*` (Vite dev server).
- Self-registration is off (invite-only), and so is "forgot password" until email is set up.

Check that the realm is up:

    curl -s http://localhost:8180/realms/kreds/.well-known/openid-configuration