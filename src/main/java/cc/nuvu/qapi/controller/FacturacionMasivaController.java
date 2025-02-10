package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.model.*;
import cc.nuvu.qapi.service.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/facturacion-masiva")
public class FacturacionMasivaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PagoFacturaService pagoFacturaService;

    @Autowired
    private NotaService nDebitoCreditoService;

    @Autowired
    private SqsService sqsService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    private String procesarJson(String jsonOriginal, String tipo, HttpServletRequest request) throws Exception {
        Map<String, Object> jsonData = objectMapper.readValue(jsonOriginal, new TypeReference<Map<String, Object>>() {});

        // Capturar headers
        Map<String, String> headersMap = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headersMap.put(headerName, request.getHeader(headerName));
        }

        // Crear nuevo JSON estructurado correctamente
        Map<String, Object> jsonFinal = new LinkedHashMap<>();
        jsonFinal.put("headers", headersMap); 
        jsonFinal.put("body", jsonData);  

        // Convertir a JSON
        String jsonFinalString = objectMapper.writeValueAsString(jsonFinal);
        String jsonDataString = objectMapper.writeValueAsString(jsonData);


        // Subir a S3
        s3Service.uploadJson(jsonFinalString, tipo, UUID.randomUUID().toString());

        // Enviar a SQS
        return sqsService.sendMessage(jsonDataString);
    }

    @PostMapping("/factura")
    public ResponseEntity<String> generarJsonFactura(@RequestBody FacturaRequest factura, HttpServletRequest request) {
        try {
            String jsonFactura = facturaService.generarJSONFactura(factura);
            String response = procesarJson(jsonFactura, "factura", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al procesar factura", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/pago")
    public ResponseEntity<String> generarJsonPago(@RequestBody PagoRequest pago, HttpServletRequest request) {
        try {
            String jsonPago = pagoService.generarJSONPago(pago);
            String response = procesarJson(jsonPago, "pago", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al procesar pago", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/pago-factura")
    public ResponseEntity<String> generarJsonPagoFactura(@RequestBody PagoFacturaRequest pagoFactura,
            HttpServletRequest request) {
        try {
            String jsonPagoFactura = pagoFacturaService.generarJSONPagoFactura(pagoFactura);
            String response = procesarJson(jsonPagoFactura, "pago-factura", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al procesar pago-factura", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/nota")
    public ResponseEntity<String> generarJsonNDebitoCredito(@RequestBody NotaRequest notaDebitoCredito,
            HttpServletRequest request) {
        try {
            String jsonNDebitoCredito = nDebitoCreditoService.generarJSONNDebito(notaDebitoCredito);
            String response = procesarJson(jsonNDebitoCredito, "nota", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al procesar nota", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}