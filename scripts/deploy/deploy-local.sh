#!/usr/bin/env bash
set -euo pipefail

GREEN="\e[32m"
RED="\e[31m"
YELLOW="\e[33m"
NC="\e[0m"

log() {
    echo -e "${GREEN}[DEPLOY]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# -------------------------------
# Ubicar el root del repo
# -------------------------------
REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

MASIVA_DIR="$REPO_ROOT/QAPI_masiva"
WORKER_DIR="$REPO_ROOT/QAPI_worker"

MASIVA_JAR=$(ls "$MASIVA_DIR"/target/QAPI_FacturacionMasiva-*.jar 2>/dev/null || true)
WORKER_JAR=$(ls "$WORKER_DIR"/target/QAPI_FacturacionMasivaWorker-*.jar 2>/dev/null || true)

log "Validando existencia de los JARs…"

[[ -f "$MASIVA_JAR" ]] || error "No se encontró el JAR de QAPI_masiva. Ejecuta primero: make build"
[[ -f "$WORKER_JAR" ]] || error "No se encontró el JAR de QAPI_worker. Ejecuta primero: make build"

log "JARs encontrados correctamente."
echo ""
echo "  QAPI_masiva jar: $MASIVA_JAR"
echo "  QAPI_worker jar: $WORKER_JAR"
echo ""

# -------------------------------
#  Detener procesos previos
# -------------------------------
log "Deteniendo procesos Java previos…"

pkill -f "QAPI_FacturacionMasiva" || true
pkill -f "QAPI_FacturacionMasivaWorker" || true

sleep 1

# -------------------------------
# Ejecutar QAPI_masiva
# -------------------------------
log "Iniciando QAPI_masiva en :8080…"

nohup java -jar "$MASIVA_JAR" \
    --spring.profiles.active=dev \
    > "$MASIVA_DIR/app.log" 2>&1 &

sleep 3

# -------------------------------
# Ejecutar QAPI_worker
# -------------------------------
log "Iniciando QAPI_worker en :8081…"

nohup java -jar "$WORKER_JAR" \
    --server.port=8081 \
    --spring.profiles.active=dev \
    > "$WORKER_DIR/app.log" 2>&1 &

sleep 3

log "Servicios iniciados:"
echo " - QAPI_masiva   -> http://localhost:8080"
echo " - QAPI_worker   -> http://localhost:8081"