package cc.nuvu.qapi.infraestructure.dynamoDB.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import cc.nuvu.qapi.infraestructure.dynamoDB.entities.InfoRequest;
import cc.nuvu.qapi.infraestructure.dynamoDB.mapper.RequestMapper;
import cc.nuvu.qapi.presentation.dto.NotaDebitoRequest;
import cc.nuvu.qapi.presentation.dto.Request;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Service
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

    public void guardar(String idMensaje, Request request) {
        String id = UUID.randomUUID().toString();

        InfoRequest infoRequest = new InfoRequest();
        infoRequest.setId(id);
        infoRequest.setIdMensaje(idMensaje);
        infoRequest.setTipoServicio(request.getServicio());
        infoRequest.setEstado("PENDIENTE");
        infoRequest.setRequest(RequestMapper.toJson(request));

        table.putItem(infoRequest);
    }

}