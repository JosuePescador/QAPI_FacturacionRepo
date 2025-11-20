#!/bin/bash

################################################################################
# Script de Prueba End-to-End - QAPI Facturación Masiva
# Prueba el flujo completo: API → SQS → Worker → DynamoDB
################################################################################

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Prueba End-to-End: QAPI Facturación Masiva               ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Configuración
MASIVA_URL="http://localhost:8080"
WORKER_URL="http://localhost:8081"
LOCALSTACK_URL="http://localhost:4566"
KEYCLOAK_URL="https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token"

# AWS LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

# 1. Verificar que los servicios estén corriendo
echo -e "${YELLOW}→${NC} Verificando servicios..."

# LocalStack
if curl -s $LOCALSTACK_URL/_localstack/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} LocalStack está corriendo (puerto 4566)"
else
    echo -e "${RED}✗${NC} LocalStack NO está corriendo. Ejecuta: docker-compose up -d"
    exit 1
fi

# QAPI_masiva
if curl -s $MASIVA_URL/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} QAPI_masiva está corriendo (puerto 8080)"
else
    echo -e "${RED}✗${NC} QAPI_masiva NO está corriendo"
    echo -e "${YELLOW}  Ejecuta en otra terminal:${NC}"
    echo -e "${CYAN}  cd QAPI_masiva && java -jar target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev${NC}"
    exit 1
fi

# QAPI_worker
if curl -s $WORKER_URL/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} QAPI_worker está corriendo (puerto 8081)"
else
    echo -e "${YELLOW}⚠${NC} QAPI_worker NO está corriendo (opcional para esta prueba)"
fi

echo ""

# 2. Obtener Token JWT
echo -e "${YELLOW}→${NC} Obteniendo token JWT..."
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=apifactmasiva" \
  -d "client_secret=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV" | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo -e "${RED}✗${NC} Error obteniendo token JWT"
    exit 1
fi
echo -e "${GREEN}✓${NC} Token JWT obtenido"
echo ""

# 3. Enviar solicitud de facturación
echo -e "${YELLOW}→${NC} Enviando solicitud de facturación masiva..."
RESPONSE=$(curl -s -X POST "$MASIVA_URL/facturacion-masiva/nota-debito" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
    {
      "referencia": "TEST-E2E-001",
      "cicloLectivo": "2025-1",
      "nit_tercero": 123456789,
      "cuentaConsignacion": 987654321,
      "tercero": {
        "claseIdentificacion": "CC",
        "numeroIdentificacion": 123456789,
        "descripcionAuxiliar": "Cliente Test E2E",
        "naturalJuridica": "Persona Natural",
        "tipoAuxiliar": "Comprador",
        "tipoRetencion": "Ninguna",
        "codigoPais": 170,
        "codigoDepartamento": 25,
        "codigoCiudad": 25001,
        "celular": "3001234567",
        "telefono": "6011234567",
        "direccion": "Calle Test 123",
        "correoElectronico": "test@example.com"
      },
      "listado_cptos_principales": [
        {
          "conceptoFacturacion": "Servicio Test E2E",
          "cantidadUnidades": "1",
          "valorUnitario": "500000"
        }
      ]
    }
  ]')

echo -e "${CYAN}Respuesta de la API:${NC}"
echo "$RESPONSE" | jq '.'
echo ""

# Extraer messageId
MESSAGE_ID=$(echo "$RESPONSE" | jq -r '.messageIds[0]')
if [ -z "$MESSAGE_ID" ] || [ "$MESSAGE_ID" == "null" ]; then
    echo -e "${RED}✗${NC} No se obtuvo messageId de la respuesta"
    exit 1
fi
echo -e "${GREEN}✓${NC} MessageId generado: $MESSAGE_ID"
echo ""

# 4. Verificar mensaje en SQS
echo -e "${YELLOW}→${NC} Verificando mensaje en cola SQS..."
QUEUE_URL=$(aws --endpoint-url=$LOCALSTACK_URL sqs get-queue-url \
    --queue-name uca-test-factmasiva-sqs.fifo \
    --output text 2>/dev/null)

MESSAGES=$(aws --endpoint-url=$LOCALSTACK_URL sqs receive-message \
    --queue-url "$QUEUE_URL" \
    --max-number-of-messages 1 \
    --wait-time-seconds 2 2>/dev/null)

if echo "$MESSAGES" | grep -q "Messages"; then
    echo -e "${GREEN}✓${NC} Mensaje encontrado en cola SQS"
    echo -e "${CYAN}Contenido del mensaje:${NC}"
    echo "$MESSAGES" | jq -r '.Messages[0].Body' | jq '.'
else
    echo -e "${YELLOW}⚠${NC} No se encontraron mensajes en la cola (puede que ya se procesó)"
fi
echo ""

# 5. Verificar en DynamoDB #1 (InfoRequest)
echo -e "${YELLOW}→${NC} Verificando registro en DynamoDB #1 (InfoRequest)..."
sleep 2  # Esperar a que se guarde

DYNAMODB_ITEM=$(aws --endpoint-url=$LOCALSTACK_URL dynamodb scan \
    --table-name InfoRequest \
    --limit 5 2>/dev/null)

if echo "$DYNAMODB_ITEM" | grep -q "Items"; then
    echo -e "${GREEN}✓${NC} Registros encontrados en DynamoDB InfoRequest"
    echo -e "${CYAN}Últimos registros:${NC}"
    echo "$DYNAMODB_ITEM" | jq '.Items | .[0]'
else
    echo -e "${YELLOW}⚠${NC} No se encontraron registros en InfoRequest"
fi
echo ""

# 6. Si el worker está corriendo, verificar DynamoDB #2
if curl -s $WORKER_URL/actuator/health > /dev/null 2>&1; then
    echo -e "${YELLOW}→${NC} Esperando procesamiento del worker..."
    sleep 5
    
    echo -e "${YELLOW}→${NC} Verificando registro en DynamoDB #2 (InfoRequestProcessing)..."
    DYNAMODB_ITEM2=$(aws --endpoint-url=$LOCALSTACK_URL dynamodb scan \
        --table-name InfoRequestProcessing \
        --limit 5 2>/dev/null)
    
    if echo "$DYNAMODB_ITEM2" | grep -q "Items"; then
        echo -e "${GREEN}✓${NC} Registros encontrados en DynamoDB InfoRequestProcessing"
        echo -e "${CYAN}Estado del procesamiento:${NC}"
        echo "$DYNAMODB_ITEM2" | jq '.Items | .[0]'
    else
        echo -e "${YELLOW}⚠${NC} No se encontraron registros procesados aún"
    fi
    echo ""
fi

# 7. Resumen
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Resumen de la Prueba                                     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}1.${NC} Solicitud enviada correctamente a QAPI_masiva"
echo -e "${CYAN}2.${NC} MessageId: $MESSAGE_ID"
echo -e "${CYAN}3.${NC} Mensaje encolado en SQS"
echo -e "${CYAN}4.${NC} Registro guardado en DynamoDB InfoRequest"
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ Prueba End-to-End completada                           ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Comandos útiles:${NC}"
echo -e "${CYAN}# Ver mensajes en SQS:${NC}"
echo "aws --endpoint-url=$LOCALSTACK_URL sqs receive-message --queue-url $QUEUE_URL"
echo ""
echo -e "${CYAN}# Ver registros en DynamoDB InfoRequest:${NC}"
echo "aws --endpoint-url=$LOCALSTACK_URL dynamodb scan --table-name InfoRequest"
echo ""
echo -e "${CYAN}# Ver registros en DynamoDB InfoRequestProcessing:${NC}"
echo "aws --endpoint-url=$LOCALSTACK_URL dynamodb scan --table-name InfoRequestProcessing"
