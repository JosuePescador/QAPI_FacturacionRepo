package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.dto.FacturaRequest;
import cc.nuvu.qapi.dto.NotaRequest;
import cc.nuvu.qapi.dto.PagoFacturaRequest;
import cc.nuvu.qapi.dto.PagoRequest;
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
    private NotaDebitoService notaDebitoService;

    @Autowired
    private SqsService sqsService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, String> procesarJson(String jsonOriginal, String tipo, HttpServletRequest request)
            throws Exception {
        Map<String, Object> jsonData = objectMapper.readValue(jsonOriginal, new TypeReference<Map<String, Object>>() {
        });

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

        // Enviar mensaje a SQS y obtener el ID real de SQS
        String sqsMessageId = sqsService.sendMessage(jsonDataString).trim(); // Asegurar que no tenga espacios

        // Asegurar que el archivo en S3 use el mismo ID del mensaje en SQS
        s3Service.uploadJson(jsonFinalString, tipo, sqsMessageId);
        // Devolver respuesta en formato JSON
        Map<String, String> response = new LinkedHashMap<>();
        response.put("mensaje", "Mensaje enviado con éxito");
        response.put("idMensaje", sqsMessageId);

        return response;
    }

    @PostMapping("/factura")
    public ResponseEntity<Map<String, String>> generarJsonFactura(@RequestBody FacturaRequest factura,
            HttpServletRequest request) {
        try {
            String jsonFactura = facturaService.generarJSONFactura(factura);
            Map<String, String> response = procesarJson(jsonFactura, "factura", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar factura"));
        }
    }

    @PostMapping("/pago")
    public ResponseEntity<Map<String, String>> generarJsonPago(@RequestBody PagoRequest pago,
            HttpServletRequest request) {
        try {
            String jsonPago = pagoService.generarJSONPago(pago);
            Map<String, String> response = procesarJson(jsonPago, "pago", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar pago"));
        }
    }

    @PostMapping("/pago-factura")
    public ResponseEntity<Map<String, String>> generarJsonPagoFactura(@RequestBody PagoFacturaRequest pagoFactura,
            HttpServletRequest request) {
        try {
            String jsonPagoFactura = pagoFacturaService.generarJSONPagoFactura(pagoFactura);
            Map<String, String> response = procesarJson(jsonPagoFactura, "pago-factura", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar pago-factura"));
        }
    }

        @PostMapping("/nota-debito")
    public ResponseEntity<Map<String, String>> generarJsonNotaDebito(@RequestBody NotaDebitoRequest notaDebito,
            HttpServletRequest request) {
        try {
            String jsonNotaDebito = notaDebitoService.generarJSONNotaDebito(notaDebito);
            Map<String, String> response = procesarJson(jsonNotaDebito, "nota-debito", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar nota-debito"));
        }
    }


    @PostMapping("/nota")
    public ResponseEntity<Map<String, String>> generarJsonNDebitoCredito(@RequestBody NotaRequest notaDebitoCredito,
            HttpServletRequest request) {
        try {
            String jsonNDebitoCredito = nDebitoCreditoService.generarJSONNDebito(notaDebitoCredito);
            Map<String, String> response = procesarJson(jsonNDebitoCredito, "nota", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar pago-factura"));
        }
    }
}
