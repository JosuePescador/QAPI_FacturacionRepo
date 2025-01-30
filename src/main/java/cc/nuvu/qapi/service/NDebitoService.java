package cc.nuvu.qapi.service;

import cc.nuvu.qapi.model.NDebitoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NDebitoService {

    /**
     * Genera un JSON con los campos proporcionados por el cliente y añade la categoría FACTURA.
     *
     * @param nDebitoService Datos de pago proporcionados por el cliente.
     * @return String con el JSON generado.
     */

    public String generarJSONNDebito(NDebitoRequest nDebitoRequest) {
        if (nDebitoRequest == null) {
            throw new IllegalArgumentException("nDebitoRequest no puede ser nulo");
        }

        try {
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            // Agregar los campos al mapa
            dataMap.put("secuencia", nDebitoRequest.getSecuencia());
            dataMap.put("cuentaBancaria", nDebitoRequest.getCuentaBancaria());
            dataMap.put("concepto", nDebitoRequest.getConcepto());
            dataMap.put("valorUnitario", nDebitoRequest.getValorUnitario());

            // Agregar la categoría
            jsonMap.put("categoria", "NDEBITO");
            jsonMap.put("data", dataMap);

            // Convertir a JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el JSON del pago", e);
        }
    }
}
