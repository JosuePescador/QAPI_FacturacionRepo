package cc.nuvu.qapi.presentation.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import cc.nuvu.qapi.infraestructure.dynamoDB.service.InfoRequestProcessingService;
import cc.nuvu.qapi.service.ProcesamientoService;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
@Slf4j
public class SqsQAPIListener {

    private final ProcesamientoService procesamientoService;
    private final InfoRequestProcessingService infoRequestProcessingService;

    public SqsQAPIListener(
            ProcesamientoService procesamientoService,
            InfoRequestProcessingService infoRequestProcessingService
            ) {
        this.procesamientoService = procesamientoService;
        this.infoRequestProcessingService = infoRequestProcessingService;
    }

    // TODO: Si las peticiones fueran objetos simples sólo habría que usar un MAP y ahí entrarían sin conflicto los values, sin embargo, hay que meter mappers y declarar peticiones para ciertos objetos, lo cual, no está chévere
    // Deberíamos simplificar la lógica de ORACLE a puros primitivos y así podríamos generalizar en JAVA
    // TODO: Actualmente el servicio de guardarFactura, es sólo una parte del servicio completo, por naturaleza de este proyecto
    // se deben guardar 1...n facturas, cosa que no se puede cumplir naturalmente con el stack actual.
    // En otras palabras yo no puedo procesar por aparte cada objeto, porque existe una validación extra en un método extra
    // que es el de guardaMultiplesFacturas, esto no permite que se abstraiga la lógica y se generalice la manera en la que
    // se llama a todos los servicios, así que para esta versión, se asumirá necesario el uso del servicio guardaMultiplesFacturas
    // Pero en el futuro, se espera, que al quedar este servicio actualizado, se puede procesar desde Java con un ciclo, y así
    // sea posible generalizar facilmente la lógica.
    @SqsListener("${aws.sqs.fifo.queue.name}")
    public void consumir(
            Message message) throws JsonMappingException, JsonProcessingException {

        String body = message.body();                 
        String messageId = message.messageId();

        // 1️⃣ Guardar mensaje consumido en InfoRequestProcessing
        log.info("📥 Mensaje consumido de SQS - MessageId: {}", messageId);
        infoRequestProcessingService.guardarMensajeConsumido(messageId, body, "RECIBIDO");

        try {
            // 2️⃣ Actualizar estado a PROCESANDO
            infoRequestProcessingService.actualizarEstado(messageId, "PROCESANDO", null);
            
            // 3️⃣ Procesar el servicio
            procesamientoService.procesarServicio(body, messageId);
            
            // 4️⃣ Si todo sale bien, marcar como COMPLETADO
            infoRequestProcessingService.actualizarEstado(messageId, "COMPLETADO", null);
            
        } catch (Exception e) {
            // 5️⃣ Si hay error, marcar como ERROR
            log.error("❌ Error procesando mensaje - MessageId: {}", messageId, e);
            infoRequestProcessingService.actualizarEstado(messageId, "ERROR", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
