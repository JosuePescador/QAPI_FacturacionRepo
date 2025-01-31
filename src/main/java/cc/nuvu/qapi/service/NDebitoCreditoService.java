package cc.nuvu.qapi.service;

import cc.nuvu.qapi.model.NDebitoCreditoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NDebitoCreditoService {

    /**
     * Genera un JSON con los campos proporcionados por el cliente y añade la categoría FACTURA.
     *
     * @param nDebitoCreditoService Datos de pago proporcionados por el cliente.
     * @return String con el JSON generado.
     */

    public String generarJSONNDebito(NDebitoCreditoRequest nDebitoCreditoRequest) {
        if (nDebitoCreditoRequest == null) {
            throw new IllegalArgumentException("nDebitoCreditoRequest no puede ser nulo");
        }

        try {
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            // Agregar los campos al mapa
            dataMap.put("secuencia", nDebitoCreditoRequest.getSecuencia());
            dataMap.put("cuentaBancaria", nDebitoCreditoRequest.getCuentaBancaria());
            dataMap.put("concepto", nDebitoCreditoRequest.getConcepto());
            dataMap.put("valorUnitario", nDebitoCreditoRequest.getValorUnitario());

            // Agregar los campos al mapa
            dataMap.put("vigencia", nDebitoCreditoRequest.getVigencia());
            dataMap.put("tipoDocumento", nDebitoCreditoRequest.getTipoDocumento());
            dataMap.put("estadoDocumento", nDebitoCreditoRequest.getEstadoDocumento());
            dataMap.put("tipoMovimiento", nDebitoCreditoRequest.getTipoMovimiento());
            dataMap.put("cuentaBancaria", nDebitoCreditoRequest.getCuentaBancaria());
            dataMap.put("tipoFacturacion", nDebitoCreditoRequest.getTipoFacturacion());
            dataMap.put("tercero", nDebitoCreditoRequest.getTercero());
            dataMap.put("concepto", nDebitoCreditoRequest.getConcepto());
            dataMap.put("valorUnitario", nDebitoCreditoRequest.getValorUnitario());

            // Agregar la categoría
            jsonMap.put("service", "nota");
            jsonMap.put("data", dataMap);

            // Convertir a JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el JSON del pago", e);
        }
    }
}
