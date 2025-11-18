#!/bin/bash

set -euo pipefail
###############################################################################
# Setup completo entorno local QAPI Facturación Masiva
# 1) Infra (Docker + LocalStack)
# 2) IaC local (Terraform sobre LocalStack)
# 3) Build (QAPI_masiva + QAPI_worker)
# 4) Deploy local (levantar los JARs)
# 5) Tests end-to-end
###############################################################################

# Descubrir BASE_DIR (root del repo)
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'
MAGENTA='\033[0;35m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Setup local: QAPI Facturación Masiva                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

###############################################################################
# Paso 1: Infraestructura local (Docker + LocalStack)
###############################################################################
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 1: Infraestructura local (Docker + LocalStack)       ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

"$BASE_DIR/scripts/infra-local.sh"
echo ""

###############################################################################
# Paso 2: IaC local (Terraform sobre LocalStack)
###############################################################################
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 2: IaC local (Terraform sobre LocalStack)            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

"$BASE_DIR/scripts/iac-local.sh"
echo ""

###############################################################################
# Paso 3: Build local (QAPI_masiva + QAPI_worker)
###############################################################################
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 3: Build local (QAPI_masiva + QAPI_worker)           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

"$BASE_DIR/scripts/build-local.sh"
echo ""

###############################################################################
# Paso 4: Deploy local (levantar jars en background)
###############################################################################
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 4: Deploy local (lanzar servicios)                   ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

"$BASE_DIR/scripts/deploy-local.sh"
echo ""

###############################################################################
# Paso 5: Tests end-to-end
###############################################################################
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 5: Tests end-to-end                                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

"$BASE_DIR/scripts/test/test-local.sh"
echo ""

echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ Setup completo (infra + IaC + build + deploy + tests)   ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

###############################################################################
# Resumen final
###############################################################################
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ Setup completado exitosamente                          ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${MAGENTA}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${MAGENTA}║  Servicios y pruebas locales                               ║${NC}"
echo -e "${MAGENTA}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}• LocalStack:${NC}    http://localhost:4566"
echo -e "${CYAN}• QAPI_masiva:${NC}   http://localhost:8080"
echo -e "${CYAN}• QAPI_worker:${NC}   http://localhost:8081"
echo -e "${CYAN}• Swagger:${NC}       http://localhost:8080/docs"
echo ""
echo -e "${YELLOW}Si necesitas re-ejecutar solo una parte:${NC}"
echo -e "  ${CYAN}- Infraestructura:${NC}   ./scripts/infra-local.sh"
echo -e "  ${CYAN}- IaC:${NC}               ./scripts/iac-local.sh"
echo -e "  ${CYAN}- Build:${NC}             ./scripts/build-local.sh"
echo -e "  ${CYAN}- Deploy:${NC}            ./scripts/deploy-local.sh"
echo -e "  ${CYAN}- Tests E2E:${NC}         ./scripts/test/test-e2e.sh"
echo ""
