# ================================
# QAPI LOCAL - Makefile Maestro
# ================================

# Variables
MASIVA_DIR=QAPI_masiva
WORKER_DIR=QAPI_worker

MASIVA_JAR=$(MASIVA_DIR)/target/QAPI_FacturacionMasiva-1.1.1.jar
WORKER_JAR=$(WORKER_DIR)/target/QAPI_FacturacionMasivaWorker-1.1.0.jar

# ============
# TARGETS
# ============

.PHONY: all localstack build run test e2e dynamo1 dynamo2 clean stop

# 🚀 Ejecuta TODO el pipeline
all: localstack build run test

# ======================
# 1️⃣ LocalStack
# ======================
localstack:
	@echo "🔥 Levantando LocalStack..."
	docker compose up -d
	./init-localstack.sh

# ======================
# 2️⃣ Compilar proyectos
# ======================
build:
	@echo "🔧 Compilando QAPI_masiva..."
	cd $(MASIVA_DIR) && mvn clean package -DskipTests
	@echo "🔧 Compilando QAPI_worker..."
	cd $(WORKER_DIR) && mvn clean package -DskipTests

# ======================
# 3️⃣ Ejecutar JARs
# ======================
run:
	@echo "🚀 Ejecutando QAPI_masiva..."
	nohup java -jar $(MASIVA_JAR) --spring.profiles.active=dev > masiva.log 2>&1 & echo $$! > masiva.pid
	@echo "🚀 Ejecutando QAPI_worker..."
	nohup java -jar $(WORKER_JAR) --spring.profiles.active=dev > worker.log 2>&1 & echo $$! > worker.pid
	@echo "✔ Servicios ejecutándose en background."
	@echo "   - Masiva: PID `cat masiva.pid`"
	@echo "   - Worker: PID `cat worker.pid`"

# ======================
# 4️⃣ Ejecutar pruebas
# ======================
test e2e:
	@echo "🧪 Ejecutando prueba E2E..."
	./test-e2e.sh

# ======================
# DynamoDB Helpers
# ======================
dynamo1:
	aws --endpoint-url=http://localhost:4566 dynamodb scan --table-name InfoRequest | jq '.Items'

dynamo2:
	aws --endpoint-url=http://localhost:4566 dynamodb scan --table-name InfoRequestProcessing | jq '.Items'

# ======================
# Parar servicios
# ======================
stop:
	@echo "🛑 Finalizando procesos..."
	@if [ -f masiva.pid ]; then kill `cat masiva.pid` || true; rm masiva.pid; fi
	@if [ -f worker.pid ]; then kill `cat worker.pid` || true; rm worker.pid; fi
	@echo "🧹 Deteniendo LocalStack..."
	docker-compose down

# ======================
# Limpieza completa
# ======================
clean: stop
	@echo "🧼 Borrando logs y builds..."
	rm -f masiva.log worker.log
	cd $(MASIVA_DIR) && mvn clean
	cd $(WORKER_DIR) && mvn clean
