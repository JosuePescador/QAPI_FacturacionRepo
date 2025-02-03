package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.check.Home;
import cc.nuvu.qapi.model.*;
import cc.nuvu.qapi.service.*;

import org.springframework.beans.factory.annotation.Autowired;
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
    private NDebitoCreditoService nDebitoCreditoService;

    @Autowired
    private SqsService sqsService;

  // Endpoint para generar JSON de Factura (objeto individual)
  @PostMapping("/factura")
  public ResponseEntity<String> generarJsonFactura(@RequestBody FacturaRequest factura) {
      String jsonFactura = facturaService.generarJSONFactura(factura);
      String response = sqsService.sendMessage(jsonFactura);
      String mensaje = Home.generarMensajeFactura(1); // 1 porque es un solo registro
      return ResponseEntity.ok(mensaje + " Respuesta: " + response);
  }

    // Endpoint para generar JSON de Pago (objeto individual)
    @PostMapping("/pago")
    public ResponseEntity<String> generarJsonPago(@RequestBody PagoRequest pago) {
        String jsonPago = pagoService.generarJSONPago(pago);
        String response = sqsService.sendMessage(jsonPago);
        String mensaje = Home.generarMensajePago(1); // 1 porque es un solo registro
        return ResponseEntity.ok(mensaje + " Respuesta: " + response);
    }

    // Endpoint para generar JSON de Pago Factura (objeto individual)
    @PostMapping("/pago-factura")
    public ResponseEntity<String> generarJsonPagoFactura(@RequestBody PagoFacturaRequest pagoFactura) {
        String jsonPagoFactura = pagoFacturaService.generarJSONPagoFactura(pagoFactura);
        String response = sqsService.sendMessage(jsonPagoFactura);
        String mensaje = Home.generarMensajePagoFactura(1); // 1 porque es un solo registro
        return ResponseEntity.ok(mensaje + " Respuesta: " + response);
    }

    // Endpoint para generar JSON de Nota Débito/Crédito (objeto individual)
    @PostMapping("/nota")
    public ResponseEntity<String> generarJsonNDebitoCredito(@RequestBody NDebitoCreditoRequest notaDebitoCredito) {
        String jsonNDebitoCredito = nDebitoCreditoService.generarJSONNDebito(notaDebitoCredito);
        String response = sqsService.sendMessage(jsonNDebitoCredito);
        String mensaje = Home.generarMensajeNota(1); // 1 porque es un solo registro
        return ResponseEntity.ok(mensaje + " Respuesta: " + response);
    }
}
