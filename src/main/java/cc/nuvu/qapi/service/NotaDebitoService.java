package cc.nuvu.qapi.service;

import cc.nuvu.qapi.model.NotaDebitoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotaDebitoService {

    /**
     * Genera un JSON con la categoría FACTURA y un arreglo de notasdebito en data.notasdebito.
     *
     * @param notadebitoRequest Datos de las notasdebito proporcionados por el cliente.
     * @return String con el JSON generado.
     */
    public String generarJSONNotaDebito(NotaDebitoRequest notadebitoRequest) {
        if (notadebitoRequest == null) {
            throw new IllegalArgumentException("NotaDebitoRequest no puede ser nulo");
        }
        if (notadebitoRequest.getNotasDebito() == null || notadebitoRequest.getNotasDebito().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos una notadebito en 'notasdebito'");
        }

        try {
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            // Estructura solicitada: { service: "NOTA DEBITO", data: { notasdebito: [ ... ] } }
            dataMap.put("notasdebito", notadebitoRequest.getNotasDebito());

            jsonMap.put("service", "NOTA-DEBITO");
            jsonMap.put("data", dataMap);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el JSON de la(s) nota(s) debito", e);
        }
    }
}

