package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.model.*;
import cc.nuvu.qapi.service.*;
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
    public String generarJsonFactura(@RequestBody FacturaRequest factura) {
        String jsonFactura = facturaService.generarJSONFactura(factura);
        return sqsService.sendMessage(jsonFactura);
    }

    // Endpoint para generar JSON de Pago
    @PostMapping("/pago/generar-json")
    public String generarJsonPago(@RequestBody PagoRequest pago) {
        String jsonPago = pagoService.generarJSONPago(pago);
        return sqsService.sendMessage(jsonPago);
    }

    // Endpoint para generar JSON de Pago Factura
    @PostMapping("/pagoFactura/generar-json")
    public String generarJsonPagoFactura(@RequestBody PagoFacturaRequest pagoFactura) {
        String jsonPagoFactura = pagoFacturaService.generarJSONPagoFactura(pagoFactura);
        return sqsService.sendMessage(jsonPagoFactura);
    }

    // Endpoint para generar JSON de Nota Débito
    @PostMapping("/notaDebito/generar-json")
    public String generarJsonNDebito(@RequestBody NDebitoRequest nDebito) {
        String jsonNDebito = nDebitoService.generarJSONNDebito(nDebito);
        return sqsService.sendMessage(jsonNDebito);
    }

    // Endpoint para generar JSON de Nota Crédito
    @PostMapping("/notaCredito/generar-json")
    public String generarJsonNCredito(@RequestBody NCreditoRequest nCredito) {
        String jsonNCredito = nCreditoService.generarJSONNCredito(nCredito);
        return sqsService.sendMessage(jsonNCredito);
    }
}