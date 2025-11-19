#!/bin/bash
## 5. Wrapper de tests locales (espera servicios + E2E)
set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

MASIVA_URL="http://localhost:8080"
WORKER_URL="http://localhost:8081"

wait_for_service() {
  local name="$1"
  local url="$2"
  local attempts=20

  echo -e "${YELLOW}→ Esperando a que ${name} responda en ${url}...${NC}"
  for i in $(seq 1 $attempts); do
    if curl -s "${url}" >/dev/null 2>&1; then
      echo -e "${GREEN}✓ ${name} está arriba${NC}"
      return 0
    fi
    echo "  ...reintentando (${i}/${attempts})"
    sleep 2
  done

  echo -e "${RED}✗ Timeout esperando a ${name}${NC}"
  return 1
}

# 1. Esperar QAPI_masiva
wait_for_service "QAPI_masiva" "${MASIVA_URL}/actuator/health"

# 2. Esperar QAPI_worker (opcional, si falla igual seguimos)
if ! wait_for_service "QAPI_worker" "${WORKER_URL}/actuator/health"; then
  echo -e "${YELLOW}⚠ QAPI_worker no respondió a tiempo, se continúa igual para la prueba E2E.${NC}"
fi

# 3. Ejecutar el test E2E real (ahora con la ruta correcta)
echo -e "${YELLOW}→ Ejecutando test end-to-end completo...${NC}"
"$BASE_DIR/scripts/test/test-e2e.sh"
