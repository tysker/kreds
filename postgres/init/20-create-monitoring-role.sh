#!/usr/bin/env bash
# Creates the read-only monitoring role for postgres_exporter (ADR-0003).
# Runs automatically on the first start; safe to run again by hand (idempotent).
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -v monitoring_password="$MONITORING_DB_PASSWORD" <<'SQL'
-- Create the role only if it does not exist yet
SELECT format('CREATE ROLE monitoring LOGIN PASSWORD %L', :'monitoring_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'monitoring') \gexec

-- pg_monitor: read statistics and settings – no table data.
-- CONNECT to the linden and keycloak databases stays revoked (10-create-databases.sh).
GRANT pg_monitor TO monitoring;
SQL