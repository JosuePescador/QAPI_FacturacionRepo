package cc.nuvu.qapi.infraestructure.sqs;

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

    public String enviarPedido(Request request) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(request);
        SendResult<String> result = sqsTemplate.send(
                to -> to
                        .queue(queueName)
                        .payload(body));

        String messageId = result.messageId().toString();
        return messageId;
    }

    public String enviarPedidoEnBatch(List<? extends Request> request) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(request);
        SendResult<String> result = sqsTemplate.send(
                to -> to
                        .queue(queueName)
                        .payload(body));

        String messageId = result.messageId().toString();
        return messageId;
    }
}
