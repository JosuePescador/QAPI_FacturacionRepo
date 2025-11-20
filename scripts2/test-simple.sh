#!/bin/bash

################################################################################
# Script de Prueba Simple - Con logs visibles
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
echo -e "${BLUE}║  Prueba Simple de Facturación Masiva                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Obtener token
echo -e "${YELLOW}→${NC} Obteniendo token JWT..."
TOKEN=$(curl -s -X POST \
  "https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=apifactmasiva" \
  -d "client_secret=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV" | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo -e "${RED}✗${NC} Error obteniendo token"
    exit 1
fi
echo -e "${GREEN}✓${NC} Token obtenido"
echo ""

# Enviar solicitud
echo -e "${YELLOW}→${NC} Enviando solicitud..."
echo -e "${CYAN}URL:${NC} http://localhost:8080/facturacion-masiva/nota-debito"
echo ""

RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST \
  "http://localhost:8080/facturacion-masiva/nota-debito" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
    {
      "referencia": "SIMPLE-TEST-001",
      "cicloLectivo": "2025-1",
      "nit_tercero": 999888777,
      "cuentaConsignacion": 111222333,
      "tercero": {
        "claseIdentificacion": "CC",
        "numeroIdentificacion": 999888777,
        "descripcionAuxiliar": "Tes",
        "naturalJuridica": "Persona Natural",
        "tipoAuxiliar": "Comprador",
        "tipoRetencion": "Ninguna",
        "codigoPais": 170,
        "codigoDepartamento": 25,
        "codigoCiudad": 25001,
        "celular": "3001111111",
        "telefono": "6011111111",
        "direccion": "Test 456",
        "correoElectronico": "simple@test.com"
      },
      "listado_cptos_principales": [
        {
          "conceptoFacturacion": "MATRICULA",
          "cantidadUnidades": "1",
          "valorUnitario": "100000"
        }
      ]
    }
  ]')

# Extraer código HTTP
HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE/d')

echo -e "${CYAN}Código HTTP:${NC} $HTTP_CODE"
echo -e "${CYAN}Respuesta:${NC}"
echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
echo ""

if [ "$HTTP_CODE" == "202" ]; then
    echo -e "${GREEN}✓${NC} Solicitud aceptada (202)"
    
    MESSAGE_ID=$(echo "$BODY" | jq -r '.messageIds[0]' 2>/dev/null)
    echo -e "${CYAN}MessageId:${NC} $MESSAGE_ID"
    echo ""
    
    # Verificar en SQS
    echo -e "${YELLOW}→${NC} Verificando en SQS..."
    sleep 2
    
    QUEUE_URL=$(aws --endpoint-url=http://localhost:4566 sqs get-queue-url \
        --queue-name uca-test-factmasiva-sqs.fifo \
        --output text 2>/dev/null)
    
    MESSAGES=$(aws --endpoint-url=http://localhost:4566 sqs receive-message \
        --queue-url "$QUEUE_URL" \
        --max-number-of-messages 1 2>/dev/null)
    
    if echo "$MESSAGES" | grep -q "Messages"; then
        echo -e "${GREEN}✓${NC} Mensaje encontrado en SQS"
        echo "$MESSAGES" | jq -r '.Messages[0].Body' | jq '.' 2>/dev/null | head -20
    else
        echo -e "${RED}✗${NC} No hay mensajes en SQS"
        echo -e "${YELLOW}Revisa los logs de QAPI_masiva${NC}"
    fi
else
    echo -e "${RED}✗${NC} Error: HTTP $HTTP_CODE"
    echo -e "${YELLOW}Revisa los logs de QAPI_masiva para ver el error${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}Comandos útiles:${NC}"
echo -e "${CYAN}# Ver mensajes en SQS:${NC}"
echo "aws --endpoint-url=http://localhost:4566 sqs receive-message --queue-url $QUEUE_URL"
echo ""
echo -e "${CYAN}# Ver logs de QAPI_masiva (en otra terminal):${NC}"
echo "tail -f /ruta/a/logs o ver la terminal donde corre"

---

## 6. Notas técnicas

* El build usa **Java 17** dentroS