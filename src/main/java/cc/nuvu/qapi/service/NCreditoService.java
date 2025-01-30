package cc.nuvu.qapi.service;

import cc.nuvu.qapi.model.NCreditoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NCreditoService {

    /**
     * Genera un JSON con los campos proporcionados por el cliente y añade la categoría FACTURA.
     *
     * @param nCreditoRequest Datos de pago proporcionados por el cliente.
     * @return String con el JSON generado.
     */

    public String generarJSONNCredito(NCreditoRequest nCreditoRequest) {
        if (nCreditoRequest == null) {
            throw new IllegalArgumentException("NCreditoRequest no puede ser nulo");
        }

        try {
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            // Agregar los campos al mapa
            dataMap.put("vigencia", nCreditoRequest.getVigencia());
            dataMap.put("tipoDocumento", nCreditoRequest.getTipoDocumento());
            dataMap.put("estadoDocumento", nCreditoRequest.getEstadoDocumento());
            dataMap.put("tipoMovimiento", nCreditoRequest.getTipoMovimiento());
            dataMap.put("cuentaBancaria", nCreditoRequest.getCuentaBancaria());
            dataMap.put("tipoFacturacion", nCreditoRequest.getTipoFacturacion());
            dataMap.put("tercero", nCreditoRequest.getTercero());
            dataMap.put("concepto", nCreditoRequest.getConcepto());
            dataMap.put("valorUnitario", nCreditoRequest.getValorUnitario());

            // Agregar la categoría
            jsonMap.put("categoria", "NCREDITO");
            jsonMap.put("data", dataMap);

            // Convertir a JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el JSON del pago", e);
        }
    }
}
