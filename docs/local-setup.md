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