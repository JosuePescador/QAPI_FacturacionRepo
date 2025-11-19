package cc.nuvu.qapi.infraestructure.dynamoDB.entity;

import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class InfoRequestProcessing {

    private String messageId;
    private String body;
    private String estado;
    private String timestamp;
    private String errorMessage;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("messageId")
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @DynamoDbAttribute("body")
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @DynamoDbAttribute("estado")
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @DynamoDbAttribute("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDbAttribute("errorMessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public InfoRequestProcessing() {
        this.timestamp = Instant.now().toString();
    }

    public InfoRequestProcessing(String messageId, String body, String estado) {
        this.messageId = messageId;
        this.body = body;
        this.estado = estado;
        this.timestamp = Instant.now().toString();
    }

    @Override
    public String toString() {
        return "InfoRequestProcessing{" +
                "messageId='" + messageId + '\'' +
                ", estado='" + estado + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
