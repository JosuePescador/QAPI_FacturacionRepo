package cc.nuvu.qapi.presentation.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.nuvu.qapi.presentation.dto.NotaDebitoRequest;
import cc.nuvu.qapi.service.ProcesarRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/facturacion-masiva")
public class FacturacionMasivaController {

    private final ProcesarRequestService procesarRequestService;

    public FacturacionMasivaController(ProcesarRequestService procesarRequestService) {
        this.procesarRequestService = procesarRequestService;
    }

    @PostMapping("/nota-debito")
    public ResponseEntity<Map<String, String>> generarJsonNotaDebito(@Valid @RequestBody NotaDebitoRequest notaDebito,
            HttpServletRequest request) {
                var info = procesarRequestService.recibirYProcesarRequest(notaDebito);
                return null;
    }
}
