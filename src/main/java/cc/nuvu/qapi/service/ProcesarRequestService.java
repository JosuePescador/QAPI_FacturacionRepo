package cc.nuvu.qapi.service;

import cc.nuvu.qapi.presentation.dto.Request;
import cc.nuvu.qapi.utils.SqsSender;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import cc.nuvu.qapi.infraestructure.dynamoDB.service.InfoRequestService;

@Service
@RequiredArgsConstructor
public class ProcesarRequestService {

    private final SqsSender sqsSender;
    private final InfoRequestService infoRequestService;

    public String recibirYProcesarRequest(Request request) {
        String id_mensaje = sqsSender.enviarPedido(request);
        infoRequestService.guardar(id_mensaje, request);
        return id_mensaje;
    }
}
