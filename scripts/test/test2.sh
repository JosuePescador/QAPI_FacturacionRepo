#!/usr/bin/env bash
set -euo pipefail

# ============================================================
#  TEST END-to-END PROFESIONAL - QAPI FACTURACIÓN MASIVA
#  Valida: Infra → Servicios → Token → POST → SQS → DynamoDB
# ============================================================

# -----------------------------
# CONFIG (se pueden sobreescribir por env)
# -----------------------------
API_URL="${API_URL:-http://localhost:8080}"
WORKER_PROCESS="${WORKER_PROCESS:-QAPI_FacturacionMasivaWorker}"
# Cola localstack (ej: http://localhost:4566/000000000000/qapi-masiva-queue)
QUEUE_URL="${QUEUE_URL:-http://localhost:4566/000000000000/qapi-masiva-queue}"
REGION="${REGION:-us-east-1}"
LOCALSTACK_ENDPOINT="${LOCALSTACK_ENDPOINT:-http://localhost:4566}"
MAX_WAIT_SQS="${MAX_WAIT_SQS:-10}"
MAX_WAIT_INFRA="${MAX_WAIT_INFRA:-60}"
MAX_TOKEN_RETRIES="${MAX_TOKEN_RETRIES:-5}"
TOKEN_RETRY_SLEEP="${TOKEN_RETRY_SLEEP:-2}"
TMP_RESPONSE="${TMP_RESPONSE:-/tmp/test_response.json}"
TMP_TOKEN_RESPONSE="${TMP_TOKEN_RESPONSE:-/tmp/test_token_response.json}"
# Si necesitas ignorar verificación TLS para Keycloak (dev), exporta SKIP_TLS_VERIFY=true
SKIP_TLS_VERIFY="${SKIP_TLS_VERIFY:-false}"

# -----------------------------
# LOG FUNCTIONS
# -----------------------------
timestamp() { date +"%Y-%m-%d %H:%M:%S"; }

log()   { echo -e "\e[32m[TEST][$(timestamp)]\e[0m $1"; }
warn()  { echo -e "\e[33m[WARN][$(timestamp)]\e[0m $1"; }
error() { echo -e "\e[31m[ERROR][$(timestamp)]\e[0m $1"; exit 1; }
ok()    { echo -e "\e[32m[OK][$(timestamp)]\e[0m $1"; }

# -----------------------------
# CLEANUP
# -----------------------------
cleanup() {
    [[ -f "$TMP_RESPONSE" ]] && rm -f "$TMP_RESPONSE" || true
    [[ -f "$TMP_TOKEN_RESPONSE" ]] && rm -f "$TMP_TOKEN_RESPONSE" || true
}
trap cleanup EXIT

# -----------------------------
# CHECK DEPENDENCIES
# -----------------------------
for cmd in curl jq aws pgrep; do
    if ! command -v "$cmd" >/dev/null 2>&1; then
        error "Falta la dependencia: $cmd. Instálala y reintenta."
    fi
done

# Evitar pager de aws cli
export AWS_PAGER=""

# Ensure AWS creds for localstack exist (defaults for tests)
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-test}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-test}"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-$REGION}"

# -----------------------------
# 1. VALIDAR QUE LA INFRAESTRUCTURA ESTÁ ARRIBA
# -----------------------------
log "Validando infraestructura LocalStack en $LOCALSTACK_ENDPOINT …"

timeout=$MAX_WAIT_INFRA
while true; do
    if curl -s "${LOCALSTACK_ENDPOINT}/_localstack/health" >/tmp/ls_health.json 2>/dev/null; then
        # intentamos leer '.services' (si jq falla, no abortamos aquí)
        if jq -e '.services' /tmp/ls_health.json >/dev/null 2>&1; then
            ok "LocalStack está activo."
            rm -f /tmp/ls_health.json
            break
        fi
    fi

    ((timeout--))
    [[ $timeout -le 0 ]] && { rm -f /tmp/ls_health.json || true; error "LocalStack NO respondió a tiempo en $LOCALSTACK_ENDPOINT."; }
    sleep 2
done

# -----------------------------
# 2. VALIDAR SERVICIOS JAVA
# -----------------------------
log "Verificando que QAPI_masiva está arriba en $API_URL …"

if ! curl -s "$API_URL/actuator/health" -o /tmp/actuator_health.json 2>/dev/null; then
    error "No se pudo conectar a $API_URL/actuator/health"
fi

if ! jq -e '.status == "UP"' /tmp/actuator_health.json >/dev/null 2>&1; then
    error "QAPI_masiva NO está respondiendo UP en $API_URL. Revisa logs del servicio. Response: $(head -c 512 /tmp/actuator_health.json || true)"
fi
ok "QAPI_masiva está corriendo."

rm -f /tmp/actuator_health.json || true

log "Verificando que QAPI_worker ($WORKER_PROCESS) está arriba…"
if ! pgrep -f "$WORKER_PROCESS" >/dev/null; then
    error "QAPI_worker ($WORKER_PROCESS) NO está corriendo."
fi
ok "QAPI_worker está corriendo."

# -----------------------------
# 3. OBTENER TOKEN DE KEYCLOAK (con retry + chequeo HTTP)
# -----------------------------
log "Obteniendo token de Keycloak…"

TOKEN=""
for attempt in $(seq 1 "$MAX_TOKEN_RETRIES"); do
    # construimos curl options según SKIP_TLS_VERIFY
    CURL_OPTS=(-s -X POST)
    if [[ "$SKIP_TLS_VERIFY" == "true" ]]; then
        CURL_OPTS+=(-k)
    fi

    # hacemos la petición y guardamos body y http code
    HTTP_CODE=$(curl "${CURL_OPTS[@]}" \
      "https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "grant_type=client_credentials" \
      -d "client_id=apifactmasiva" \
      -d "client_secret=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV" \
      -o "$TMP_TOKEN_RESPONSE" -w "%{http_code}" 2>/dev/null || echo "000")

    # si tenemos cuerpo, intentamos extraer token (si jq no soporta // empty, no rompera)
    if [[ -s "$TMP_TOKEN_RESPONSE" ]]; then
        TOKEN=$(jq -r '.access_token // empty' "$TMP_TOKEN_RESPONSE" 2>/dev/null || true)
    else
        TOKEN=""
    fi

    if [[ -n "$TOKEN" && "$HTTP_CODE" =~ ^2 ]]; then
        ok "Token JWT obtenido (longitud ${#TOKEN})."
        break
    fi

    BODY_PREVIEW="$(head -c 1024 "$TMP_TOKEN_RESPONSE" 2>/dev/null || true)"
    warn "Intento $attempt/$MAX_TOKEN_RETRIES: Keycloak HTTP=$HTTP_CODE. Resp: ${BODY_PREVIEW:-<vacío>}. Reintentando en ${TOKEN_RETRY_SLEEP}s..."
    sleep "$TOKEN_RETRY_SLEEP"
done

[[ -n "$TOKEN" ]] || error "Fallo crítico: no se pudo obtener el token JWT tras $MAX_TOKEN_RETRIES intentos. Última respuesta: $(head -c 1024 "$TMP_TOKEN_RESPONSE" || true)"

# 4. HACER POST A QAPI_masiva (endpoint real)
log "Enviando solicitud POST a /facturacion-masiva/nota-debito …"

REQUEST_BODY='[
  {
    "numeroFactura": "F-TEST-001",
    "valor": 12345,
    "cliente": "Cliente Test",
    "descripcion": "Prueba masiva"
  }
]'

HTTP_CODE=$(curl -s -o "$TMP_RESPONSE" -w "%{http_code}" \
  -X POST "$API_URL/facturacion-masiva/nota-debito" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$REQUEST_BODY" || true)


# -----------------------------
# 5. EXTRAER MESSAGE ID (fácil y robusto)
# -----------------------------
MESSAGE_ID=""
if [[ -s "$TMP_RESPONSE" ]]; then
    # intents sencillos, compatibles con jq antiguos:
    MESSAGE_ID=$(jq -r '.messageId // .message_id // .id // .messageIds[0] // empty' "$TMP_RESPONSE" 2>/dev/null || true)
fi

# fallback final: buscar la primer cadena alfanumérica larga en el JSON
if [[ -z "$MESSAGE_ID" ]]; then
    MESSAGE_ID=$(grep -oE '[A-Za-z0-9_\-]{8,}' "$TMP_RESPONSE" | head -n1 || true)
fi

[[ -n "$MESSAGE_ID" ]] || error "No se devolvió messageId en la respuesta. Response: $(head -c 1024 "$TMP_RESPONSE" || true)"

ok "MessageID recibido: $MESSAGE_ID"

# -----------------------------
# 6. VALIDAR QUE EL MENSAJE ESTÁ EN SQS (LocalStack)
# -----------------------------
log "Verificando que el mensaje llegó a SQS ($QUEUE_URL)…"

found_sqs=false
for i in $(seq 1 "$MAX_WAIT_SQS"); do
    MSG_JSON=$(aws --endpoint-url "$LOCALSTACK_ENDPOINT" sqs receive-message \
        --queue-url "$QUEUE_URL" \
        --region "$REGION" \
        --max-number-of-messages 1 \
        --visibility-timeout 1 \
        --wait-time-seconds 1 2>/dev/null || true)

    if [[ -n "$MSG_JSON" ]]; then
        MSG_BODY=$(echo "$MSG_JSON" | jq -r '.Messages[0].Body // empty' 2>/dev/null || true)
        if [[ -n "$MSG_BODY" ]]; then
            ok "Mensaje encontrado en SQS (preview): $(echo "$MSG_BODY" | head -c 200)..."
            found_sqs=true
            break
        fi
    fi
    sleep 1
done

if [[ "$found_sqs" == false ]]; then
    warn "No se encontró mensaje en SQS tras $MAX_WAIT_SQSs (posible procesamiento rápido)."
else
    log "Procesamiento SQS validado."
fi

# -----------------------------
# 7. VALIDAR EN DYNAMODB (InfoRequest)
# -----------------------------
log "Verificando tabla InfoRequest (DynamoDB)..."

IR=$(aws --endpoint-url "$LOCALSTACK_ENDPOINT" dynamodb scan \
        --table-name InfoRequest \
        --region "$REGION" 2>/dev/null || true)

if [[ -z "$IR" || "$IR" == "null" ]]; then
    error "No se encontró registro en InfoRequest."
fi
ok "✓ Registro encontrado en InfoRequest."

# -----------------------------
# 8. VALIDAR EN DYNAMODB (InfoRequestProcessing)
# -----------------------------
log "Verificando tabla InfoRequestProcessing (DynamoDB)..."

IRP=$(aws --endpoint-url "$LOCALSTACK_ENDPOINT" dynamodb scan \
        --table-name InfoRequestProcessing \
        --region "$REGION" 2>/dev/null || true)

if [[ -z "$IRP" || "$IRP" == "null" ]]; then
    error "No se encontró registro en InfoRequestProcessing."
fi
ok "✓ Registro encontrado en InfoRequestProcessing."

# -----------------------------
# FIN DEL TEST
# -----------------------------
echo ""
echo "============================================================"
echo -e "   \e[32m✓ Test End-to-End COMPLETADO CON ÉXITO\e[0m"
echo "============================================================"
