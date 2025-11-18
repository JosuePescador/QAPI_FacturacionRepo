#!/bin/bash
## 1. Docker + LocalStack
set -euo pipefail

# Descubrir BASE_DIR relativo a este script
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Colores básicos
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}▶ Iniciando infraestructura local (Docker + LocalStack)...${NC}"

cd "$BASE_DIR"

echo -e "${YELLOW}→ Deteniendo contenedores anteriores de LocalStack (si los hay)...${NC}"
docker compose down >/dev/null 2>&1 || true

echo -e "${YELLOW}→ Iniciando LocalStack...${NC}"
docker compose up -d

echo -e "${YELLOW}→ Esperando a que LocalStack responda en 4566...${NC}"
for i in {1..15}; do
  if curl -s http://localhost:4566/_localstack/health >/dev/null 2>&1; then
    echo -e "${GREEN}✓ LocalStack está corriendo en http://localhost:4566${NC}"
    exit 0
  fi
  echo "  ...reintentando ($i/15)"
  sleep 2
done

echo -e "${RED}✗ Timeout esperando a LocalStack${NC}"
exit 1
