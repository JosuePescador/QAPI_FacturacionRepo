# QAPI Facturación Masiva – Entorno Local con LocalStack

Este repositorio contiene el entorno **local** para probar la solución de **Facturación Masiva** de QAPI, compuesta por:

- **QAPI_masiva**: API REST que expone los endpoints de facturación masiva.
- **QAPI_worker**: servicio worker que consume mensajes de SQS, orquesta el procesamiento (Oracle, DynamoDB, etc.) y actualiza el estado de las solicitudes.

El objetivo de este setup es poder levantar **toda la arquitectura en local** usando:

- **Docker + LocalStack** para emular servicios de AWS (SQS, DynamoDB, S3).
- **Terraform** para provisionar la infraestructura en LocalStack.
- **Maven en Docker** (JDK 17) para compilar los proyectos sin depender de Java en la máquina host.
- Scripts de shell para orquestar todo el flujo: **infra → IaC → build → deploy → test E2E**.

---

## 1. Estructura del repositorio

```text
QAPI_FacturacionRepo/
├── QAPI_masiva/                # API REST de facturación masiva (Spring Boot)
├── QAPI_worker/                # Worker de procesamiento (Spring Boot)
├── infra/
│   └── terraform/              # IaC local contra LocalStack (SQS, Dynamo, S3)
├── localstack-data/            # Datos/cache de LocalStack (volúmenes)
├── scripts/
│   ├── infra-local.sh          # Paso 1: Levantar infraestructura Docker (LocalStack)
│   ├── iac-local.sh            # Paso 2: Terraform init/apply sobre LocalStack
│   ├── build-local-docker.sh   # Paso 3: Build de QAPI_masiva y QAPI_worker en Docker
│   ├── deploy-local.sh         # Paso 4: Levantar los servicios (java -jar)
│   └── test/
│       ├── test-e2e.sh         # Paso 5: Prueba End-to-End completa
│       ├── test-local.sh       # Otros tests locales
│       └── test-simple.sh      # Test simple
├── docker-compose.yml          # LocalStack (SQS, DynamoDB, S3, etc.)
├── init-localstack.sh          # Script auxiliar para LocalStack
├── SETUP_LOCAL.md              # Guía detallada original de setup local
└── setup.sh                    # Pipeline completo (infra + IaC + build + deploy + test)
````
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
Dentro de cada módulo:

* `QAPI_masiva/`:

  * `pom.xml`, `src/main/java/...`: código de la API.
  * `application-*.yml`: configuración por perfil.
  * `lambda/lambda.py`: código relacionado con lambdas originales.
  * `target/QAPI_FacturacionMasiva-1.1.1.jar`: artefacto generado.

* `QAPI_worker/`:

  * `pom.xml`, `src/main/java/...`: código del worker.
  * Usa:

    * Oracle como fuente de datos (`infraestructure/Oracle/...`).
    * DynamoDB (`infraestructure/dynamoDB/...`).
    * SQS (`presentation/listener/SqsQAPIListener.java`).
  * `target/QAPI_FacturacionMasivaWorker-1.1.0.jar`: artefacto generado.

---

## 2. Requisitos previos

Para ejecutar el entorno local necesitas:

* **Docker** y **docker-compose** funcionando.
* **Terraform** (v1.x).
* **bash** (los scripts están escritos para bash).
* Opcional pero recomendable:

  * **AWS CLI** o `awslocal` para inspeccionar SQS/Dynamo/S3 en LocalStack.

> 💡 **No es obligatorio tener Java 17 ni Maven instalados en el host.**
> El build se hace con la imagen Docker `maven:3.9-eclipse-temurin-17`.

---

## 3. Setup rápido (todo en un solo comando)

Desde la raíz del repositorio (`QAPI_FacturacionRepo`):

```bash
./setup.sh
```

Este script orquesta 5 pasos:

### Paso 1 – Infraestructura local (Docker + LocalStack)

* Levanta el contenedor `localstack-qapi` usando `docker-compose.yml`.
* Espera a que LocalStack responda en `http://localhost:4566`.

### Paso 2 – IaC local (Terraform → LocalStack)

* Ejecuta `terraform init` y `terraform apply` en `infra/terraform`.
* Crea los recursos en LocalStack:

  * **SQS**: `uca-test-factmasiva-sqs.fifo`
  * **DynamoDB**:

    * `InfoRequest`
    * `InfoRequestProcessing`
  * **S3**:

    * `uca-test-facturacionmasiva-auditory`

### Paso 3 – Build local (Docker + Maven 17)

Usa `scripts/build-local-docker.sh`:

* Monta `QAPI_masiva` y `QAPI_worker` en contenedores:

  * Imagen: `maven:3.9-eclipse-temurin-17`
  * Comandos:

    * `mvn clean package -DskipTests` en `QAPI_masiva/`
    * `mvn clean package -DskipTests` en `QAPI_worker/`
* Resultado:

  * `QAPI_masiva/target/QAPI_FacturacionMasiva-1.1.1.jar`
  * `QAPI_worker/target/QAPI_FacturacionMasivaWorker-1.1.0.jar`

### Paso 4 – Deploy local (lanzar servicios)

Usa `scripts/deploy-local.sh`:

* Levanta los servicios en segundo plano:

  * `QAPI_FacturacionMasiva` en `http://localhost:8080`
  * `QAPI_FacturacionMasivaWorker` en `http://localhost:8081`
* Registra logs en:

  * `log-masiva.out`
  * `log-worker.out`

Para verificar a mano:

```bash
curl http://localhost:8080/actuator/health   # API principal
# el worker responde 404 en /actuator/health, pero el proceso sí está arriba
```

Para detenerlos (desde la raíz del repo):

```bash
ps aux | grep QAPI_FacturacionMasiva | grep -v grep | awk '{print $2}' | xargs kill
ps aux | grep QAPI_FacturacionMasivaWorker | grep -v grep | awk '{print $2}' | xargs kill
```

### Paso 5 – Test End-to-End

Usa `scripts/test/test-e2e.sh`:

* Verifica que:

  * LocalStack está arriba.
  * QAPI_masiva responde en `8080`.
  * QAPI_worker responde en `8081`.
* Obtiene un token JWT.
* Envía una solicitud de facturación masiva a la API.
* Recibe un `HTTP 202` con un `messageId` generado.

Ejemplo de respuesta:

```json
{
  "messageIds": [
    "e8e4df67-0b85-4c12-b6ab-f7c2601ada24"
  ],
  "count": 1,
  "status": "ACCEPTED"
}
```

* Luego, el script continúa verificando el mensaje en la cola SQS en LocalStack.

---

## 4. Flujo funcional simplificado

De forma simplificada, el flujo end-to-end es:

1. **Cliente → QAPI_masiva**

   * Se envía una solicitud de facturación masiva a la API (p.ej. `POST /qapi/facturacionMasiva`).
   * La API:

     * Almacena la solicitud en la tabla `InfoRequest` (DynamoDB).
     * Envía un mensaje a la cola FIFO `uca-test-factmasiva-sqs.fifo` (SQS).
     * Devuelve `202 ACCEPTED` con uno o varios `messageIds`.

2. **QAPI_worker → SQS + Oracle + DynamoDB**

   * El worker escucha la cola SQS (`SqsQAPIListener`).
   * Por cada mensaje:

     * Lee la solicitud desde `InfoRequest` (DynamoDB).
     * Llama a procedimientos en Oracle (`OraclePackageService`, `ProcedureCallerImpl`).
     * Registra el resultado/estado en `InfoRequestProcessing` (DynamoDB).
     * Puede generar información de auditoría en S3 (`uca-test-facturacionmasiva-auditory`).

3. **Consulta / auditoría**

   * El estado de las solicitudes puede consultarse desde DynamoDB.
   * Los archivos de auditoría quedan en S3 (en LocalStack, emulado en local).

---

## 5. Comandos útiles

### Levantar todo desde cero

```bash
./setup.sh
```

### Solo infraestructura (Docker + Terraform)

```bash
./scripts/infra-local.sh   # LocalStack
./scripts/iac-local.sh     # Terraform init + apply
```

### Solo build (Docker + Maven 17)

```bash
./scripts/build-local-docker.sh
```

### Solo deploy (levantar jars)

```bash
./scripts/deploy-local.sh
```

### Tests

```bash
./scripts/test/test-e2e.sh      # flujo completo
./scripts/test/test-local.sh    # otros tests locales
./scripts/test/test-simple.sh   # test sencillo
```

---

## 6. Notas técnicas

* El build usa **Java 17** dentro del contenedor (`maven:3.9-eclipse-temurin-17`), por eso **no hace falta configurar JAVA_HOME** en el host.
* LocalStack guarda su estado en `localstack-data/`, por lo que la infraestructura se mantiene entre ejecuciones mientras no se borre esa carpeta.
* Terraform usa `infra/terraform/terraform.tfstate` para rastrear los recursos aplicados en LocalStack.


