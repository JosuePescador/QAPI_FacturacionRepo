# ============================================
# QAPI FACTURACIÓN MASIVA - MAKEFILE MAESTRO
# ============================================

SHELL=/bin/bash

# Paths
SCRIPTS_DIR=./scripts
BUILD_SCRIPT=$(SCRIPTS_DIR)/build/build.sh
INFRA_SCRIPT=$(SCRIPTS_DIR)/infra/infra.sh
DEPLOY_SCRIPT=$(SCRIPTS_DIR)/deploy/deploy-local.sh
RUN_SCRIPT=$(SCRIPTS_DIR)/run/run_worker.sh
TEST_SCRIPT=$(SCRIPTS_DIR)/test/test.sh

# AWS LocalStack
AWS = aws --endpoint-url=http://localhost:4566 --region us-east-1
export AWS_ACCESS_KEY_ID=fakeAccessKey123
export AWS_SECRET_ACCESS_KEY=fakeSecretKey456
export AWS_SESSION_TOKEN=fakeSession789
export AWS_DEFAULT_REGION=us-east-1

.PHONY: all infra build deploy run test verify stop clean logs dynamo1 dynamo2 sqs

# ============================================
# Pipeline
# ============================================

all: infra build deploy run test

# ============================================
# 1️⃣ Infraestructura (LocalStack)
# ============================================

infra: 
	@echo "🔥 [1/5] Levantando infraestructura LocalStack..."
	@chmod +x $(INFRA_SCRIPT)
	@$(INFRA_SCRIPT)

# ============================================
# 2️⃣ Build
# ============================================

build: 
	@echo "🔧 [2/5] Compilando servicios Java..."
	@chmod +x $(BUILD_SCRIPT)
	@$(BUILD_SCRIPT)

# ============================================
# 3️⃣ Deploy (Tablas, colas, lambdas)
# ============================================

deploy:
	@echo "📦 [3/5] Creando recursos (DynamoDB, SQS, Lambdas)..."
	@chmod +x $(DEPLOY_SCRIPT)
	@$(DEPLOY_SCRIPT)

# ============================================
# 4️⃣ Ejecutar servicios
# ============================================

run:
	@echo "🚀 [4/5] Ejecutando QAPI..."
	@chmod +x $(RUN_SCRIPT)
	@$(RUN_SCRIPT)
	@echo "✔ Servicios ejecutados correctamente"

# ============================================
# 5️⃣ Tests
# ============================================

test:
	@echo "🧪 [5/5] Ejecutando pruebas End-To-End..."
	@chmod +x $(TEST_SCRIPT)
	@$(TEST_SCRIPT)

# ============================================
# VERIFY — Usa después de deploy/run
# ============================================

verify:
	@echo "🔍 Verificando salud de LocalStack..."
	@curl -s http://localhost:4566/_localstack/health | jq '.services' 2>/dev/null || \
    echo "⚠️ LocalStack aún no listo o endpoint no disponible"


	@echo "\n📌 Verificando Dynamo1..."
	$(AWS) dynamodb describe-table --table-name InfoRequest >/dev/null 2>&1 && \
		echo "✔️ InfoRequest existe" || echo "❌ No existe"

	@echo "\n📝 Insertando item de prueba..."
	$(AWS) dynamodb put-item \
		--table-name InfoRequest \
		--item '{"id": {"S":"test-123"}, "payload": {"S":"hello"}}'

	@echo "\n🔎 Leyendo item..."
	$(AWS) dynamodb get-item \
		--table-name InfoRequest \
		--key '{"id": {"S":"test-123"}}' | jq '.Item'

	@echo "\n📨 Colas SQS:"
	$(AWS) sqs list-queues | jq '.QueueUrls'

	@echo "\n📬 Consumir mensaje de la cola..."
	@QUEUE_URL=`$(AWS) sqs list-queues --query "QueueUrls[0]" --output text`; \
	$(AWS) sqs receive-message --queue-url $$QUEUE_URL | jq '.Messages'

	@echo "\n📗 Verificando tabla procesamiento (InfoRequestProcessing)..."
	$(AWS) dynamodb scan --table-name InfoRequestProcessing | jq '.Items'

	@echo "\n🎉 VERIFICACIÓN COMPLETA"

# ============================================
# UTILIDADES
# ============================================

logs:
	tail -f QAPI_masiva/app.log QAPI_worker/app.log

dynamo1:
	$(AWS) dynamodb scan --table-name InfoRequest | jq '.Items'

dynamo2:
	$(AWS) dynamodb scan --table-name InfoRequestProcessing | jq '.Items'

sqs:
	$(AWS) sqs list-queues | jq

# ============================================
# STOP & CLEAN
# ============================================

stop:
	@echo "🛑 Deteniendo servicios..."
	@if [ -f masiva.pid ]; then kill `cat masiva.pid` || true; rm masiva.pid; fi
	@if [ -f worker.pid ]; then kill `cat worker.pid` || true; rm worker.pid; fi
	docker compose down || true

clean: stop
	@echo "🧼 Limpiando archivos..."
	@rm -f masiva.log worker.log masiva.pid worker.pid
	@echo "✔ Limpieza completa"
