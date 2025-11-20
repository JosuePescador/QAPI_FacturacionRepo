#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
LOG_DIR="$ROOT/logs"
JAR="$ROOT/QAPI_worker/target/QAPI_FacturacionMasivaWorker-1.1.0.jar"

mkdir -p "$LOG_DIR"

echo "[RUN] Iniciando QAPI_worker en 8081..."

nohup java -jar "$JAR" \
  --server.port=8081 \
  --spring.profiles.active=dev \
  > "$LOG_DIR/QAPI_worker.log" 2>&1 &
#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
LOG_DIR="$ROOT/logs"
JAR="$ROOT/QAPI_worker/target/QAPI_FacturacionMasivaWorker-1.1.0.jar"

mkdir -p "$LOG_DIR"

echo "[RUN] Iniciando QAPI_worker en 8081..."

nohup java -jar "$JAR" \
  --server.port=8081 \
  --spring.profiles.active=dev \
  > "$LOG_DIR/QAPI_worker.log" 2>&1 &
