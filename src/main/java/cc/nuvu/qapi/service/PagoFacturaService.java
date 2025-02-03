package cc.nuvu.qapi.service;

import cc.nuvu.qapi.model.PagoFacturaRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PagoFacturaService {

    /**
     * Genera un JSON con los campos proporcionados por el cliente y añade la categoría FACTURA.
     *
     * @param pagoFacturaRequest Datos de pago proporcionados por el cliente.
     * @return String con el JSON generado.
     */

    public String generarJSONPagoFactura(PagoFacturaRequest pagoFacturaRequest) {
        if (pagoFacturaRequest == null) {
            throw new IllegalArgumentException("PagoFacturaRequest no puede ser nulo");
        }

        try {
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Object> dataMap = new HashMap<>();

            // PAGO
            dataMap.put("cuentaBancaria", pagoFacturaRequest.getCuentaBancaria());
            dataMap.put("operacionEfectivoCaja", pagoFacturaRequest.getOperacionEfectivoCaja());
            dataMap.put("areaResponsabilidad", pagoFacturaRequest.getAreaResponsabilidad());
            dataMap.put("beneficiario", pagoFacturaRequest.getBeneficiario());
            dataMap.put("valor", pagoFacturaRequest.getValor());
            // Agregar los campos al mapa
            dataMap.put("referencia", pagoFacturaRequest.getReferencia());
            dataMap.put("cicloLectivo", pagoFacturaRequest.getCicloLectivo());
            dataMap.put("auxiliar", pagoFacturaRequest.getAuxiliar());
            dataMap.put("observacion", pagoFacturaRequest.getObservacion());
            dataMap.put("dependencia", pagoFacturaRequest.getDependencia());
            dataMap.put("cuentaConsignacion", pagoFacturaRequest.getCuentaConsignacion());
            dataMap.put("conceptoFacturacion", pagoFacturaRequest.getConceptoFacturacion());
            dataMap.put("valorUnitario", pagoFacturaRequest.getValorUnitario());
            dataMap.put("conceptoAdicionales", pagoFacturaRequest.getConceptoAdicionales());

            // Terceros
            dataMap.put("claseIdentificacion", pagoFacturaRequest.getClaseIdentificacion());
            dataMap.put("numeroIdentificacion", pagoFacturaRequest.getNumeroIdentificacion());
            dataMap.put("documentoAlterno", pagoFacturaRequest.getDocumentoAlterno());
            dataMap.put("descripcionAuxiliar", pagoFacturaRequest.getDescripcionAuxiliar());
            dataMap.put("naturalJuridica", pagoFacturaRequest.getNaturalJuridica());
            dataMap.put("tipoAuxiliar", pagoFacturaRequest.getTipoAuxiliar());
            dataMap.put("tipoRetencion", pagoFacturaRequest.getTipoRetencion());
            dataMap.put("centroCostosAsociado", pagoFacturaRequest.getCentroCostosAsociado());
            dataMap.put("pais", pagoFacturaRequest.getPais());
            dataMap.put("departamento", pagoFacturaRequest.getDepartamento());
            dataMap.put("ciudad", pagoFacturaRequest.getCiudad());
            dataMap.put("celular", pagoFacturaRequest.getCelular());
            dataMap.put("telefono", pagoFacturaRequest.getTelefono());
            dataMap.put("direccion", pagoFacturaRequest.getDireccion());
            dataMap.put("correoElectronico", pagoFacturaRequest.getCorreoElectronico());

            // Agregar la categoría
            jsonMap.put("service", "PAGO-FACTURA");
            jsonMap.put("data", dataMap);

            // Convertir a JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el JSON del pago", e);
        }
    }
}
