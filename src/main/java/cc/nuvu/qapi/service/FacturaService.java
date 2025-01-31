package cc.nuvu.qapi.service;

import cc.nuvu.qapi.model.FacturaRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FacturaService {

    /**
     * Genera un JSON con los campos proporcionados por el cliente y añade la categoría FACTURA.
     *
     * @param facturaRequest Datos de la factura proporcionados por el cliente.
     * @return String con el JSON generado.
     */
    public String generarJSONFactura(FacturaRequest facturaRequest) {
        if (facturaRequest == null) {
            throw new IllegalArgumentException("FacturaRequest no puede ser nulo");
        }

        try {
            // Crear el mapa para los datos
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            // Agregar los campos al mapa
            dataMap.put("referencia", facturaRequest.getReferencia());
            dataMap.put("cicloLectivo", facturaRequest.getCicloLectivo());
            dataMap.put("auxiliar", facturaRequest.getAuxiliar());
            dataMap.put("observacion", facturaRequest.getObservacion());
            dataMap.put("dependencia", facturaRequest.getDependencia());
            dataMap.put("cuentaConsignacion", facturaRequest.getCuentaConsignacion());
            dataMap.put("conceptoFacturacion", facturaRequest.getConceptoFacturacion());
            dataMap.put("valorUnitario", facturaRequest.getValorUnitario());
            dataMap.put("conceptoAdicionales", facturaRequest.getConceptoAdicionales());

            // Terceros
            dataMap.put("claseIdentificacion", facturaRequest.getClaseIdentificacion());
            dataMap.put("numeroIdentificacion", facturaRequest.getNumeroIdentificacion());
            dataMap.put("documentoAlterno", facturaRequest.getDocumentoAlterno());
            dataMap.put("descripcionAuxiliar", facturaRequest.getDescripcionAuxiliar());
            dataMap.put("naturalJuridica", facturaRequest.getNaturalJuridica());
            dataMap.put("tipoAuxiliar", facturaRequest.getTipoAuxiliar());
            dataMap.put("tipoRetencion", facturaRequest.getTipoRetencion());
            dataMap.put("centroCostosAsociado", facturaRequest.getCentroCostosAsociado());
            dataMap.put("pais", facturaRequest.getPais());
            dataMap.put("departamento", facturaRequest.getDepartamento());
            dataMap.put("ciudad", facturaRequest.getCiudad());
            dataMap.put("celular", facturaRequest.getCelular());
            dataMap.put("telefono", facturaRequest.getTelefono());
            dataMap.put("direccion", facturaRequest.getDireccion());
            dataMap.put("correoElectronico", facturaRequest.getCorreoElectronico());

            // Agregar la categoría
            jsonMap.put("service", "factura");
            jsonMap.put("data", dataMap);

            // Convertir a JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el JSON de la factura", e);
        }
    }
}

