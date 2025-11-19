package cc.nuvu.qapi.presentation.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import cc.nuvu.qapi.presentation.dto.NotaDebitoRequest;
import cc.nuvu.qapi.service.ProcesarRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/facturacion-masiva")
public class FacturacionMasivaController {

    private final ProcesarRequestService procesarRequestService;

    public FacturacionMasivaController(ProcesarRequestService procesarRequestService) {
        this.procesarRequestService = procesarRequestService;
    }

    @PostMapping("/nota-debito")
    public ResponseEntity<Map<String, Object>> generarJsonNotaDebito(@Valid @RequestBody List<NotaDebitoRequest> notaDebito,
            HttpServletRequest request) throws JsonProcessingException {
        
        log.info("📬 Recibida solicitud POST /nota-debito con {} facturas", notaDebito.size());
        
        var info = procesarRequestService.recibirYProcesarRequestBatch(notaDebito);
        
        Map<String, Object> response = new HashMap<>();
        response.put("messageIds", info);
        response.put("status", "ACCEPTED");
        response.put("count", info.size());
        
        log.info("✅ Respuesta preparada: {} messageIds", info.size());
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
