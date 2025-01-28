package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.model.FacturaRequest;
import cc.nuvu.qapi.service.FacturaService;
import cc.nuvu.qapi.service.SqsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/factura")
public class FacturacionMasivaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private SqsService sqsService;

    @PostMapping("/generar-json")
    public String generarJsonFactura(@RequestBody FacturaRequest factura) {
        // Generar el JSON
        String jsonFactura = facturaService.generarJSONFactura(factura);

        // Enviar el JSON a la cola SQS
        String response = sqsService.sendMessage(jsonFactura);

        return response; // Retornar el ID del mensaje enviado
    }
}
