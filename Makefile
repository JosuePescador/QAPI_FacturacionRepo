# ============================================
# QAPI FACTURACIÓN MASIVA - MAKEFILE MAESTRO
# ============================================

SHELL := /bin/bash

# ============================================
# Paths
# ============================================

BASE_DIR := $(shell pwd)
SCRIPTS_DIR := $(BASE_DIR)/scripts
TEST_DIR := $(SCRIPTS_DIR)/test

INFRA_SCRIPT := $(SCRIPTS_DIR)/infra-local.sh
IAC_SCRIPT := $(SCRIPTS_DIR)/iac-local.sh
BUILD_SCRIPT := $(SCRIPTS_DIR)/build-local-docker.sh
DEPLOY_SCRIPT := $(SCRIPTS_DIR)/deploy-local.sh
TEST_LOCAL_SCRIPT := $(TEST_DIR)/test-e2e.sh


# ============================================
# AWS LocalStack
# ============================================

AWS := aws --endpoint-url=http://localhost:4566 --region us-east-1

export AWS_ACCESS_KEY_ID := fakeAccessKey123
export AWS_SECRET_ACCESS_KEY := fakeSecretKey456
export AWS_SESSION_TOKEN := fakeSession789
export AWS_DEFAULT_REGION := us-east-1

.PHONY: all setup infra iac build deploy run test test-e2e verify \
        logs dynamo1 dynamo2 sqs stop clean

# ============================================
# Pipeline principal
# ============================================

## Pipeline completo estilo setup.sh
all: setup

## Alias explícito del setup local completo
setup: infra iac build deploy test
	@echo "✅ Setup completo (infra + IaC + build + deploy + tests)"

# ============================================
# 1️⃣ Infraestructura local (Docker + LocalStack)
# ============================================

infra:
	@echo "🔥 [1/5] Infraestructura local (Docker + LocalStack)..."
	@chmod +x $(INFRA_SCRIPT)
	@$(INFRA_SCRIPT)

# ============================================
# 2️⃣ IaC local (Terraform sobre LocalStack)
# ============================================

iac:
	@echo "🧱 [2/5] IaC local (Terraform sobre LocalStack)..."
	@chmod +x $(IAC_SCRIPT)
	@$(IAC_SCRIPT)

# ============================================
# 3️⃣ Build local (QAPI_masiva + QAPI_worker)
# ============================================

build:
	@echo "🔧 [3/5] Build local (QAPI_masiva + QAPI_worker)..."
	@chmod +x $(BUILD_SCRIPT)
	@$(BUILD_SCRIPT)

# ============================================
# 4️⃣ Deploy local (lanzar servicios)
# ============================================

deploy:
	@echo "🚀 [4/5] Deploy local (lanzar servicios)..."
	@chmod +x $(DEPLOY_SCRIPT)
	@$(DEPLOY_SCRIPT)
	@echo "✔ Servicios desplegados correctamente"

# Alias por compatibilidad con el Makefile viejo
run: deploy

# ============================================
# 5️⃣ Tests
# ============================================

test:
	@echo "🧪 [5/5] Tests locales (E2E)..."
	@chmod +x $(TEST_LOCAL_SCRIPT)
	@$(TEST_LOCAL_SCRIPT)

# ============================================
# VERIFY — Usa después de deploy/run
# ============================================

verify:
	@echo "🔍 Verificando salud de LocalStack..."
	@curl -s http://localhost:4566/health | jq '.services' || true

	@echo "\n📌 Verificando tabla InfoRequest..."
	@$(AWS) dynamodb describe-table --table-name InfoRequest >/dev/null 2>&1 && \
		echo "✔️ InfoRequest existe" || echo "❌ InfoRequest NO existe"

	@echo "\n📝 Insertando item de prueba en InfoRequest..."
	@$(AWS) dynamodb put-item \
		--table-name InfoRequest \
		--item '{"id": {"S":"test-123"}, "payload": {"S":"hello"}}'

	@echo "\n🔎 Leyendo item de InfoRequest..."
	@$(AWS) dynamodb get-item \
		--table-name InfoRequest \
		--key '{"id": {"S":"test-123"}}' | jq '.Item'

	@echo "\n📨 Listando colas SQS..."
	@$(AWS) sqs list-queues | jq '.QueueUrls'

	@echo "\n📬 Consumir mensaje de la primera cola..."
	@QUEUE_URL=`$(AWS) sqs list-queues --query "QueueUrls[0]" --output text`; \
	if [ "$$QUEUE_URL" != "None" ]; then \
		$(AWS) sqs receive-message --queue-url $$QUEUE_URL | jq '.Messages'; \
	else \
		echo "⚠️ No hay colas SQS definidas"; \
	fi

	@echo "\n📗 Verificando tabla InfoRequestProcessing..."
	@$(AWS) dynamodb scan --table-name InfoRequestProcessing | jq '.Items'

	@echo "\n🎉 VERIFICACIÓN COMPLETA"

# ============================================
# UTILIDADES
# ============================================

logs:
	@echo "📜 Siguiendo logs de QAPI..."
	@tail -f QAPI_masiva/app.log QAPI_worker/app.log

dynamo1:
	@echo "📗 Contenido de tabla InfoRequest..."
	@$(AWS) dynamodb scan --table-name InfoRequest | jq '.Items'

dynamo2:
	@echo "📘 Contenido de tabla InfoRequestProcessing..."
	@$(AWS) dynamodb scan --table-name InfoRequestProcessing | jq '.Items'

sqs:
	@echo "📨 Colas SQS..."
	@$(AWS) sqs list-queues | jq

# ============================================
# STOP & CLEAN
# ============================================

stop:
	@echo "🛑 Deteniendo servicios..."
	@if [ -f masiva.pid ]; then kill `cat masiva.pid` || true; rm masiva.pid; fi
	@if [ -f worker.pid ]; then kill `cat worker.pid` || true; rm worker.pid; fi
	@docker compose down || true

clean: stop
	@echo "🧼 Limpiando archivos temporales..."
	@rm -f masiva.log worker.log masiva.pid worker.pid
	@echo "✔ Limpieza completa"
