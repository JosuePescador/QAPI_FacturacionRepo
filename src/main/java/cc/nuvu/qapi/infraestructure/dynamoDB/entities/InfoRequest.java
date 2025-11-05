package cc.nuvu.qapi.infraestructure.dynamoDB.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;


@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfoRequest {

    private String id;
    private String idMensaje;
    private String tipoServicio;
    private String estado;
    private String request;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
    
}
