# 🚀 QAPI FacturacionMasiva v1.1.1

Sistema de **Facturación Masiva en AWS** con Spring Boot 3.4.2

---

## ⚡ Inicio Rápido (2 minutos)

### Opción 1: Script Automático (Recomendado)

```bash
cd /home/joseu_pescado/Descargas/QAPI_FacturacionMasiva

# Menú interactivo
./run.sh

# O directamente: compilar y ejecutar
./run.sh 1
```

### Opción 2: Comandos Manuales

```bash
# Compilar
./mvnw clean package -DskipTests

# Ejecutar
java -jar target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev
```

---

## ✅ Verificar que Funciona

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Swagger UI (abre en navegador)
http://localhost:8080/docs
```

---

## 📚 Documentación Completa

| Documento | Contenido |
|-----------|-----------|
| **[GUIA_EJECUCION.md](./GUIA_EJECUCION.md)** | Guía completa paso a paso |
| **[EJEMPLOS_API.md](./EJEMPLOS_API.md)** | Ejemplos de cURL y testing |
| **[Analisis_ConceptoPrincipal.md](./Analisis_ConceptoPrincipal.md)** | Análisis del modelo de datos |

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│           QAPI FacturacionMasiva (Spring Boot)              │
├─────────────────────────────────────────────────────────────┤
│  Presentation Layer                                         │
│  ├─ FacturacionMasivaController (POST endpoints)            │
│  └─ HomeController                                          │
├─────────────────────────────────────────────────────────────┤
│  Business Logic                                             │
│  ├─ ProcesarRequestService                                  │
│  └─ S3Service                                               │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure                                             │
│  ├─ SqsSender → AWS SQS FIFO (Encolamiento)                 │
│  └─ InfoRequestService → AWS DynamoDB (Persistencia)        │
├─────────────────────────────────────────────────────────────┤
│  Configuration                                              │
│  ├─ SecurityConfig (OAuth2 Keycloak)                        │
│  └─ SwaggerConfig (OpenAPI 3.0)                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Requisitos

- ✅ **Java 17+** → `java -version`
- ✅ **Maven 3.6+** → `mvn -version` (o incluido ./mvnw)
- ✅ **Git** (para clonar)
- ✅ **AWS Credentials** (para SQS/DynamoDB)

---

## 📊 Flujo de Ejecución

```
1. Cliente envia solicitud POST
        ↓
2. FacturacionMasivaController recibe
        ↓
3. ProcesarRequestService procesa
        ↓
4. SqsSender encolada en AWS SQS FIFO
        ↓
5. InfoRequestService persiste en DynamoDB
        ↓
6. Retorna ID de mensaje
```

---

## 🌍 Perfiles de Configuración

| Perfil | JWT Issuer | AWS Endpoint | Uso |
|--------|-----------|---|---|
| **dev** | iam.ia.ucaldas.nuvu.cc | localhost:4566 | Desarrollo local |
| **prod** | iam.ia.ucaldas.nuvu.cc | AWS (real) | Producción |

---

## 🔐 Autenticación

```bash
# Obtener token (Producción)
TOKEN=$(curl -s -X POST \
  "https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials' \
  -d 'client_id=apifactmasiva' \
  -d 'client_secret=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV' | jq -r '.access_token')

# Usar en peticiones
curl -H "Authorization: Bearer $TOKEN" ...
```

---

## 📦 Dependencias Principales

```
Spring Boot 3.4.2
├─ spring-boot-starter-web
├─ spring-security-oauth2-resource-server
├─ spring-cloud-aws (SQS, DynamoDB)
├─ jackson-databind (JSON)
├─ lombok (Anotaciones)
├─ jjwt (JWT)
└─ springdoc-openapi (Swagger/OpenAPI)
```

---

## 🧪 Testing

```bash
# Compilar con tests
./run.sh 4

# Solo tests
./run.sh 5

# Coverage
./mvnw test jacoco:report
```

---

## 🐳 Docker

```bash
# Build
docker build -t qapi-facturacion:1.1.1 .

# Run
docker run -p 8080:8080 \
  -e AWS_ACCESS_KEY_ID=tu_key \
  -e AWS_SECRET_ACCESS_KEY=tu_secret \
  -e SPRING_PROFILES_ACTIVE=dev \
  qapi-facturacion:1.1.1
```

---

## 🚢 Deploy a AWS ECS

```bash
cd uca-devs-facturacionmasiva/
bash cf-ecs.deploy.sh
```

---

## 📝 Estructura de Directorios

```
QAPI_FacturacionMasiva/
├── src/
│   ├── main/java/cc/nuvu/qapi/
│   │   ├── config/              (SecurityConfig, SwaggerConfig)
│   │   ├── controllers/         (REST Endpoints)
│   │   ├── dto/                 (Data Transfer Objects)
│   │   ├── model/               (ConceptoPrincipal, Tercero)
│   │   ├── service/             (Business Logic)
│   │   └── infraestructure/     (SQS, DynamoDB)
│   ├── main/resources/          (application-dev.yml, application-prod.yml)
│   └── test/
├── target/                      (Compilados)
├── pom.xml                      (Dependencias Maven)
├── run.sh                       (Script de ejecución)
├── dockerfile                   (Contenedor Docker)
├── GUIA_EJECUCION.md           (Documentación)
└── EJEMPLOS_API.md             (Ejemplos cURL)
```

---

## 🆘 Solución de Problemas

### Error: "Port 8080 already in use"
```bash
# Cambiar puerto
java -jar target/QAPI_FacturacionMasiva-1.1.1.jar --server.port=9090
```

### Error: "AWS Credentials not found"
```bash
export AWS_ACCESS_KEY_ID="tu_key"
export AWS_SECRET_ACCESS_KEY="tu_secret"
```

### Error: "Java version not compatible"
```bash
# Verificar Java 17
java -version
```

---

## 📞 Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/facturacion-masiva/nota-debito` | Generar Nota de Débito (Batch) |
| `POST` | `/facturacion-masiva/factura` | Generar Factura |
| `POST` | `/facturacion-masiva/nota-credito` | Generar Nota de Crédito |
| `POST` | `/facturacion-masiva/pago` | Registrar Pago |
| `GET` | `/actuator/health` | Health Check |
| `GET` | `/docs` | Swagger UI |
| `GET` | `/api-docs` | OpenAPI JSON |

---

## 🎯 Checklist de Verificación

- [ ] Java 17 instalado
- [ ] Proyecto compilado (`./run.sh 2`)
- [ ] Aplicación iniciada en puerto 8080
- [ ] Health Check responde OK
- [ ] Swagger UI accesible
- [ ] Credenciales AWS configuradas (si usas SQS)
- [ ] Tests pasando

---

## 📈 Próximos Pasos

1. **Leer documentación completa:** [GUIA_EJECUCION.md](./GUIA_EJECUCION.md)
2. **Ver ejemplos de API:** [EJEMPLOS_API.md](./EJEMPLOS_API.md)
3. **Configurar AWS:** Credenciales y permisos SQS/DynamoDB
4. **Desarrollar:** Agregar más endpoints o funcionalidad
5. **Deploy:** Usar Docker o AWS ECS CloudFormation

---

## 📞 Contacto & Soporte

- **Versión:** 1.1.1
- **Java:** 17+
- **Spring Boot:** 3.4.2
- **Última actualización:** 13 de noviembre de 2025

---

**¡Listo! 🎉 Tu sistema está configurado y listo para ejecutarse.**

