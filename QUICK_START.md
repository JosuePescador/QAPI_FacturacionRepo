# 🚀 Inicio Rápido - QAPI Local

## ⚡ Opción 1: Script Automático (Recomendado)

```bash
cd /home/adminblend/Escritorio/Blend/ucaldas/QAPI_FacturacionRepo
./setup.sh
```

Luego sigue las instrucciones en pantalla.

---

## 📝 Opción 2: Paso a Paso Manual

### 1️⃣ Iniciar LocalStack

```bash
docker-compose up -d
./init-localstack.sh
```

### 2️⃣ Compilar proyectos

```bash
cd QAPI_masiva && mvn clean package -DskipTests && cd ..
cd QAPI_worker && mvn clean package -DskipTests && cd ..
```

### 3️⃣ Ejecutar aplicaciones (en terminales separadas)

**Terminal 1:**
```bash
cd QAPI_masiva
java -jar target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev
```

**Terminal 2:**
```bash
cd QAPI_worker
java -jar target/QAPI_FacturacionMasivaWorker-1.1.0.jar --spring.profiles.active=dev
```

### 4️⃣ Ejecutar prueba

**Terminal 3:**
```bash
./test-e2e.sh
```

---

## 🔍 Verificar Datos en DynamoDB

```bash
# DynamoDB #1 (InfoRequest - QAPI_masiva)
aws --endpoint-url=http://localhost:4566 dynamodb scan \
    --table-name InfoRequest | jq '.Items'

# DynamoDB #2 (InfoRequestProcessing - QAPI_worker)
aws --endpoint-url=http://localhost:4566 dynamodb scan \
    --table-name InfoRequestProcessing | jq '.Items'
```

---

## 🌐 URLs Importantes

- **QAPI_masiva**: http://localhost:8080
- **QAPI_worker**: http://localhost:8081
- **Swagger**: http://localhost:8080/docs
- **LocalStack**: http://localhost:4566

---

## 📚 Documentación Completa

Ver: [SETUP_LOCAL.md](./SETUP_LOCAL.md)
