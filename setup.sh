#!/bin/bash

################################################################################
# Script Maestro - Configuración y Ejecución Completa
# Ejecuta todo el sistema QAPI en orden
################################################################################

set -euo pipefail

# ==== Colores ====
RED="\e[31m"
GREEN="\e[32m"
YELLOW="\e[33m"
BLUE="\e[34m"
MAGENTA="\e[35m"
NC="\e[0m" # No Color

# ==== Directorio base (raíz del repo) ====
# Calculado en función de la ruta del propio script
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$BASE_DIR"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 1: Iniciando LocalStack                              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"

echo -e "${YELLOW}→${NC} Deteniendo contenedores anteriores..."
docker compose down >/dev/null 2>&1 || true

echo -e "${YELLOW}→${NC} Iniciando LocalStack..."
docker compose up -d

# ==== Esperar a que LocalStack esté listo ====
echo -e "${YELLOW}→${NC} Esperando a que LocalStack esté listo..."

HEALTH_URL="http://localhost:4566/_localstack/health"
MAX_RETRIES=30
SLEEP_SECONDS=2
retry=0

while ! curl -s "$HEALTH_URL" >/dev/null 2>&1; do
    retry=$((retry + 1))
    if [ "$retry" -ge "$MAX_RETRIES" ]; then
        echo -e "${RED}✗${NC} LocalStack no respondió después de $((MAX_RETRIES * SLEEP_SECONDS))s."
        echo -e "   Revisa los logs con: ${YELLOW}docker logs localstack-qapi${NC}"
        exit 1
    fi
    sleep "$SLEEP_SECONDS"
done

echo -e "${GREEN}✓${NC} LocalStack está corriendo en puerto 4566"

# 3. Inicializar recursos
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 3: Creando recursos de AWS                           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"

if [ -x "./init-localstack.sh" ]; then
  ./init-localstack.sh
else
  echo -e "${YELLOW}⚠ init-localstack.sh no encontrado o sin permiso de ejecución.${NC}"
  echo -e "  Si tienes un script para crear colas/tablas/buckets, revisa su ruta."
fi

# 4. Compilar proyectos
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 4: Compilando proyectos                              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"

echo -e "${YELLOW}→${NC} Compilando QAPI_masiva..."
cd "$BASE_DIR/QAPI_masiva"
mvn clean package -DskipTests -q
if [ -f "target/QAPI_FacturacionMasiva-1.1.1.jar" ]; then
    echo -e "${GREEN}✓${NC} QAPI_masiva compilado"
else
    echo -e "${RED}✗${NC} Error compilando QAPI_masiva (jar no encontrado)"
    exit 1
fi

echo -e "${YELLOW}→${NC} Compilando QAPI_worker..."
cd "$BASE_DIR/QAPI_worker"
mvn clean package -DskipTests -q
if [ -f "target/QAPI_FacturacionMasivaWorker-1.1.0.jar" ]; then
    echo -e "${GREEN}✓${NC} QAPI_worker compilado"
else
    echo -e "${RED}✗${NC} Error compilando QAPI_worker (jar no encontrado)"
    exit 1
fi

cd "$BASE_DIR"

# 5. Instrucciones finales
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ Setup completado exitosamente                           ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${MAGENTA}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${MAGENTA}║  Próximos pasos:                                           ║${NC}"
echo -e "${MAGENTA}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}TERMINAL 1:${NC} Ejecutar QAPI_masiva"
echo -e "${CYAN:-}cd $BASE_DIR/QAPI_masiva"
echo -e "${CYAN:-}java -jar target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev"
echo ""
echo -e "${YELLOW}TERMINAL 2:${NC} Ejecutar QAPI_worker"
echo -e "${CYAN:-}cd $BASE_DIR/QAPI_worker"
echo -e "${CYAN:-}java -jar target/QAPI_FacturacionMasivaWorker-1.1.0.jar --spring.profiles.active=dev"
echo ""
echo -e "${YELLOW}TERMINAL 3:${NC} Ejecutar prueba end-to-end"
echo -e "${CYAN:-}cd $BASE_DIR"
echo -e "${CYAN:-}./test-e2e.sh"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Servicios disponibles:${NC}"
echo -e "  ${GREEN}•${NC} LocalStack:    http://localhost:4566"
echo -e "  ${GREEN}•${NC} QAPI_masiva:   http://localhost:8080"
echo -e "  ${GREEN}•${NC} QAPI_worker:   http://localhost:8081"
echo -e "  ${GREEN}•${NC} Swagger:       http://localhost:8080/docs"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
