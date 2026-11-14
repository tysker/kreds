#!/usr/bin/env bash
# Runs ONCE, when the Postgres data volume is still empty (official image: /docker-entrypoint-initdb.d).
# Creates one database and one login role per application, so neither can read the other's data.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -v kreds_password="$KREDS_DB_PASSWORD" \
    -v keycloak_password="$KEYCLOAK_DB_PASSWORD" <<'SQL'
-- Kreds (Spring Boot + Flyway)
CREATE ROLE kreds LOGIN PASSWORD :'kreds_password';
CREATE DATABASE kreds OWNER kreds;
REVOKE ALL ON DATABASE kreds FROM PUBLIC;

-- Keycloak (manages its own schema)
CREATE ROLE keycloak LOGIN PASSWORD :'keycloak_password';
CREATE DATABASE keycloak OWNER keycloak;
REVOKE ALL ON DATABASE keycloak FROM PUBLIC;
SQL

