output "sqs_queue_url" {
  value       = aws_sqs_queue.factmasiva_fifo.url
  description = "URL de la cola SQS FIFO de facturación masiva"
}

output "info_request_table" {
  value       = aws_dynamodb_table.info_request.name
  description = "Nombre de la tabla Dynamo InfoRequest"
}

output "info_request_processing_table" {
  value       = aws_dynamodb_table.info_request_processing.name
  description = "Nombre de la tabla Dynamo InfoRequestProcessing"
}

output "auditory_bucket_name" {
  value       = aws_s3_bucket.auditory.bucket
  description = "Nombre del bucket S3 de auditoría"
}
