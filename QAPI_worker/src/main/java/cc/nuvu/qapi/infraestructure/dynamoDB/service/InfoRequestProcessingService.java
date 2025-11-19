package cc.nuvu.qapi.infraestructure.dynamoDB.service;

import org.springframework.stereotype.Service;

import cc.nuvu.qapi.infraestructure.dynamoDB.entity.InfoRequestProcessing;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Service
@Slf4j
public class InfoRequestProcessingService {

    private final DynamoDbTable<InfoRequestProcessing> table;

    public InfoRequestProcessingService(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("InfoRequestProcessing", 
            TableSchema.fromBean(InfoRequestProcessing.class));
    }

    /**
     * Guarda el mensaje consumido desde SQS en DynamoDB
     * 
     * @param messageId ID del mensaje SQS
     * @param body Cuerpo del mensaje (JSON)
     * @param estado Estado inicial: "RECIBIDO", "PROCESANDO", "COMPLETADO", "ERROR"
     */
    public void guardarMensajeConsumido(String messageId, String body, String estado) {
        try {
            InfoRequestProcessing item = new InfoRequestProcessing(messageId, body, estado);
            
            log.info("💾 Guardando mensaje en InfoRequestProcessing - MessageId: {}, Estado: {}", 
                messageId, estado);
            
            table.putItem(item);
            
            log.info("✅ Mensaje guardado exitosamente en InfoRequestProcessing");
            
        } catch (Exception e) {
            log.error("❌ Error guardando en InfoRequestProcessing - MessageId: {}", messageId, e);
            // No lanzamos excepción para no interrumpir el procesamiento
        }
    }

    /**
     * Actualiza el estado del mensaje procesado
     * 
     * @param messageId ID del mensaje
     * @param nuevoEstado Nuevo estado
     * @param errorMessage Mensaje de error (opcional)
     */
    public void actualizarEstado(String messageId, String nuevoEstado, String errorMessage) {
        try {
            // Obtener el item actual
            InfoRequestProcessing item = table.getItem(r -> r.key(k -> k.partitionValue(messageId)));
            
            if (item != null) {
                item.setEstado(nuevoEstado);
                if (errorMessage != null) {
                    item.setErrorMessage(errorMessage);
                }
                
                table.putItem(item);
                log.info("✅ Estado actualizado en InfoRequestProcessing - MessageId: {}, Estado: {}", 
                    messageId, nuevoEstado);
            } else {
                log.warn("⚠️ No se encontró mensaje con ID: {}", messageId);
            }
            
        } catch (Exception e) {
            log.error("❌ Error actualizando estado en InfoRequestProcessing - MessageId: {}", 
                messageId, e);
        }
    }
}
