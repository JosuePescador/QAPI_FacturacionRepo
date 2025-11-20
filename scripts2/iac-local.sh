#!/bin/bash
## 2. IaC local (Terraform sobre LocalStack)
set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}▶ Aplicando IaC local (Terraform → LocalStack)...${NC}"

cd "$BASE_DIR/infra/terraform"

echo -e "${YELLOW}→ terraform init...${NC}"
terraform init -input=false

echo -e "${YELLOW}→ terraform apply...${NC}"
terraform apply -input=false -auto-approve

echo -e "${GREEN}✓ IaC local aplicada (SQS, Dynamo, S3 en LocalStack)${NC}"
