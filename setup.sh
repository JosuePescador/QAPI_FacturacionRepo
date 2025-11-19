#!/bin/bash

#ejecucion completa 


# Directorio base
BASE_DIR="/home/joseu_pescado/Descargas/QAPI_FacturacionRepo"
cd "$BASE_DIR"


# 1. Iniciar LocalStack
echo -e  "Iniciando LocalStack"

echo -e " Deteniendo contenedores anteriores..."
docker-compose down 2>/dev/null || true

echo -e "Iniciando LocalStack..."
docker-compose up -d

echo -e "${YELLOW}→${NC} Esperando a que LocalStack esté listo..."
sleep 5

if curl -s http://localhost:4566/_localstack/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} LocalStack está corriendo en puerto 4566"
else
    echo -e "${RED}✗${NC} LocalStack no respondió. Revisa con: docker logs localstack-qapi"
    exit 1
fi

# 3. Inicializar recursos
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 3: Creando recursos de AWS                          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"

./init-localstack.sh

# 4. Compilar proyectos
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Paso 4: Compilando proyectos                             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"

echo -e "${YELLOW}→${NC} Compilando QAPI_masiva..."
cd "$BASE_DIR/QAPI_masiva"
mvn clean package -DskipTests -q
if [ -f "target/QAPI_FacturacionMasiva-1.1.1.jar" ]; then
    echo -e "${GREEN}✓${NC} QAPI_masiva compilado"
else
    echo -e "${RED}✗${NC} Error compilando QAPI_masiva"
    exit 1
fi

echo -e "${YELLOW}→${NC} Compilando QAPI_worker..."
cd "$BASE_DIR/QAPI_worker"
mvn clean package -DskipTests -q
if [ -f "target/QAPI_FacturacionMasivaWorker-1.1.0.jar" ]; then
    echo -e "${GREEN}✓${NC} QAPI_worker compilado"
else
    echo -e "${RED}✗${NC} Error compilando QAPI_worker"
    exit 1
fi

cd "$BASE_DIR"

# 5. Instrucciones finales
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ Setup completado exitosamente                          ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${MAGENTA}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${MAGENTA}║  Próximos pasos:                                          ║${NC}"
echo -e "${MAGENTA}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}TERMINAL 1:${NC} Ejecutar QAPI_masiva"
echo -e "${CYAN}cd $BASE_DIR/QAPI_masiva${NC}"
echo -e "${CYAN}java -jar target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev${NC}"
echo ""
echo -e "${YELLOW}TERMINAL 2:${NC} Ejecutar QAPI_worker"
echo -e "${CYAN}cd $BASE_DIR/QAPI_worker${NC}"
echo -e "${CYAN}java -jar target/QAPI_FacturacionMasivaWorker-1.1.0.jar --spring.profiles.active=dev${NC}"
echo ""
echo -e "${YELLOW}TERMINAL 3:${NC} Ejecutar prueba end-to-end"
echo -e "${CYAN}cd $BASE_DIR${NC}"
echo -e "${CYAN}./test-e2e.sh${NC}"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Servicios disponibles:${NC}"
echo -e "  ${GREEN}•${NC} LocalStack:    http://localhost:4566"
echo -e "  ${GREEN}•${NC} QAPI_masiva:   http://localhost:8080"
echo -e "  ${GREEN}•${NC} QAPI_worker:   http://localhost:8081"
echo -e "  ${GREEN}•${NC} Swagger:       http://localhost:8080/docs"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
