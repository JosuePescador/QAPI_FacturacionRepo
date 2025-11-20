# Guía de Migración: AWS SDK v2 → AWS Spring Cloud

## Resumen de Cambios

Este documento describe la migración del proyecto de AWS SDK v2 directo a **AWS Spring Cloud** (awspring), que proporciona integración nativa con Spring Boot para servicios AWS.

## Cambios Principales

### 1. Arquitectura

**ANTES**:
- Polling manual con `@Scheduled` cada 5 segundos
- Consulta activa a SQS usando `SqsClient`
- Logs almacenados en S3 como archivos JSON
- Gestión manual de visibility timeout y eliminación de mensajes

**DESPUÉS**:
- Event-driven con `@SqsListener`
- Spring gestiona automáticamente el polling, acknowledgement y reintentos
- Logs almacenados en DynamoDB con TTL automático
- Configuración declarativa simplificada

### 2. Dependencias (pom.xml)

#### Eliminadas
```xml
<!-- AWS SDK v2 - SQS -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sqs</artifactId>
    <version>2.31.69</version>
</dependency>

<!-- AWS SDK v2 - S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.31.69</version>
</dependency>
```

#### Agregadas
```xml
<!-- AWS Spring Cloud - SQS -->
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-sqs</artifactId>
    <version>3.2.1</version>
</dependency>

<!-- AWS Spring Cloud - DynamoDB -->
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-dynamodb</artifactId>
    <version>3.2.1</version>
</dependency>
```

### 3. Nuevos Componentes

#### LogEntry.java (Entidad DynamoDB)
- Reemplaza archivos JSON en S3
- Partition Key: `procedure` (FACTURA, NOTA-DEBITO, etc.)
- Sort Key: `timestamp` (ISO-8601)
- TTL automático de 90 días

**Ubicación**: `src/main/java/cc/nuvu/qapi/model/LogEntry.java`

#### LogEntryRepository.java
- Repositorio para operaciones CRUD en DynamoDB
- Métodos: `save()`, `findByProcedure()`, `findByProcedureAndTimestamp()`

**Ubicación**: `src/main/java/cc/nuvu/qapi/repository/LogEntryRepository.java`

#### SqsMessageListener.java
- Reemplaza el `ScheduledWorker` para procesamiento de mensajes
- Usa `@SqsListener` para escuchar mensajes de forma event-driven
- Acknowledgement manual para control fino de procesamiento
- Listener adicional para monitoreo de DLQ

**Ubicación**: `src/main/java/cc/nuvu/qapi/listener/SqsMessageListener.java`

### 4. Componentes Modificados

#### LoggerServiceData.java
**ANTES**:
```java
public void logToS3(String procedure, String jsonBody, boolean success, String errorMessage) {
    // Guardaba en S3 usando S3Client
    s3Client.putObject(...);
}
```

**DESPUÉS**:
```java
public void logToDynamoDB(String procedure, String jsonBody, boolean success, String errorMessage) {
    // Guarda en DynamoDB usando repository
    logEntryRepository.save(logEntry);
}

// Método legacy para compatibilidad
@Deprecated
public void logToS3(...) {
    logToDynamoDB(...);
}
```

#### ScheduledWorker.java
- **Comentado completamente** (las anotaciones `@Component` y `@Scheduled`)
- Se mantiene como referencia para rollback si es necesario
- Para reactivar: descomentar `@Component` y `@Scheduled`

#### AwsConfig.java
**ANTES**:
```java
@Bean
public S3Client s3Client() {
    return S3Client.builder()...;
}
```

**DESPUÉS**:
```java
@Bean
public DynamoDbClient dynamoDbClient() {
    return DynamoDbClient.builder()...;
}

@Bean
public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
    return DynamoDbEnhancedClient.builder()...;
}
```

#### application.properties
Nuevas propiedades agregadas:

```properties
# AWS Region
aws.region=us-east-1

# SQS - AWS Spring Cloud format
sqs.fifo.queue-name=uca-test-factmasiva-sqs.fifo
sqs.dlq.queue-name=uca-test-factmasiva-DLQ.fifo

# SQS Listener Configuration
spring.cloud.aws.sqs.listener.max-concurrent-messages=10
spring.cloud.aws.sqs.listener.poll-timeout=10s

# DynamoDB
aws.dynamodb.table.logs=FacturacionMasivaLogs

# Logging
logging.level.io.awspring.cloud.sqs=DEBUG
```

### 5. MensajesServiceImpl.java
- **No modificado** - Se mantiene para compatibilidad si se reactiva ScheduledWorker
- Puede eliminarse en el futuro si se confirma que no se necesita

## Ventajas de la Migración

### Performance
1. **Event-driven**: No hay polling innecesario cuando no hay mensajes
2. **Concurrencia**: Procesa hasta 10 mensajes simultáneamente
3. **Long Polling optimizado**: Spring maneja automáticamente el polling eficiente

### Confiabilidad
1. **Acknowledgement automático**: Spring gestiona el ciclo de vida del mensaje
2. **DLQ automática**: No se necesita código para reintentos
3. **Visibility timeout**: Gestionado automáticamente por Spring

### Mantenibilidad
1. **Menos código**: ~200 líneas eliminadas del ScheduledWorker
2. **Configuración declarativa**: Todo en `application.properties`
3. **Spring-native**: Mejor integración con el ecosistema Spring Boot

### Costos
1. **DynamoDB**: Más económico que S3 para acceso frecuente
2. **SQS**: Menos llamadas API con event-driven vs polling
3. **TTL automático**: Limpieza automática de logs antiguos

### Observabilidad
1. **CloudWatch automático**: Métricas publicadas por Spring
2. **Queries eficientes**: DynamoDB permite consultas por procedure y timestamp
3. **Logs estructurados**: Mejor análisis que archivos JSON en S3

## Flujo de Procesamiento Comparado

### ANTES (Polling Manual)
```
[Timer: cada 5s] → [getApproximateNumberOfMessages]
                 ↓
         [Hay mensajes?] → NO → Esperar 5s
                 ↓ SI
         [receiveMessage (max 10)]
                 ↓
         [procesar cada mensaje]
                 ↓
         [deleteMessage si exitoso]
         [sendMessage a DLQ si falla]
                 ↓
         [Loop hasta vaciar cola]
```

### DESPUÉS (Event-driven)
```
[Mensaje llega a SQS]
         ↓
[@SqsListener detecta mensaje automáticamente]
         ↓
[procesar mensaje]
         ↓
[ack.acknowledge() si exitoso]
[no acknowledge si falla → reintento o DLQ automático]
```

## Compatibilidad con Versiones Anteriores

### Rollback a versión anterior
1. Descomentar `@Component` y `@Scheduled` en `ScheduledWorker.java`
2. Comentar `@Component` en `SqsMessageListener.java`
3. Cambiar `logToDynamoDB()` por `logToS3()` en `ProcedureCallerImpl.java`

### Coexistencia temporal
- Ambos sistemas pueden coexistir durante pruebas
- Para activar ambos: descomentar ScheduledWorker (cuidado con procesamiento duplicado)

## Testing

### Verificar que el listener está activo

```bash
# Ver logs de Spring Boot
tail -f application.log | grep "io.awspring.cloud.sqs"

# Deberías ver:
# [io.awspring.cloud.sqs] : Starting SQS message listener for queue: uca-test-factmasiva-sqs.fifo
```

### Enviar mensaje de prueba

Ver `AWS_SETUP.md` para comandos de testing.

### Verificar logs en DynamoDB

```bash
aws dynamodb scan --table-name FacturacionMasivaLogs --limit 10
```

## Próximos Pasos (Opcional)

1. **Eliminar código legacy**:
   - `ScheduledWorker.java` (completo)
   - `MensajesServiceImpl.java` (completo)
   - Propiedades `sqs.fifo.endpoint` del application.properties

2. **Agregar métricas custom**:
   - Usar Micrometer para métricas adicionales
   - Dashboard de CloudWatch personalizado

3. **Implementar Circuit Breaker**:
   - Resilience4j para manejo de fallos en Oracle DB

4. **Agregar tracing distribuido**:
   - AWS X-Ray para seguimiento de requests

## Soporte

Para dudas o problemas con la migración:
1. Revisar logs con `logging.level.io.awspring.cloud.sqs=DEBUG`
2. Consultar documentación oficial: https://docs.awspring.io/
3. Verificar permisos IAM (ver `AWS_SETUP.md`)

---

**Fecha de migración**: 2025-11-05
**Versión**: 1.2.0 (sugerida)
**Responsable**: Claude Code
