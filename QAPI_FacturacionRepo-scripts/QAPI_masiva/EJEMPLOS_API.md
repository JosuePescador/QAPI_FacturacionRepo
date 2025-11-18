# Ejemplos de Pruebas de API - QAPI FacturacionMasiva

## Base URL
```
http://localhost:8080
```

---

## 1. HEALTH CHECK - Verificar que la aplicación está funcionando

```bash
curl -X GET "http://localhost:8080/actuator/health" \
  -H "Content-Type: application/json"
```

**Respuesta esperada:**
```json
{
  "status": "UP"
}
```

---

## 2. SWAGGER UI - Documentación interactiva

Abre en el navegador:
```
http://localhost:8080/docs
```

O descarga la definición OpenAPI:
```bash
curl -X GET "http://localhost:8080/api-docs" \
  -H "Content-Type: application/json" | jq
```

---

## 3. OBTENER TOKEN JWT - Para acceder a endpoints protegidos

```bash
TOKEN=$(curl -s -X POST \
  "https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=apifactmasiva" \
  -d "client_secret=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV" | jq -r '.access_token')

echo "Token obtenido: $TOKEN"
```

---

## 4. GENERAR NOTA DE DÉBITO - Enviar en batch

**Endpoint:** `POST /facturacion-masiva/nota-debito`

```bash
curl -X POST "http://localhost:8080/facturacion-masiva/nota-debito" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
    {
      "tercero": {
        "claseIdentificacion": "CC",
        "numeroIdentificacion": 1234567890,
        "descripcionAuxiliar": "Cliente Empresa SA",
        "naturalJuridica": "Persona Natural",
        "tipoAuxiliar": "Comprador",
        "tipoRetencion": "Ninguna",
        "codigoPais": 170,
        "codigoDepartamento": 25,
        "codigoCiudad": 25001,
        "celular": "3001234567",
        "telefono": "6011234567",
        "direccion": "Calle Principal #123",
        "correoElectronico": "cliente@empresa.com"
      },
      "conceptoPrincipal": [
        {
          "conceptoFacturacion": "Servicio de Consultoría",
          "cantidadUnidades": "10",
          "valorUnitario": "100000"
        },
        {
          "conceptoFacturacion": "Soporte Técnico",
          "cantidadUnidades": "5",
          "valorUnitario": "50000"
        }
      ]
    },
    {
      "tercero": {
        "claseIdentificacion": "NIT",
        "numeroIdentificacion": 9876543210,
        "descripcionAuxiliar": "Empresa Distribuidora",
        "naturalJuridica": "Persona Jurídica",
        "tipoAuxiliar": "Vendedor",
        "tipoRetencion": "IVA",
        "codigoPais": 170,
        "codigoDepartamento": 5,
        "codigoCiudad": 5001,
        "celular": "3119876543",
        "telefono": "6015555555",
        "direccion": "Avenida Comercial #456",
        "correoElectronico": "contacto@distribuidor.com"
      },
      "conceptoPrincipal": [
        {
          "conceptoFacturacion": "Productos Varios",
          "cantidadUnidades": "100",
          "valorUnitario": "25000"
        }
      ]
    }
  ]'
```

**Respuesta esperada (si está conectado a SQS):**
```json
{
  "mensaje_id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

---

## 5. GENERAR FACTURA - Ejemplo similar

```bash
curl -X POST "http://localhost:8080/facturacion-masiva/factura" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "tercero": {
      "claseIdentificacion": "CC",
      "numeroIdentificacion": 1234567890,
      "descripcionAuxiliar": "Cliente Individual",
      "naturalJuridica": "Persona Natural",
      "tipoAuxiliar": "Comprador",
      "tipoRetencion": "Ninguna",
      "codigoPais": 170,
      "codigoDepartamento": 5,
      "codigoCiudad": 5001,
      "celular": "3001234567",
      "telefono": "6011234567",
      "direccion": "Calle 10 #20-30",
      "correoElectronico": "cliente@ejemplo.com"
    },
    "conceptoPrincipal": [
      {
        "conceptoFacturacion": "Producto A",
        "cantidadUnidades": "5",
        "valorUnitario": "50000"
      }
    ]
  }'
```

---

## 6. GENERAR NOTA DE CRÉDITO - Anular/Devolver

```bash
curl -X POST "http://localhost:8080/facturacion-masiva/nota-credito" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "tercero": {
      "claseIdentificacion": "CC",
      "numeroIdentificacion": 1234567890,
      "descripcionAuxiliar": "Cliente Empresa SA",
      "naturalJuridica": "Persona Natural",
      "tipoAuxiliar": "Comprador",
      "tipoRetencion": "Ninguna",
      "codigoPais": 170,
      "codigoDepartamento": 25,
      "codigoCiudad": 25001,
      "celular": "3001234567",
      "telefono": "6011234567",
      "direccion": "Calle Principal #123",
      "correoElectronico": "cliente@empresa.com"
    },
    "conceptoPrincipal": [
      {
        "conceptoFacturacion": "Devolución Producto",
        "cantidadUnidades": "2",
        "valorUnitario": "100000"
      }
    ]
  }'
```

---

## 7. REGISTRAR PAGO - Marcar factura como pagada

```bash
curl -X POST "http://localhost:8080/facturacion-masiva/pago" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "numeroFactura": "FV-2025-000123",
    "fechaPago": "2025-11-13",
    "monto": 500000,
    "metodoPago": "Transferencia Bancaria"
  }'
```

---

## 8. MONITOREO - Obtener estado de las solicitudes

```bash
# Ver logs en tiempo real
tail -f nohup.out

# Ver solo errores
tail -f nohup.out | grep -i error

# Ver solo warnings
tail -f nohup.out | grep -i warn
```

---

## 9. TESTING SIN AUTENTICACIÓN (Modo Dev)

Si estás en modo `dev`, algunos endpoints podrían estar sin protección:

```bash
curl -X POST "http://localhost:8080/facturacion-masiva/nota-debito" \
  -H "Content-Type: application/json" \
  -d '[{
    "tercero": {"claseIdentificacion": "CC", "numeroIdentificacion": 1234567890},
    "conceptoPrincipal": [{"conceptoFacturacion": "Servicio", "cantidadUnidades": "1", "valorUnitario": "100000"}]
  }]'
```

---

## 10. VALIDAR CONEXIÓN A AWS (Si está configurado)

```bash
# Verificar SQS
aws sqs list-queues --region us-east-1

# Verificar DynamoDB
aws dynamodb list-tables --region us-east-1

# Ver mensajes en cola SQS
aws sqs receive-message \
  --queue-url "https://sqs.us-east-1.amazonaws.com/225711149285/uca-test-factmasiva-sqs.fifo" \
  --region us-east-1
```

---

## 11. SCRIPT COMPLETO DE PRUEBA (Bash)

```bash
#!/bin/bash

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="http://localhost:8080"

echo -e "${BLUE}🧪 Iniciando pruebas de API${NC}"
echo ""

# Health Check
echo -e "${BLUE}1. Health Check${NC}"
curl -s -X GET "$BASE_URL/actuator/health" | jq .
echo ""

# Swagger
echo -e "${BLUE}2. Verificando Swagger${NC}"
curl -s -o /dev/null -w "Status: %{http_code}\n" "$BASE_URL/docs"
echo ""

# Nota de Débito (sin autenticación en dev)
echo -e "${BLUE}3. Enviando Nota de Débito (Batch)${NC}"
curl -s -X POST "$BASE_URL/facturacion-masiva/nota-debito" \
  -H "Content-Type: application/json" \
  -d '[{
    "tercero": {"claseIdentificacion": "CC", "numeroIdentificacion": 1234567890, "descripcionAuxiliar": "Test"},
    "conceptoPrincipal": [{"conceptoFacturacion": "Test", "cantidadUnidades": "1", "valorUnitario": "50000"}]
  }]' | jq .
echo ""

echo -e "${GREEN}✓ Pruebas completadas${NC}"
```

Guarda como `test-api.sh` y ejecuta con `bash test-api.sh`

---

## Notas Importantes

- **Autenticación:** Requerida en PROD, opcional en DEV
- **SQS:** Necesita credenciales AWS configuradas
- **DynamoDB:** La tabla debe existir o ser creada automáticamente
- **Rate Limiting:** No configurado por defecto
- **CORS:** Revisar `SecurityConfig.java` si tienes problemas de CORS

