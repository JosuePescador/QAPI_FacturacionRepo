package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.model.*;
import cc.nuvu.qapi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    private NDebitoCreditoService nDebitoCreditoService;

    @Autowired
    private SqsService sqsService;

    // Endpoint para generar JSON de Factura
    @PostMapping("/factura")
    public String generarJsonFactura(@RequestBody FacturaRequest factura) {
        String jsonFactura = facturaService.generarJSONFactura(factura);
        return sqsService.sendMessage(jsonFactura);
    }

    // Endpoint para generar JSON de Pago
    @PostMapping("/pago")
    public String generarJsonPago(@RequestBody PagoRequest pago) {
        String jsonPago = pagoService.generarJSONPago(pago);
        return sqsService.sendMessage(jsonPago);
    }

    // Endpoint para generar JSON de Pago Factura
    @PostMapping("/pago-Factura")
    public String generarJsonPagoFactura(@RequestBody PagoFacturaRequest pagoFactura) {
        String jsonPagoFactura = pagoFacturaService.generarJSONPagoFactura(pagoFactura);
        return sqsService.sendMessage(jsonPagoFactura);
    }

    // Endpoint para generar JSON de Nota DébitoCredito
    @PostMapping("/nota")
    public String generarJsonNDebitoCredito(@RequestBody NDebitoCreditoRequest notaDebitoCredito) {
        String jsonNDebitoCredito = nDebitoCreditoService.generarJSONNDebito(notaDebitoCredito);
        return sqsService.sendMessage(jsonNDebitoCredito);
    }
}
