#!/bin/bash
# 4. Levantar QAPI_masiva + QAPI_worker
set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

YELLOW='\033[1;33m'
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Deploy local: QAPI_masiva + QAPI_worker                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

JAR_MASIVA="$BASE_DIR/QAPI_masiva/target/QAPI_FacturacionMasiva-1.1.1.jar"
JAR_WORKER="$BASE_DIR/QAPI_worker/target/QAPI_FacturacionMasivaWorker-1.1.0.jar"

if [ ! -f "$JAR_MASIVA" ] || [ ! -f "$JAR_WORKER" ]; then
  echo -e "${RED}✗ No se encontraron los jars. Ejecuta primero:${NC}"
  echo -e "${YELLOW}  ./scripts/build-local.sh${NC}"
  exit 1
fi

echo -e "${YELLOW}→ Levantando QAPI_masiva en segundo plano (puerto 8080)...${NC}"
cd "$BASE_DIR/QAPI_masiva"
nohup java -jar "$JAR_MASIVA" --spring.profiles.active=dev > "$BASE_DIR/log-masiva.out" 2>&1 &

echo -e "${YELLOW}→ Levantando QAPI_worker en segundo plano (puerto 8081)...${NC}"
cd "$BASE_DIR/QAPI_worker"
nohup java -jar "$JAR_WORKER" --spring.profiles.active=dev > "$BASE_DIR/log-worker.out" 2>&1 &

echo ""
echo -e "${GREEN}✓ Deploy local completado${NC}"
echo ""
echo -e "${YELLOW}Logs:${NC}"
echo -e "  QAPI_masiva: $BASE_DIR/log-masiva.out"
echo -e "  QAPI_worker: $BASE_DIR/log-worker.out"
echo ""
echo -e "${YELLOW}Para detenerlos:${NC}"
echo '  ps aux | grep QAPI_FacturacionMasiva | grep -v grep | awk "{print \$2}" | xargs kill'
echo '  ps aux | grep QAPI_FacturacionMasivaWorker | grep -v grep | awk "{print \$2}" | xargs kill'
