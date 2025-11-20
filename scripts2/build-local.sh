#!/bin/bash
# 3. mvn clean package
set -e

# Descubrir BASE_DIR relativo a este script
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Build local: QAPI_masiva + QAPI_worker                   ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${YELLOW}→ Compilando QAPI_masiva...${NC}"
cd "$BASE_DIR/QAPI_masiva"
mvn clean package -DskipTests -q

if [ -f "target/QAPI_FacturacionMasiva-1.1.1.jar" ]; then
    echo -e "${GREEN}✓ QAPI_masiva compilado (target/QAPI_FacturacionMasiva-1.1.1.jar)${NC}"
else
    echo -e "${RED}✗ Error compilando QAPI_masiva (no se encontró el jar)${NC}"
    exit 1
fi

echo -e "${YELLOW}→ Compilando QAPI_worker...${NC}"
cd "$BASE_DIR/QAPI_worker"
mvn clean package -DskipTests -q

if [ -f "target/QAPI_FacturacionMasivaWorker-1.1.0.jar" ]; then
    echo -e "${GREEN}✓ QAPI_worker compilado (target/QAPI_FacturacionMasivaWorker-1.1.0.jar)${NC}"
else
    echo -e "${RED}✗ Error compilando QAPI_worker (no se encontró el jar)${NC}"
    exit 1
fi

cd "$BASE_DIR"

echo ""
echo -e "${GREEN}✓ Build local completado exitosamente${NC}"
