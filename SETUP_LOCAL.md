# 🚀 Guía de Ejecución Local Completa - QAPI Facturación Masiva

Esta guía te permitirá ejecutar y probar el sistema completo localmente usando LocalStack.

---

## 📋 Requisitos Previos

- ✅ **Java 17** instalado
- ✅ **Maven 3.6+** instalado
- ✅ **Docker** y **Docker Compose** instalados
- ✅ **AWS CLI** instalado
- ✅ **jq** instalado (para formatear JSON)
- ✅ **curl** instalado

### Verificar instalaciones:

```bash
java -version      # Debe mostrar Java 17
mvn -version       # Debe mostrar Maven 3.6+
docker --version   # Debe mostrar Docker
aws --version      # Debe mostrar AWS CLI
jq --version       # Debe mostrar jq
```

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│ QAPI_masiva │─────▶│   SQS FIFO   │─────▶│ QAPI_worker │
│  (API REST) │      │  (LocalStack)│      │ (Processor) │
│   :8080     │      └──────────────┘      │   :8081     │
└──────┬──────┘                            └──────┬──────┘
       │                                          │
       ▼                                          ▼
┌──────────────┐                          ┌──────────────┐
│ DynamoDB #1  │                          │ DynamoDB #2  │
│ InfoRequest  │                          │ Processing   │
│ (LocalStack) │                          │ (LocalStack) │
└──────────────┘                          └──────────────┘
```

---

## 🔧 PASO 1: Iniciar LocalStack

LocalStack simula AWS localmente (SQS, DynamoDB, S3).

### 1.1 Iniciar LocalStack con Docker Compose

```bash
cd /home/adminblend/Escritorio/Blend/ucaldas/QAPI_FacturacionRepo
docker-compose up -d
```

Verás:
```
✔ Container localstack-qapi  Started
```

### 1.2 Verificar que LocalStack está corriendo

```bash
curl http://localhost:4566/_localstack/health
```

Deberías ver:
```json
{
  "services": {
    "sqs": "running",
    "dynamodb": "running",
    "s3": "running"
  }
}
```

### 1.3 Inicializar recursos de AWS

```bash
chmod +x init-localstack.sh
./init-localstack.sh
```

Esto creará:
- ✅ Cola SQS FIFO: `uca-test-factmasiva-sqs.fifo`
- ✅ Tabla DynamoDB #1: `InfoRequest`
- ✅ Tabla DynamoDB #2: `InfoRequestProcessing`
- ✅ Bucket S3: `uca-test-facturacionmasiva-auditory`

---

## 📦 PASO 2: Compilar los Proyectos

### 2.1 Compilar QAPI_masiva

```bash
cd QAPI_masiva
mvn clean package -DskipTests
```

Resultado esperado:
```
BUILD SUCCESS
target/QAPI_FacturacionMasiva-1.1.1.jar
```

### 2.2 Compilar QAPI_worker

```bash
cd ../QAPI_worker
mvn clean package -DskipTests
```

Resultado esperado:
```
BUILD SUCCESS
target/QAPI_FacturacionMasivaWorker-1.1.0.jar
```

---

## 🚀 PASO 3: Ejecutar las Aplicaciones

### 3.1 Ejecutar QAPI_masiva (Terminal 1)

```bash
cd /home/adminblend/Escritorio/Blend/ucaldas/QAPI_FacturacionRepo/QAPI_masiva
java -jar target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev
```

Espera a ver:
```
Started QapiFacturacionMasivaApplication in X.XXX seconds
```

### 3.2 Ejecutar QAPI_worker (Terminal 2)

```bash
cd /home/adminblend/Escritorio/Blend/ucaldas/QAPI_FacturacionRepo/QAPI_worker
java -jar target/QAPI_FacturacionMasivaWorker-1.1.0.jar --spring.profiles.active=dev
```

Espera a ver:
```
Started QapiFacturacionMasivaWorkerApplication in X.XXX seconds
```

---

## 🧪 PASO 4: Ejecutar Prueba End-to-End

En una **tercera terminal**, ejecuta el script de prueba:

```bash
cd /home/joseu_pescado/Descargas/QAPI_FacturacionRepo
chmod +x test-e2e.sh
./test-e2e.sh
```

Este script:
1. ✅ Verifica que todos los servicios estén corriendo
2. ✅ Obtiene un token JWT de Keycloak
3. ✅ Envía una solicitud POST a `/facturacion-masiva/nota-debito`
4. ✅ Verifica que el mensaje llegó a SQS
5. ✅ Verifica que se guardó en DynamoDB #1 (InfoRequest)
6. ✅ Verifica que se procesó y guardó en DynamoDB #2 (InfoRequestProcessing)

---

## 🔍 PASO 5: Verificar Datos en DynamoDB

### Opción A: Con AWS CLI

```bash
# Ver registros en DynamoDB #1 (InfoRequest)
aws --endpoint-url=http://localhost:4566 dynamodb scan \
    --table-name InfoRequest | jq '.Items'

# Ver registros en DynamoDB #2 (InfoRequestProcessing)
aws --endpoint-url=http://localhost:4566 dynamodb scan \
    --table-name InfoRequestProcessing | jq '.Items'
```

### Opción B: Con interfaz web de LocalStack

1. Instala la extensión de LocalStack:
```bash
pip install awscli-local
```

2. Usa comandos más simples:
```bash
awslocal dynamodb scan --table-name InfoRequest
awslocal dynamodb scan --table-name InfoRequestProcessing
```

### Opción C: Usar DynamoDB Admin (GUI)

```bash
npm install -g dynamodb-admin
DYNAMO_ENDPOINT=http://localhost:4566 dynamodb-admin
```

Abre: `http://localhost:8001`

---

## 📊 PASO 6: Prueba Manual con cURL

Si prefieres hacer la prueba manualmente:

### 6.1 Obtener Token JWT

```bash
TOKEN=$(curl -s -X POST \
  "https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=apifactmasiva" \
  -d "client_secret=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV" | jq -r '.access_token')

echo "Token: $TOKEN"
```

### 6.2 Enviar Solicitud

```bash
curl -X POST "http://localhost:8080/facturacion-masiva/nota-debito" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
    {
      "referencia": "TEST-001",
      "cicloLectivo": "2025-1",
      "nit_tercero": 123456789,
      "cuentaConsignacion": 987654321,
      "tercero": {
        "claseIdentificacion": "CC",
        "numeroIdentificacion": 123456789,
        "descripcionAuxiliar": "Cliente Test",
        "naturalJuridica": "Persona Natural",
        "tipoAuxiliar": "Comprador",
        "tipoRetencion": "Ninguna",
        "codigoPais": 170,
        "codigoDepartamento": 25,
        "codigoCiudad": 25001,
        "celular": "3001234567",
        "telefono": "6011234567",
        "direccion": "Calle 123",
        "correoElectronico": "test@example.com"
      },
      "listado_cptos_principales": [
        {
          "conceptoFacturacion": "Servicio Test",
          "cantidadUnidades": "1",
          "valorUnitario": "100000"
        }
      ]
    }
  ]' | jq
```

**Respuesta esperada (202 Accepted):**

```json
{
  "status": "success",
  "message": "Solicitudes encoladas correctamente",
  "totalSolicitudes": 1,
  "messageIds": [
    "mensaje-id-uuid"
  ],
  "timestamp": "2025-11-14T..."
}
```

### 6.3 Verificar en DynamoDB

```bash
# Ver lo que se guardó en InfoRequest
aws --endpoint-url=http://localhost:4566 dynamodb scan \
    --table-name InfoRequest | jq '.Items[0]'
```

Deberías ver algo como:

```json
{
  "id": { "S": "mensaje-id-uuid" },
  "tipoServicio": { "S": "NOTA-DEBITO" },
  "estado": { "S": "Procesando" },
  "timestamp": { "S": "2025-11-14T..." },
  "params": { "S": "{...}" }
}
```

---

## 🛠️ Comandos Útiles

### Ver logs de LocalStack

```bash
docker logs -f localstack-qapi
```

### Ver mensajes en SQS

```bash
QUEUE_URL=$(aws --endpoint-url=http://localhost:4566 sqs get-queue-url \
    --queue-name uca-test-factmasiva-sqs.fifo --output text)

aws --endpoint-url=http://localhost:4566 sqs receive-message \
    --queue-url "$QUEUE_URL" \
    --max-number-of-messages 10
```

### Limpiar cola SQS

```bash
aws --endpoint-url=http://localhost:4566 sqs purge-queue \
    --queue-url "$QUEUE_URL"
```

### Limpiar DynamoDB

```bash
aws --endpoint-url=http://localhost:4566 dynamodb delete-table \
    --table-name InfoRequest

aws --endpoint-url=http://localhost:4566 dynamodb delete-table \
    --table-name InfoRequestProcessing
```

Luego vuelve a ejecutar `./init-localstack.sh`

---

## 🐛 Troubleshooting

### LocalStack no arranca

```bash
docker-compose down
docker-compose up -d
docker logs -f localstack-qapi
```

### QAPI_masiva da error 500

Verifica que compilaste con los cambios más recientes:
```bash
cd QAPI_masiva
mvn clean package -DskipTests
```

Reinicia la aplicación.

### No se ve nada en DynamoDB #2

Verifica que el worker esté corriendo y revisa sus logs.
El worker puede tener problemas conectándose a Oracle en modo dev.

### Error "Could not connect to server"

Verifica que las aplicaciones estén corriendo:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:4566/_localstack/health
```

---

## 📝 Checklist de Ejecución

- [ ] LocalStack corriendo (`docker-compose up -d`)
- [ ] Recursos de AWS creados (`./init-localstack.sh`)
- [ ] QAPI_masiva compilado y corriendo en :8080
- [ ] QAPI_worker compilado y corriendo en :8081
- [ ] Token JWT obtenido
- [ ] Solicitud POST enviada exitosamente (202)
- [ ] Mensaje verificado en SQS
- [ ] Registro verificado en DynamoDB InfoRequest
- [ ] Registro verificado en DynamoDB InfoRequestProcessing

---

## 🎯 Resultado Final Esperado

Después de ejecutar todo:

1. **QAPI_masiva** recibe tu solicitud → retorna 202 Accepted
2. **SQS** tiene el mensaje encolado
3. **DynamoDB InfoRequest** tiene un registro con estado "Procesando"
4. **QAPI_worker** consume el mensaje de SQS
5. **DynamoDB InfoRequestProcessing** tiene un registro con el resultado ("Éxito" o "Error")

¡Listo! 🎉
