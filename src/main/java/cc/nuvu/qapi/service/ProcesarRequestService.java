package cc.nuvu.qapi.service;

import cc.nuvu.qapi.presentation.dto.Request;
import cc.nuvu.qapi.utils.SqsSender;

import org.springframework.stereotype.Service;

@Service
public class ProcesarRequestService {

    private final S3Service s3service;
    private final SqsSender sqsSender;

    public ProcesarRequestService(S3Service s3service, SqsSender sqsSender) {
        this.s3service = s3service;
        this.sqsSender = sqsSender;
    }

    public String recibirYProcesarRequest(Request request){
        String id_mensaje = sqsSender.enviarPedido(request);
        return id_mensaje;
    }
}

