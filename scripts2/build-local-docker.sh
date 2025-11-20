#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MVN_IMAGE="maven:3.9-eclipse-temurin-17"

docker_maven() {
  local module="$1"
  echo "→ Compilando módulo ${module} con Maven en Docker..."

  docker run --rm \
    -v "${ROOT_DIR}/${module}":/app \
    -v "${HOME}/.m2":/root/.m2 \
    -w /app \
    "${MVN_IMAGE}" \
    mvn clean package -DskipTests
}

docker_maven "QAPI_masiva"
docker_maven "QAPI_worker"
