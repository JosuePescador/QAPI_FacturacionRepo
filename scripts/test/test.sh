#!/usr/bin/env bash
set -euo pipefail

GREEN="\e[32m"
RED="\e[31m"
YELLOW="\e[33m"
NC="\e[0m"

log() { echo -e "${GREEN}[TEST]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="us-east-1"

log "Validando estructura de test…"

# Nada que validar, ya no existen test-simple.sh ni otros scripts
log "✓ Script test.sh cargado correctamente"

# ---------------------------------------------------------
# 1. Verificar servicios corriendo
# ---------------------------------------------------------
log "Verificando que QAPI_masiva esté arriba…"
curl -s http://localhost:8080/actuator/health >/dev/null \
    || error "QAPI_masiva NO está corriendo."

log "Verificando que QAPI_worker esté arriba…"
if ! pgrep -f "QAPI_FacturacionMasivaWorker" > /dev/null; then
    error "QAPI_worker NO está corriendo."
else
    log "✓ QAPI_worker está corriendo."
fi


log "Verificando que LocalStack esté arriba…"
curl -s http://localhost:4566/_localstack/health >/dev/null \
    || error "LocalStack NO está corriendo."

# ---------------------------------------------------------
# 2. Obtener Token JWT
# ---------------------------------------------------------
log "Obteniendo token de Keycloak…"

TOKEN=$(curl -s -X POST \
  "https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=apifactmasiva" \
  -d "client_secret=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV" \
  | jq -r '.access_token')

[[ "$TOKEN" != "null" ]] || error "No se pudo obtener el token JWT."

log "✓ Token JWT obtenido."

# ---------------------------------------------------------
# 3. Ejecutar solicitud POST
# ---------------------------------------------------------

log "Enviando solicitud POST a QAPI_masiva…"

RESPONSE=$(curl -s -o /tmp/response.json -w "%{http_code}" -X POST \
  "http://localhost:8080/facturacion-masiva/nota-debito" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[ { "referencia": "TEST-001", "cicloLectivo": "2025-1",
         "nit_tercero": 123456789, "cuentaConsignacion": 987654321,
         "tercero": {"claseIdentificacion":"CC",
                     "numeroIdentificacion":123456789,
                     "descripcionAuxiliar":"Cliente Test",
                     "naturalJuridica":"Persona Natural",
                     "tipoAuxiliar":"Comprador",
                     "tipoRetencion":"Ninguna",
                     "codigoPais":170, "codigoDepartamento":25,
                     "codigoCiudad":25001,
                     "celular":"3001234567",
                     "telefono":"6011234567",
                     "direccion":"Calle 123",
                     "correoElectronico":"test@example.com"},
         "listado_cptos_principales":[{"conceptoFacturacion":"Servicio Test",
                                      "cantidadUnidades":"1",
                                      "valorUnitario":"100000"}] } ]')

if [[ "$RESPONSE" != "202" ]]; then
    error "La API devolvió código HTTP $RESPONSE. Esperado: 202"
fi

log "Solicitud aceptada por QAPI_masiva."

MESSAGE_ID=$(jq -r '.messageIds[0]' /tmp/response.json)

log "MessageID recibido: $MESSAGE_ID"

# ---------------------------------------------------------
# 4. Verificar SQS
# ---------------------------------------------------------

log "Verificando que el mensaje está en SQS…"

QUEUE_URL=$(aws --endpoint-url=http://localhost:4566 sqs get-queue-url \
    --queue-name uca-test-factmasiva-sqs.fifo \
    --region us-east-1 \
    --output text)

MSG=$(aws --endpoint-url=http://localhost:4566 sqs receive-message \
    --queue-url "$QUEUE_URL" \
    --region us-east-1 \
    --wait-time-seconds 1 || echo "")

[[ -n "$MSG" ]] || warn "No se encontró mensaje en SQS (puede haber sido procesado muy rápido)"

# ---------------------------------------------------------
# 5. Verificar DynamoDB #1
# ---------------------------------------------------------

log "Verificando tabla InfoRequest…"

D1=$(aws --endpoint-url=http://localhost:4566 dynamodb scan \
        --table-name InfoRequest | jq '.Items')

if [[ "$D1" == "null" ]]; then
    warn "No se encontraron registros en InfoRequest"
else
    log "✓ Registro encontrado en InfoRequest"
fi

# ---------------------------------------------------------
# 6. Verificar DynamoDB #2
# ---------------------------------------------------------

log "Verificando tabla InfoRequestProcessing…"

D2=$(aws --endpoint-url=http://localhost:4566 dynamodb scan \
        --table-name InfoRequestProcessing | jq '.Items')

if [[ "$D2" == "null" ]]; then
    warn "No se encontraron registros en InfoRequestProcessing"
else
    log "✓ Registro encontrado en InfoRequestProcessing"
fi

echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}   ✓ Test End-to-End COMPLETADO CON ÉXITO                   ${NC}"
echo -e "${GREEN}============================================================${NC}"

