terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region                      = var.aws_region
  access_key                  = "test"
  secret_key                  = "test"

  s3_use_path_style           = true
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    sqs      = "http://localhost:4566"
    dynamodb = "http://localhost:4566"
    s3       = "http://localhost:4566"
  }
}

########################
# 1. Cola SQS FIFO
########################
resource "aws_sqs_queue" "factmasiva_fifo" {
  name                          = var.queue_name
  fifo_queue                    = true
  content_based_deduplication   = true

  tags = {
    System = "QAPI_FacturacionMasiva"
    Env    = var.env
  }
}

########################
# 2. Tabla DynamoDB InfoRequest
########################
resource "aws_dynamodb_table" "info_request" {
  name         = var.info_request_table_name
  billing_mode = "PROVISIONED"

  read_capacity  = 5
  write_capacity = 5

  hash_key = "id"

  attribute {
    name = "id"
    type = "S"
  }

  stream_enabled   = true
  stream_view_type = "NEW_AND_OLD_IMAGES"

  tags = {
    System = "QAPI_FacturacionMasiva"
    Env    = var.env
  }
}

########################
# 3. Tabla DynamoDB InfoRequestProcessing
########################
resource "aws_dynamodb_table" "info_request_processing" {
  name         = var.info_request_processing_table_name
  billing_mode = "PROVISIONED"

  read_capacity  = 5
  write_capacity = 5

  hash_key = "messageId"

  attribute {
    name = "messageId"
    type = "S"
  }

  tags = {
    System = "QAPI_FacturacionMasiva"
    Env    = var.env
  }
}

########################
# 4. Bucket S3 Auditoría
########################
resource "aws_s3_bucket" "auditory" {
  bucket        = var.auditory_bucket_name
  force_destroy = true

  tags = {
    System = "QAPI_FacturacionMasiva"
    Env    = var.env
  }
}
