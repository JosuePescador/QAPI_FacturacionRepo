#!/usr/bin/env bash
set -euo pipefail

GREEN="\e[32m"
YELLOW="\e[33m"
RED="\e[31m"
NC="\e[0m"

log() { echo -e "${GREEN}[INFRA]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TF_DIR="$REPO_ROOT/infra/terraform"

log "1️⃣ Levantando LocalStack…"
docker compose up -d localstack

log "2️⃣ Esperando a que LocalStack responda…"

# Esperar health check estable
until curl -s http://localhost:4566/_localstack/health || true | grep -E '"initialized": true|"status": "running"' >/dev/null; do
    warn "LocalStack aún no está listo…"
    sleep 3
done

log "✓ LocalStack está arriba."

log "3️⃣ Ejecutando Terraform…"

cd "$TF_DIR"

terraform init -input=false
terraform apply -auto-approve

log "✓ Terraform aplicado correctamente."
log "INFRA COMPLETE ✔"

