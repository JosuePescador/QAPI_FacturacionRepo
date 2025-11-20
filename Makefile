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

export AWS_ACCESS_KEY_ID=fakeAccessKey123
export AWS_SECRET_ACCESS_KEY=fakeSecretKey456
export AWS_SESSION_TOKEN=fakeSession789
export AWS_DEFAULT_REGION=us-east-1

# ============================================
# TARGETS PRINCIPALES
# ============================================

.PHONY: all infra build deploy run test stop clean logs dynamo1 dynamo2 sqs

# 🚀 Pipeline completo
all: infra build deploy run test

# --------------------------------------------
# 1️⃣ Infraestructura (LocalStack)
# --------------------------------------------
infra:
	@echo "🔥 [1/5] Levantando infraestructura LocalStack..."
	@chmod +x $(INFRA_SCRIPT)
	@$(INFRA_SCRIPT)

# --------------------------------------------
# 2️⃣ Build completo
# --------------------------------------------
build:
	@echo "🔧 [2/5] Compilando servicios Java..."
	@chmod +x $(BUILD_SCRIPT)
	@$(BUILD_SCRIPT)

# --------------------------------------------
# 3️⃣ Crear colas/tables/config
# --------------------------------------------
deploy:
	@echo "📦 [3/5] Deploy local (colas, tablas Dynamo, etc.)..."
	@chmod +x $(DEPLOY_SCRIPT)
	@$(DEPLOY_SCRIPT)

# --------------------------------------------
# 4️⃣ Ejecutar servicios
# --------------------------------------------
run:
	@echo "🚀 [4/5] Ejecutando QAPI (masiva + worker)..."
	@chmod +x $(RUN_SCRIPT)
	@$(RUN_SCRIPT)
	@echo "✔ Servicios ejecutados correctamente"

# --------------------------------------------
# 5️⃣ Tests E2E
# --------------------------------------------
test:
	@echo "🧪 [5/5] Ejecutando pruebas End-To-End..."
	@chmod +x $(TEST_SCRIPT)
	@$(TEST_SCRIPT)

# ============================================
# UTILIDADES
# ============================================

logs:
	@echo "📄 Logs de Masiva (amarillo):"
	@echo ""
	@tail -f QAPI_masiva/app.log | sed 's/^/\x1b[33m[MASIVA]\x1b[0m /' & \
	tail -f QAPI_worker/app.log | sed 's/^/\x1b[36m[WORKER]\x1b[0m /'



dynamo1:
	aws --endpoint-url=http://localhost:4566 --region us-east-1 dynamodb scan --table-name InfoRequest | jq '.Items'

dynamo2:
	aws --endpoint-url=http://localhost:4566 --region us-east-1 dynamodb scan --table-name InfoRequestProcessing | jq '.Items'

sqs:
	aws --endpoint-url=http://localhost:4566 sqs list-queues | jq

# ============================================
# STOP & CLEAN
# ============================================

stop:
	@echo "🛑 Deteniendo QAPI..."
	@if [ -f masiva.pid ]; then kill `cat masiva.pid` || true; rm masiva.pid; fi
	@if [ -f worker.pid ]; then kill `cat worker.pid` || true; rm worker.pid; fi
	@echo "🧹 Deteniendo LocalStack..."
	docker compose down || true


clean: stop
	@echo "🧼 Limpiando proyecto..."
	@rm -f masiva.log worker.log
	@rm -f masiva.pid worker.pid
	@echo "✔ Limpieza realizada."