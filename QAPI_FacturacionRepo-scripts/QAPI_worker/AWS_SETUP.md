# Configuración de AWS para QAPI_FacturacionMasivaWorker

## Tabla DynamoDB

### Crear tabla `FacturacionMasivaLogs`

Ejecuta este comando con AWS CLI:

```bash
aws dynamodb create-table \
    --table-name FacturacionMasivaLogs \
    --attribute-definitions \
        AttributeName=procedure,AttributeType=S \
        AttributeName=timestamp,AttributeType=S \
    --key-schema \
        AttributeName=procedure,KeyType=HASH \
        AttributeName=timestamp,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    --region us-east-1 \
    --tags \
        Key=Environment,Value=uca-test \
        Key=Application,Value=QAPI_FacturacionMasivaWorker
```

### Configurar TTL (Time To Live) - Opcional

Para eliminar automáticamente logs después de 90 días:

```bash
aws dynamodb update-time-to-live \
    --table-name FacturacionMasivaLogs \
    --time-to-live-specification \
        Enabled=true,AttributeName=ttl \
    --region us-east-1
```

### Estructura de la tabla

| Campo        | Tipo   | Descripción                                    |
|--------------|--------|------------------------------------------------|
| procedure    | String | **Partition Key** - Tipo (FACTURA, NOTA-DEBITO) |
| timestamp    | String | **Sort Key** - ISO-8601 timestamp             |
| status       | String | SUCCESS o ERROR                                |
| success      | Boolean| Indicador de éxito                            |
| errorMessage | String | Mensaje de error (null si exitoso)            |
| payload      | String | JSON completo del mensaje procesado           |
| ttl          | Number | Timestamp Unix para expiración automática     |

### Ejemplo de item

```json
{
  "procedure": "FACTURA",
  "timestamp": "2025-11-05T14:23:45.123Z",
  "status": "SUCCESS",
  "success": true,
  "errorMessage": null,
  "payload": "{\"referencia\":\"REF123\",\"ciclo_lectivo\":\"2025-1\",...}",
  "ttl": 1739568225
}
```

## Configuración de SQS

### Verificar configuración de la cola FIFO

Las colas ya existen, pero asegúrate de que tengan:

**Cola principal**: `uca-test-factmasiva-sqs.fifo`
- **Visibility Timeout**: 30 segundos (aumentado desde 15)
- **Message Retention**: 4 días
- **Receive Message Wait Time**: 20 segundos (Long Polling)
- **Dead Letter Queue**: Configurada con `maxReceiveCount=3`
- **Content-Based Deduplication**: Habilitado

**Dead Letter Queue**: `uca-test-factmasiva-DLQ.fifo`
- **Message Retention**: 14 días (para análisis)

### Actualizar configuración de la cola principal

```bash
# Aumentar visibility timeout
aws sqs set-queue-attributes \
    --queue-url https://sqs.us-east-1.amazonaws.com/225711149285/uca-test-factmasiva-sqs.fifo \
    --attributes VisibilityTimeout=30 \
    --region us-east-1

# Configurar Long Polling
aws sqs set-queue-attributes \
    --queue-url https://sqs.us-east-1.amazonaws.com/225711149285/uca-test-factmasiva-sqs.fifo \
    --attributes ReceiveMessageWaitTimeSeconds=20 \
    --region us-east-1
```

## Permisos IAM

### Política IAM requerida para el servicio

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes",
        "sqs:SendMessage"
      ],
      "Resource": [
        "arn:aws:sqs:us-east-1:225711149285:uca-test-factmasiva-sqs.fifo",
        "arn:aws:sqs:us-east-1:225711149285:uca-test-factmasiva-DLQ.fifo"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:Query",
        "dynamodb:Scan",
        "dynamodb:DeleteItem"
      ],
      "Resource": "arn:aws:dynamodb:us-east-1:225711149285:table/FacturacionMasivaLogs"
    }
  ]
}
```

## Queries útiles para DynamoDB

### Consultar logs por procedimiento

```bash
aws dynamodb query \
    --table-name FacturacionMasivaLogs \
    --key-condition-expression "procedure = :proc" \
    --expression-attribute-values '{":proc":{"S":"FACTURA"}}' \
    --region us-east-1
```

### Consultar logs de errores

```bash
aws dynamodb scan \
    --table-name FacturacionMasivaLogs \
    --filter-expression "#s = :status" \
    --expression-attribute-names '{"#s":"status"}' \
    --expression-attribute-values '{":status":{"S":"ERROR"}}' \
    --region us-east-1
```

### Consultar logs por rango de tiempo

```bash
aws dynamodb query \
    --table-name FacturacionMasivaLogs \
    --key-condition-expression "procedure = :proc AND #ts BETWEEN :start AND :end" \
    --expression-attribute-names '{"#ts":"timestamp"}' \
    --expression-attribute-values '{
        ":proc":{"S":"FACTURA"},
        ":start":{"S":"2025-11-05T00:00:00Z"},
        ":end":{"S":"2025-11-05T23:59:59Z"}
    }' \
    --region us-east-1
```

## Monitoreo con CloudWatch

### Métricas automáticas de AWS Spring Cloud

AWS Spring Cloud publica automáticamente métricas a CloudWatch:

- `spring.cloud.aws.sqs.listener.messages.received`
- `spring.cloud.aws.sqs.listener.messages.processed`
- `spring.cloud.aws.sqs.listener.messages.failed`
- `spring.cloud.aws.sqs.listener.processing.time`

### Alarmas recomendadas

1. **Mensajes en DLQ**: Alerta cuando `ApproximateNumberOfMessagesVisible` > 10
2. **Edad de mensajes**: Alerta cuando `ApproximateAgeOfOldestMessage` > 300 segundos
3. **Errores de procesamiento**: Alerta cuando tasa de errores > 10%
4. **DynamoDB Throttling**: Alerta cuando `UserErrors` > 0

## Deployment

### Actualizar buildspec.yml (si es necesario)

No se requieren cambios en el buildspec.yml ya que las dependencias se gestionan con Maven.

### Variables de entorno (opcional)

Si prefieres usar variables de entorno en vez de application.properties:

```bash
export AWS_REGION=us-east-1
export SQS_FIFO_QUEUE_NAME=uca-test-factmasiva-sqs.fifo
export SQS_DLQ_QUEUE_NAME=uca-test-factmasiva-DLQ.fifo
export AWS_DYNAMODB_TABLE_LOGS=FacturacionMasivaLogs
```

## Testing

### Enviar mensaje de prueba a SQS

```bash
aws sqs send-message \
    --queue-url https://sqs.us-east-1.amazonaws.com/225711149285/uca-test-factmasiva-sqs.fifo \
    --message-body '{
        "service": "FACTURA",
        "data": {
            "referencia": "TEST-001",
            "ciclo_lectivo": "2025-1",
            "nit_tercero": "123456789",
            "cuenta_consignacion": "CTA001",
            "listado_cptos_principales": [
                {
                    "concepto_facturacion": "MATRICULA",
                    "cantidad_unidades": 1,
                    "valor_unitario": 1000000
                }
            ],
            "tercero": {
                "clase_identificacion": "CC",
                "numero_identificacion": "123456789",
                "descripcion_auxiliar": "Test Usuario"
            }
        }
    }' \
    --message-group-id "test-group" \
    --message-deduplication-id "test-$(date +%s)" \
    --region us-east-1
```

### Verificar procesamiento

```bash
# Verificar logs en DynamoDB
aws dynamodb query \
    --table-name FacturacionMasivaLogs \
    --key-condition-expression "procedure = :proc" \
    --expression-attribute-values '{":proc":{"S":"FACTURA"}}' \
    --limit 1 \
    --scan-index-forward false \
    --region us-east-1
```
