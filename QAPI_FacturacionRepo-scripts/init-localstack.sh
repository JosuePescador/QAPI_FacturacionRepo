#!/bin/bash

################################################################################
# Script de Inicialización de LocalStack para QAPI
# Crea las colas SQS, tablas DynamoDB y buckets S3 necesarios
#YA NO SE USA 
################################################################################

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configurar AWS CLI para LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

# 1. Crear Cola SQS FIFO
echo -e "${YELLOW}→${NC} Creando cola SQS FIFO..."
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name uca-test-factmasiva-sqs.fifo \
    --attributes FifoQueue=true,ContentBasedDeduplication=true \
    --region us-east-1 2>/dev/null || echo "  Cola ya existe"
echo -e "${GREEN}✓${NC} Cola SQS creada: uca-test-factmasiva-sqs.fifo"

# Obtener URL de la cola
QUEUE_URL=$(aws --endpoint-url=http://localhost:4566 sqs get-queue-url \
    --queue-name uca-test-factmasiva-sqs.fifo \
    --region us-east-1 --output text 2>/dev/null)
echo -e "${BLUE}ℹ${NC} URL de la cola: $QUEUE_URL"
echo ""

# 2. Crear Tabla DynamoDB #1 (para QAPI_masiva - Info Request)
echo -e "${YELLOW}→${NC} Creando tabla DynamoDB #1 (InfoRequest)..."
aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name InfoRequest \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --stream-specification \
        StreamEnabled=true,StreamViewType=NEW_AND_OLD_IMAGES \
    --region us-east-1 2>/dev/null || echo "  Tabla ya existe"
echo -e "${GREEN}✓${NC} Tabla DynamoDB creada: InfoRequest"
echo ""

# 3. Crear Tabla DynamoDB #2 (para QAPI_worker - Estado Procesamiento)
echo -e "${YELLOW}→${NC} Creando tabla DynamoDB #2 (InfoRequestProcessing)..."
aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name InfoRequestProcessing \
    --attribute-definitions \
        AttributeName=messageId,AttributeType=S \
    --key-schema \
        AttributeName=messageId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --region us-east-1 2>/dev/null || echo "  Tabla ya existe"
echo -e "${GREEN}✓${NC} Tabla DynamoDB creada: InfoRequestProcessing"
echo ""

# 4. Crear Bucket S3 (para auditoría)
echo -e "${YELLOW}→${NC} Creando bucket S3..."
aws --endpoint-url=http://localhost:4566 s3 mb \
    s3://uca-test-facturacionmasiva-auditory \
    --region us-east-1 2>/dev/null || echo "  Bucket ya existe"
echo -e "${GREEN}✓${NC} Bucket S3 creado: uca-test-facturacionmasiva-auditory"
echo ""

# 5. Listar recursos creados
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Recursos creados en LocalStack                           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${YELLOW}Colas SQS:${NC}"
aws --endpoint-url=http://localhost:4566 sqs list-queues --region us-east-1
echo ""

echo -e "${YELLOW}Tablas DynamoDB:${NC}"
aws --endpoint-url=http://localhost:4566 dynamodb list-tables --region us-east-1
echo ""

echo -e "${YELLOW}Buckets S3:${NC}"
aws --endpoint-url=http://localhost:4566 s3 ls
echo ""

echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ LocalStack inicializado correctamente                  ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
