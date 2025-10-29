// package cc.nuvu.qapi.service;

// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.stereotype.Service;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import cc.nuvu.qapi.dto.NotaDebitoRequest;

// @Service
// public class xd {

//     public String generarJSONNotaDebito(NotaDebitoRequest notaDebitoRequest) throws JsonProcessingException {
//         try {

//             Map<String, Object> conceptoMap = Map.of(
//                     "conceptoFacturacion", notaDebitoRequest.getConcepto().getConceptoFacturacion(),
//                     "cantidadUnidades", notaDebitoRequest.getConcepto().getCantidadUnidades(),
//                     "valorUnitario", notaDebitoRequest.getConcepto().getValorUnitario());

//             Map<String, Object> tercerosMap = new HashMap<>();
//             tercerosMap.put("claseIdentificacion", notaDebitoRequest.getTercero().getClaseIdentificacion());
//             tercerosMap.put("numeroIdentificacion", notaDebitoRequest.getTercero().getNumeroIdentificacion());
//             tercerosMap.put("descripcionAuxiliar", notaDebitoRequest.getTercero().getDescripcionAuxiliar());
//             tercerosMap.put("naturalJuridica", notaDebitoRequest.getTercero().getNaturalJuridica());
//             tercerosMap.put("tipoAuxiliar", notaDebitoRequest.getTercero().getTipoAuxiliar());
//             tercerosMap.put("tipoRetencion", notaDebitoRequest.getTercero().getTipoRetencion());
//             tercerosMap.put("pais", notaDebitoRequest.getTercero().getCodigoPais());
//             tercerosMap.put("departamento", notaDebitoRequest.getTercero().getCodigoDepartamento());
//             tercerosMap.put("ciudad", notaDebitoRequest.getTercero().getCodigoCiudad());
//             tercerosMap.put("celular", notaDebitoRequest.getTercero().getCelular());
//             tercerosMap.put("telefono", notaDebitoRequest.getTercero().getTelefono());
//             tercerosMap.put("direccion", notaDebitoRequest.getTercero().getDireccion());
//             tercerosMap.put("correoElectronico", notaDebitoRequest.getTercero().getCorreoElectronico());

//             Map<String, Object> dataMap = Map.of(
//                     "referencia", notaDebitoRequest.getReferencia(),
//                     "cicloLectivo", notaDebitoRequest.getCicloLectivo(),
//                     "auxiliar", notaDebitoRequest.getAuxiliar(),
//                     "observacion", notaDebitoRequest.getObservacion(),
//                     "dependencia", notaDebitoRequest.getDependencia(),
//                     "cuentaConsignacion", notaDebitoRequest.getCuentaConsignacion(),
//                     "concepto", conceptoMap,
//                     "terceros", tercerosMap);

//             Map<String, Object> jsonMap = Map.of(
//                     "service", "NOTA_DEBITO",
//                     "data", dataMap);

//             ObjectMapper objectMapper = new ObjectMapper();
//             return objectMapper.writeValueAsString(jsonMap);
//         }

//         catch (RuntimeException e) {
//             throw new RuntimeException("Error generando el JSON de la factura", e);
//         }
//     }
// }
