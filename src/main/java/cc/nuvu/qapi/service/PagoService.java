package cc.nuvu.qapi.service;

import cc.nuvu.qapi.model.PagoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PagoService {

    /**
     * Genera un JSON con los campos proporcionados por el cliente y añade la categoría FACTURA.
     *
     * @param pagoRequest Datos de pago proporcionados por el cliente.
     * @return String con el JSON generado.
     */

    public String generarJSONPago(PagoRequest pagoRequest) {
        if (pagoRequest == null) {
            throw new IllegalArgumentException("PagoRequest no puede ser nulo");
        }

        try {
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            // Agregar los campos al mapa
            dataMap.put("cuentaBancaria", pagoRequest.getCuentaBancaria());
            dataMap.put("operacionEfectivoCaja", pagoRequest.getOperacionEfectivoCaja());
            dataMap.put("areaResponsabilidad", pagoRequest.getAreaResponsabilidad());
            dataMap.put("beneficiario", pagoRequest.getBeneficiario());
            dataMap.put("valor", pagoRequest.getValor());
            dataMap.put("referencia", pagoRequest.getReferencia());

            // Agregar la categoría
            jsonMap.put("categoria", "PAGO");
            jsonMap.put("data", dataMap);

            // Convertir a JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el JSON del pago", e);
        }
    }
}
