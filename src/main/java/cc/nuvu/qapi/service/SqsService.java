package cc.nuvu.qapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.UUID;

@Service
public class SqsService {

    private final SqsClient sqsClient;

    @Value("${aws.sqs.fifo.queue.url}")
    private String queueUrl;

    public SqsService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public String sendMessage(String messageBody) {
        try {
            // Crear el request para enviar un mensaje
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageGroupId("FacturaGroup") // ID del grupo (requerido para FIFO)
                    .messageDeduplicationId(UUID.randomUUID().toString()) // ID único para evitar duplicados
                    .build();

            // Enviar el mensaje
            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);

            return "Mensaje enviado con éxito. ID del mensaje: " + response.messageId();
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el mensaje a la cola SQS", e);
        }
    }
}
