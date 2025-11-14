# Guía de Ejecución y Verificación - QAPI FacturacionMasiva

## 📋 Resumen del Proyecto

Es una aplicación **Spring Boot 3.4.2** con Java 17 que proporciona API REST para procesamiento masivo de facturas usando:
- **AWS SQS FIFO** - Para encolar solicitudes
- **AWS DynamoDB** - Para almacenar información de solicitudes
- **Keycloak OAuth2** - Para autenticación
- **Swagger/OpenAPI** - Para documentación de API

---

## 🚀 Paso 1: Verificar Requisitos Previos

```bash
# Verificar Java 17 instalado
java -version

# Verificar Maven instalado (si no está, usaremos ./mvnw)
mvn -version
```

**Requisitos:**
- ✅ Java 17 (mínimo)
- ✅ Maven 3.6+ (o usar ./mvnw)
- ✅ Git
- ✅ Docker (para LocalStack - opcional)

---

## 🔨 Paso 2: Compilar el Proyecto

```bash
cd /home/joseu_pescado/Descargas/QAPI_FacturacionMasiva

# Opción A: Usando Maven wrapper (recomendado)
./mvnw clean package -DskipTests

# Opción B: Usando Maven instalado
mvn clean package -DskipTests
```

**Tiempo esperado:** 2-5 minutos

**Resultado esperado:**
```
BUILD SUCCESS
target/QAPI_FacturacionMasiva-1.1.1.jar
```

---

## 🔑 Paso 3: Configurar Credenciales AWS (para Desarrollo)

### Opción A: Variables de Entorno

```bash
export AWS_ACCESS_KEY_ID="tu_access_key"
export AWS_SECRET_ACCESS_KEY="tu_secret_key"
export AWS_REGION="us-east-1"
```

### Opción B: Archivo ~/.aws/credentials

```ini
[default]
aws_access_key_id = tu_access_key
aws_secret_access_key = tu_secret_key
region = us-east-1
```

---

## 🏃 Paso 4: Ejecutar la Aplicación

### Opción A: Con Maven (Desarrollo)

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Opción B: Directamente con Java (Recomendado para testing)

```bash
java -jar target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev
```

### Opción C: Con Docker (Si está compilado)

```bash
docker build -t qapi-facturacion:1.1.1 .
docker run -p 8080:8080 \
  -e AWS_ACCESS_KEY_ID="tu_key" \
  -e AWS_SECRET_ACCESS_KEY="tu_secret" \
  -e SPRING_PROFILES_ACTIVE="dev" \
  qapi-facturacion:1.1.1
```

**Salida esperada:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.4.2)

QAPI_FacturacionMasiva started on port(s): 8080
```

---

## ✅ Paso 5: Verificar que la Aplicación Funciona

### 5.1 - Verificar Health Check

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

### 5.2 - Acceder a Swagger UI

```
http://localhost:8080/docs
```

Deberías ver la interfaz gráfica de Swagger con todos los endpoints documentados.

### 5.3 - Verificar Endpoints Disponibles

```bash
curl -X GET "http://localhost:8080/actuator/env" \
  -H "Content-Type: application/json" | grep -E "spring.profiles|app.name"
```

---

## 🔐 Paso 6: Autenticación con Keycloak (Producción)

Para acceder a los endpoints protegidos, necesitas obtener un token JWT:

```bash
# Obtener token usando credenciales client (del archivo test.txt)
TOKEN=$(curl -s -X POST "https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials' \
  -d 'client_id=apifactmasiva' \
  -d 'client_secret=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV' | jq -r '.access_token')

echo "Token: $TOKEN"
```

Luego usar el token en las peticiones:

```bash
curl -X POST "http://localhost:8080/facturacion-masiva/nota-debito" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{...}]'
```

---

## 🧪 Paso 7: Pruebas de API

### 7.1 - Endpoint: Generar Nota de Débito (Batch)

```bash
curl -X POST "http://localhost:8080/facturacion-masiva/nota-debito" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "tercero": {
        "claseIdentificacion": "CC",
        "numeroIdentificacion": 1234567890,
        "descripcionAuxiliar": "Cliente 1",
        "naturalJuridica": "Persona Natural",
        "tipoAuxiliar": "Tipo1",
        "tipoRetencion": "Retencion1",
        "codigoPais": 170,
        "codigoDepartamento": 25,
        "codigoCiudad": 25001,
        "celular": "3001234567",
        "telefono": "6011234567",
        "direccion": "Calle 1 #1-1",
        "correoElectronico": "cliente@example.com"
      },
      "conceptoPrincipal": [
        {
          "conceptoFacturacion": "Servicio",
          "cantidadUnidades": "1",
          "valorUnitario": "100000"
        }
      ]
    }
  ]'
```

**Respuesta esperada (si está conectado a SQS):**
```json
{
  "mensaje_id": "uuid-generado"
}
```

---

## 📊 Paso 8: Monitoreo y Logs

### Ver logs en tiempo real

```bash
# Si está corriendo con Maven
./mvnw spring-boot:run 2>&1 | tail -f

# Si está corriendo con Java
tail -f nohup.out
```

### Filtrar logs específicos

```bash
tail -f application.log | grep -E "ERROR|WARN|INFO"
```

### Logs esperados en startup

```
DEBUG: SecurityConfig activado para profile: dev
INFO: Starting QAPI_FacturacionMasiva
INFO: Swagger UI configurado en /docs
INFO: SqsTemplate inicializado
```

---

## 🔧 Paso 9: Solución de Problemas Comunes

### ❌ Error: "Failed to connect to AWS SQS"

**Causa:** Credenciales AWS no configuradas
**Solución:**
```bash
export AWS_ACCESS_KEY_ID="tu_key"
export AWS_SECRET_ACCESS_KEY="tu_secret"
```

### ❌ Error: "Spring profiles active: dev not found"

**Causa:** El perfil dev no está en application-dev.yml
**Solución:** Verifica que `/src/main/resources/application-dev.yml` exista

### ❌ Error: "Port 8080 already in use"

**Causa:** Otro servicio usando el puerto
**Solución:**
```bash
# Cambiar puerto
java -jar target/QAPI_FacturacionMasiva-1.1.1.jar \
  --spring.profiles.active=dev \
  --server.port=9090
```

### ❌ Error de compilación "Java version"

**Causa:** Java 17 no está instalado
**Solución:**
```bash
java -version
# Debe mostrar: openjdk version "17.x.x"
```

---

## 🎯 Checklist de Verificación Final

- [ ] **Compilación:** `mvn clean package` completado sin errores
- [ ] **Inicio:** Aplicación inicia en puerto 8080
- [ ] **Health Check:** GET `/actuator/health` retorna `{"status":"UP"}`
- [ ] **Swagger:** Accesible en `http://localhost:8080/docs`
- [ ] **Endpoints:** Al menos 1 POST endpoint visible en Swagger
- [ ] **Logs:** No hay errores CRITICAL en startup
- [ ] **AWS Config:** Si accedes a SQS, credenciales están configuradas

---

## 📝 Notas Importantes

### Configuración por Perfil

| Perfil | Uso | JWT Issuer | AWS Endpoint |
|--------|-----|-----------|--------------|
| **dev** | Desarrollo local | https://iam.ia.ucaldas.nuvu.cc/... | http://localhost:4566 |
| **prod** | Producción | https://iam.ia.ucaldas.nuvu.cc/... | AWS (real) |

### Dependencias Principales

```xml
- Spring Boot 3.4.2
- Spring Cloud AWS 3.4.0 (SQS, DynamoDB)
- Lombok (generación de getters/setters)
- Jackson (serialización JSON)
- JWT (io.jsonwebtoken 0.12.6)
- Swagger/OpenAPI (springdoc-openapi)
```

---

## 📞 Próximos Pasos

1. **Ejecutar pruebas unitarias:**
   ```bash
   mvn test
   ```

2. **Generar cobertura de pruebas:**
   ```bash
   mvn test jacoco:report
   ```

3. **Build Docker:**
   ```bash
   docker build -t qapi-facturacion:1.1.1 .
   ```

4. **Deploy con CloudFormation:**
   ```bash
   cd uca-devs-facturacionmasiva
   bash cf-ecs.deploy.sh
   ```

---

**Última actualización:** 13 de noviembre de 2025
