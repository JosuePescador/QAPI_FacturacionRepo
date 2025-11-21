#!/usr/bin/env bash
set -euo pipefail

GREEN="\e[32m"
YELLOW="\e[33m"
RED="\e[31m"
NC="\e[0m"

log() {
    echo -e "${GREEN}[BUILD]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

# Descubrir raíz del repo
REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

QAPI_MASIVA_DIR="$REPO_ROOT/QAPI_masiva"
QAPI_WORKER_DIR="$REPO_ROOT/QAPI_worker"

log "Iniciando build de los proyectos Java…"

# -----------------------------------------
# 1. Validar directorios
# -----------------------------------------
[[ -d "$QAPI_MASIVA_DIR" ]] || error "No se encontró la carpeta QAPI_masiva"
[[ -d "$QAPI_WORKER_DIR" ]] || error "No se encontró la carpeta QAPI_worker"

# -----------------------------------------
# 2. Build QAPI_masiva
# -----------------------------------------
log "Compilando QAPI_masiva…"
mvn -f "$QAPI_MASIVA_DIR/pom.xml" clean package -DskipTests

# -----------------------------------------
# 3. Build QAPI_worker
# -----------------------------------------
log "Compilando QAPI_worker…"
mvn -f "$QAPI_WORKER_DIR/pom.xml" clean package -DskipTests

# -----------------------------------------
# 4. Final
# -----------------------------------------
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   ✓ Build completado sin errores       ${NC}"
echo -e "${GREEN}========================================${NC}"
