package cc.nuvu.qapi.service;

import cc.nuvu.qapi.presentation.dto.Request;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import cc.nuvu.qapi.infraestructure.dynamoDB.service.InfoRequestService;
import cc.nuvu.qapi.infraestructure.sqs.SqsSender;

@Service
@RequiredArgsConstructor
public class ProcesarRequestService {

    private final SqsSender sqsSender;
    private final InfoRequestService infoRequestService;

    public String recibirYProcesarRequest(Request request) throws JsonProcessingException {
        String id_mensaje = sqsSender.enviarPedido(request);
        infoRequestService.guardar(id_mensaje, request);
        return id_mensaje;
    }

    public List<String> recibirYProcesarRequestBatch(List<? extends Request> request) throws JsonProcessingException {
        List<String> id_mensaje = sqsSender.enviarSolicitud(request);
        for(String mensaje : id_mensaje){
            infoRequestService.guardarBatch(mensaje, request);
        }
        return id_mensaje;
    }
}
