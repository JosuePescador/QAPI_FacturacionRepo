package cc.nuvu.qapi.infraestructure.dynamoDB.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import cc.nuvu.qapi.infraestructure.dynamoDB.entities.InfoRequest;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Service
@Slf4j
public class InfoRequestService {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<InfoRequest> table;

    public InfoRequestService(DynamoDbClient dynamoDbClient) {
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        this.table = enhancedClient.table("InfoRequest",
                TableSchema.fromBean(InfoRequest.class));
    }

    public void guardarEstado(String idMensaje, JsonNode request, String estado) {

        InfoRequest infoRequest = table.getItem(r -> r.key(k -> k.partitionValue(idMensaje)));

        // Si el JSON es una lista (batch), usamos el primer elemento para leer datos
        JsonNode firstNode = request.isArray() ? request.get(0) : request;

        // Evitar NullPointer
        String tipoServicio = firstNode.has("servicio") && !firstNode.get("servicio").isNull()
                ? firstNode.get("servicio").asText()
                : "DESCONOCIDO";

        log.info("📝 Guardando estado para messageId: {} | Estado: {}", idMensaje, estado);

        if (infoRequest != null) {
            // Actualizar registro existente
            log.info("♻️ Actualizando registro existente");
            infoRequest.setTipoServicio(tipoServicio);
            infoRequest.setEstado(estado);
            infoRequest.setRequest(firstNode.toString());
            infoRequest.setCantidad(request.size());
            infoRequest.setFechaRegistro(LocalDateTime.now().toString());
        } else {
            // Crear nuevo registro
            log.info("🆕 Creando nuevo registro");
            infoRequest = new InfoRequest();
            infoRequest.setId(idMensaje); // ← CRÍTICO: establecer el ID (partition key)!
            infoRequest.setTipoServicio(tipoServicio);
            infoRequest.setEstado(estado);
            infoRequest.setRequest(firstNode.toString());
            infoRequest.setCantidad(request.size());
            infoRequest.setFechaRegistro(LocalDateTime.now().toString());
        }

        table.putItem(infoRequest);
        log.info("✅ Registro guardado en InfoRequest: {}", idMensaje);
    }

}