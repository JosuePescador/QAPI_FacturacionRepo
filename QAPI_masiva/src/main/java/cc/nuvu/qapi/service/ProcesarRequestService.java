package cc.nuvu.qapi.service;

import cc.nuvu.qapi.presentation.dto.Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import cc.nuvu.qapi.infraestructure.dynamoDB.service.InfoRequestService;
import cc.nuvu.qapi.infraestructure.sqs.SqsSender;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesarRequestService {

    private final SqsSender sqsSender;
    private final InfoRequestService infoRequestService;

    public String recibirYProcesarRequest(Request request) throws JsonProcessingException {
        log.info("📨 Procesando request individual...");
        String id_mensaje = sqsSender.enviarPedido(request);
        infoRequestService.guardar(id_mensaje, request);
        log.info("✅ Request procesado - ID: {}", id_mensaje);
        return id_mensaje;
    }

    public List<String> recibirYProcesarRequestBatch(List<? extends Request> request) throws JsonProcessingException {
        log.info("📦 Procesando batch de {} solicitudes...", request.size());
        
        // 1. Enviar a SQS
        log.info("🔄 Paso 1: Enviando a SQS...");
        List<String> id_mensaje = sqsSender.enviarSolicitud(request);
        log.info("✅ Mensajes enviados a SQS: {}", id_mensaje);
        
        // 2. Guardar en DynamoDB
        log.info("🔄 Paso 2: Guardando en DynamoDB...");
        for(String mensaje : id_mensaje){
            infoRequestService.guardarBatch(mensaje, request);
        }
        log.info("✅ Guardado en DynamoDB completado");
        
        return id_mensaje;
    }
}
