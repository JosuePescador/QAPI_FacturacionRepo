package cc.nuvu.qapi.controller;

import cc.nuvu.qapi.model.FacturaRequest;
import cc.nuvu.qapi.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/factura")
public class FacturacionMasivaController {

    @Autowired
    private FacturaService facturaService;

    @PostMapping("/generar-json")
    public String generarJsonFactura(@RequestBody FacturaRequest factura) {
        return facturaService.generarJSONFactura(factura);
    }
}