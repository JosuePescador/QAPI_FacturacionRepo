package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.model.*;
import cc.nuvu.qapi.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Endpoint para generar JSON de Factura (objeto individual)
    @PostMapping("/factura")
    public ResponseEntity<String> generarJsonFactura(@RequestBody FacturaRequest factura) {
        String jsonFactura = facturaService.generarJSONFactura(factura);
        try {
            String response = sqsService.sendMessage(jsonFactura);
            s3Service.uploadJson(jsonFactura, "factura", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String message = "Ha ocurrido un error al enviar el mensaje de factura";
            System.out.println(message);
            System.out.println(e.getMessage());
            return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para generar JSON de Pago (objeto individual)
    @PostMapping("/pago")
    public ResponseEntity<String> generarJsonPago(@RequestBody PagoRequest pago) {
        String jsonPago = pagoService.generarJSONPago(pago);
        try {
            String response = sqsService.sendMessage(jsonPago);
            s3Service.uploadJson(jsonPago, "pago", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String message = "Ha ocurrido un error al enviar el mensaje de factura";
            System.out.println(message);
            System.out.println(e.getMessage());
            return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para generar JSON de Pago Factura (objeto individual)
    @PostMapping("/pago-factura")
    public ResponseEntity<String> generarJsonPagoFactura(@RequestBody PagoFacturaRequest pagoFactura) {
        String jsonPagoFactura = pagoFacturaService.generarJSONPagoFactura(pagoFactura);
        try {
            String response = sqsService.sendMessage(jsonPagoFactura);
            s3Service.uploadJson(jsonPagoFactura, "pago-factura", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String message = "Ha ocurrido un error al enviar el mensaje de factura";
            System.out.println(message);
            System.out.println(e.getMessage());
            return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para generar JSON de Nota Débito/Crédito (objeto individual)
    @PostMapping("/nota")
    public ResponseEntity<String> generarJsonNDebitoCredito(@RequestBody NotaRequest notaDebitoCredito) {
        String jsonNDebitoCredito = nDebitoCreditoService.generarJSONNDebito(notaDebitoCredito);
        try {
            String response = sqsService.sendMessage(jsonNDebitoCredito);
            s3Service.uploadJson(jsonNDebitoCredito, "nota", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String message = "Ha ocurrido un error al enviar el mensaje de factura";
            System.out.println(message);
            System.out.println(e.getMessage());
            return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
