variable "aws_region" {
  description = "Región AWS para LocalStack"
  type        = string
  default     = "us-east-1"
}

variable "env" {
  description = "Nombre del entorno (local, dev, etc.)"
  type        = string
  default     = "localstack-dev"
}

variable "queue_name" {
  description = "Nombre de la cola SQS FIFO"
  type        = string
  default     = "uca-test-factmasiva-sqs.fifo"
}

variable "info_request_table_name" {
  description = "Nombre de la tabla Dynamo InfoRequest"
  type        = string
  default     = "InfoRequest"
}

variable "info_request_processing_table_name" {
  description = "Nombre de la tabla Dynamo InfoRequestProcessing"
  type        = string
  default     = "InfoRequestProcessing"
}

variable "auditory_bucket_name" {
  description = "Nombre del bucket S3 de auditoría"
  type        = string
  default     = "uca-test-facturacionmasiva-auditory"
}
