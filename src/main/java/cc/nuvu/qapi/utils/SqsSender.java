package cc.nuvu.qapi.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cc.nuvu.qapi.presentation.dto.Request;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SqsSender {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.fifo.queue.name}")
    private String queueName;


    public SqsSender(SqsTemplate sqsTemplate){
        this.sqsTemplate = sqsTemplate;
    }

    public String enviarPedido(Request request){
                System.out.println(queueName);
                SendResult<Request> result = sqsTemplate.send(
                    to -> to
                    .queue(queueName)
                    .payload(request)
                );

                String messageId = result.messageId().toString();
                return messageId;
    }
}
