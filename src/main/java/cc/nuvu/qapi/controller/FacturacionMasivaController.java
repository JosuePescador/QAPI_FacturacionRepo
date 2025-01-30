package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.model.*;
import cc.nuvu.qapi.service.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FacturacionMasivaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PagoFacturaService pagoFacturaService;

    @Autowired
    private NDebitoService nDebitoService;

    @Autowired
    private NCreditoService nCreditoService;

    @Autowired
    private SqsService sqsService;

    // Endpoint para generar JSON de Factura
    @PostMapping("/factura/generar-json")
    public List<String> generarJsonFactura(@RequestBody List<FacturaRequest> facturas) {
        List<String> respuestas = new ArrayList<>();
        for (FacturaRequest factura : facturas) {
            String jsonFactura = facturaService.generarJSONFactura(factura);
            String response = sqsService.sendMessage(jsonFactura);
            respuestas.add(response);
        }
        return respuestas;
    }

    // Endpoint para generar JSON de Pago
    @PostMapping("/pago/generar-json")
    public List<String> generarJsonPago(@RequestBody List<PagoRequest> pagos) {
        List<String> respuestas = new ArrayList<>();
        for (PagoRequest pago : pagos) {
            String jsonPago = pagoService.generarJSONPago(pago);
            String response = sqsService.sendMessage(jsonPago);
            respuestas.add(response);
        }
        return respuestas;
    }

    // Endpoint para generar JSON de Pago Factura
    @PostMapping("/pagoFactura/generar-json")
    public List<String> generarJsonPagoFactura(@RequestBody List<PagoFacturaRequest> pagoFacturas) {
        List<String> respuestas = new ArrayList<>();
        for (PagoFacturaRequest pagoFactura : pagoFacturas) {
            String jsonPagoFactura = pagoFacturaService.generarJSONPagoFactura(pagoFactura);
            String response = sqsService.sendMessage(jsonPagoFactura);
            respuestas.add(response);
        }
        return respuestas;
    }

    // Endpoint para generar JSON de Nota Débito
    @PostMapping("/notaDebito/generar-json")
    public List<String> generarJsonNDebito(@RequestBody List<NDebitoRequest> notasDebito) {
        List<String> respuestas = new ArrayList<>();
        for (NDebitoRequest notaDebito : notasDebito) {
            String jsonNDebito = nDebitoService.generarJSONNDebito(notaDebito);
            String response = sqsService.sendMessage(jsonNDebito);
            respuestas.add(response);
        }
        return respuestas;
    }

    // Endpoint para generar JSON de Nota Crédito
    @PostMapping("/notaCredito/generar-json")
    public List<String> generarJsonNCredito(@RequestBody List<NCreditoRequest> notasCredito) {
        List<String> respuestas = new ArrayList<>();
        for (NCreditoRequest notaCredito : notasCredito) {
            String jsonNCredito = nCreditoService.generarJSONNCredito(notaCredito);
            String response = sqsService.sendMessage(jsonNCredito);
            respuestas.add(response);
        }
        return respuestas;
    }
}