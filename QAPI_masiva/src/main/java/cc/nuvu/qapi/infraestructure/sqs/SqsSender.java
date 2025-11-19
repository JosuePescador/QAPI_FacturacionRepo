package cc.nuvu.qapi.infraestructure.sqs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.nuvu.qapi.presentation.dto.Request;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SqsSender {

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.fifo.queue.name}")
    private String queueName;

    public SqsSender(SqsTemplate sqsTemplate, ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }

    public List<String> enviarSolicitud(List<? extends Request> request) throws JsonProcessingException {
        List<List<? extends Request>> partes = dividirEnPartes(request, 400);
        List<String> idsInBatch = new ArrayList<>();
        
        for(List<? extends Request> parte: partes) {
            idsInBatch.add(enviarPedidoEnBatch(request));
        }
        return idsInBatch;
    }

    public String enviarPedido(Request request) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(request);
        log.info("📨 Enviando mensaje individual a SQS - Cola: {}", queueName);
        log.debug("📄 Body: {}", body);
        
        SendResult<String> result = sqsTemplate.send(
                to -> to
                        .queue(queueName)
                        .payload(body));

        String messageId = result.messageId().toString();
        log.info("✅ Mensaje enviado - MessageId: {}", messageId);
        return messageId;
    }

    public String enviarPedidoEnBatch(List<? extends Request> request) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(request);
        log.info("📨 Enviando batch a SQS - Cola: {} - Items: {}", queueName, request.size());
        log.debug("📄 Body: {}", body);
        
        SendResult<String> result = sqsTemplate.send(
                to -> to
                        .queue(queueName)
                        .payload(body));

        String messageId = result.messageId().toString();
        log.info("✅ Batch enviado - MessageId: {}", messageId);
        return messageId;
    }

    private List<List<? extends Request>> dividirEnPartes(List<? extends Request> lista, int tamano) {
        List<List<? extends Request>> partes = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamano) {
            int fin = Math.min(i + tamano, lista.size());
            partes.add(new ArrayList<>(lista.subList(i, fin)));
        }
        return partes;
    }
}
