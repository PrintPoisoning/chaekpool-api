#!/bin/sh
set -euo pipefail

# --- Configuration ---
HOST="10.1.0.140"
USER="root"
JAR_PATH="/opt/api/api.jar"

SSH="ssh -o StrictHostKeyChecking=no ${USER}@${HOST}"
SCP="scp -o StrictHostKeyChecking=no"

# --- Find JAR ---
JAR_FILE=$(ls build/libs/chaekpool-*.jar | grep -v plain | head -1)
echo "[DEPLOY] Found JAR: ${JAR_FILE}"

# --- Upload JAR ---
echo "[DEPLOY] Uploading JAR to ${HOST}..."
$SCP "$JAR_FILE" "${USER}@${HOST}:${JAR_PATH}.new"

# --- Deploy env file ---
echo "[DEPLOY] Deploying environment file..."
$SCP "$API_ENV_FILE" "${USER}@${HOST}:/etc/api/.env"
$SSH "chown api:api /etc/api/.env && chmod 0600 /etc/api/.env"

# --- Deploy (atomic swap + health check + rollback) ---
echo "[DEPLOY] Deploying..."
$SSH <<'REMOTE_SCRIPT'
set -eu

JAR="/opt/api/api.jar"

# Stop
rc-service api stop 2>/dev/null || true

# Backup + Swap
[ -f "$JAR" ] && mv "$JAR" "${JAR}.bak"
mv "${JAR}.new" "$JAR"
chown api:api "$JAR"

# Start
rc-service api start

# Health check
for i in $(seq 1 30); do
    if wget -q -O /dev/null http://localhost:8080/actuator/health 2>/dev/null; then
        echo "[DEPLOY] Health check passed (${i}x2s)"
        exit 0
    fi
    sleep 2
done

# Rollback
echo "[DEPLOY] Health check failed, rolling back..."
rc-service api stop 2>/dev/null || true
if [ -f "${JAR}.bak" ]; then
    mv "${JAR}.bak" "$JAR"
    chown api:api "$JAR"
    rc-service api start
    echo "[DEPLOY] Rolled back to previous version"
fi
exit 1
REMOTE_SCRIPT

echo "[DEPLOY] Done"
