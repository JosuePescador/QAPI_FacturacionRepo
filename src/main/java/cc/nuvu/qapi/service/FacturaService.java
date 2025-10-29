package cc.nuvu.qapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.nuvu.qapi.dto.FacturaRequest;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FacturaService {

    /**
     * Genera un JSON con la categoría FACTURA y un arreglo de facturas en data.facturas.
     *
     * @param facturaRequest Datos de las facturas proporcionados por el cliente.
     * @return String con el JSON generado.
     */
    public String generarJSONFactura(FacturaRequest facturaRequest) {
        if (facturaRequest == null) {
            throw new IllegalArgumentException("FacturaRequest no puede ser nulo");
        }
        if (facturaRequest.getFacturas() == null || facturaRequest.getFacturas().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos una factura en 'facturas'");
        }

        try {
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            // Estructura solicitada: { service: "FACTURA", data: { facturas: [ ... ] } }
            dataMap.put("facturas", facturaRequest.getFacturas());

            jsonMap.put("service", "FACTURA");
            jsonMap.put("data", dataMap);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el JSON de la(s) factura(s)", e);
        }
    }
}

