// package cc.nuvu.qapi.service;

// import java.util.ArrayList;
// import java.util.List;

// import org.hibernate.Session;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.google.gson.JsonElement;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;

// import cc.nuvu.qapi.model.ConceptoPrincipal;
// import cc.nuvu.qapi.model.Tercero;
// import jakarta.persistence.EntityManager;
// import jakarta.persistence.PersistenceContext;

// import java.sql.CallableStatement;
// import java.sql.Struct;

// import oracle.jdbc.OracleConnection;
// import oracle.sql.ARRAY;
// import oracle.sql.ArrayDescriptor;

// import java.sql.SQLException;

// @Service
// public class ProcedureCallerImpl implements ProcedureCaller {

//     @Autowired
//     @PersistenceContext
//     private EntityManager em;

//     @Autowired
//     private LoggerService logger;

//     @Autowired
//     private LoggerServiceData loggerd;

//     private static final String ORACLE_TYPE_SCHEMA = "VU_SFI"; // Ajustar si los tipos viven en otro esquema

//     private String qualifyType(String typeName) {
//         if (typeName == null)
//             return null;
//         return typeName.contains(".") ? typeName : ORACLE_TYPE_SCHEMA + "." + typeName;
//     }

//     private Struct createStruct(OracleConnection conn, String sqlTypeName, Object[] attrs) throws SQLException {
//         try {
//             return conn.createStruct(sqlTypeName, attrs);
//         } catch (SQLException e) {
//             // Reintenta con esquema calificado
//             return conn.createStruct(qualifyType(sqlTypeName), attrs);
//         }
//     }

//     private java.sql.Array createArray(OracleConnection conn, String arrayTypeName, Object[] elements)
//             throws SQLException {
//         try {
//             return conn.createOracleArray(arrayTypeName, elements);
//         } catch (SQLException e) {
//             // Reintenta con esquema calificado
//             return conn.createOracleArray(qualifyType(arrayTypeName), elements);
//         }
//     }

//     @Override
//     public boolean callProcedure(String name, MensajeBody mensajeBody) {
//         JsonParser jsonParser = new JsonParser();
//         JsonObject body = jsonParser.parse(mensajeBody.getData()).getAsJsonObject();

//         if ("FACTURA".equals(name)) {
//             try {
//                 // Caso nuevo: arreglo de facturas => FA_QFAMA.guardaMultiplesFacturas
//                 if (body.has("facturas") && body.get("facturas").isJsonArray()) {
//                     Session session = em.unwrap(Session.class);
//                     OracleConnection oracleConnection = session
//                             .doReturningWork(conn -> conn.unwrap(OracleConnection.class));

//                     List<Struct> facturasStructList = new ArrayList<>();

//                     for (JsonElement facturaEl : body.getAsJsonArray("facturas")) {
//                         JsonObject f = facturaEl.getAsJsonObject();

//                         // 1) Conceptos principales
//                         List<ConceptoPrincipal> conceptoPrincipalList = new ArrayList<>();
//                         if (f.has("listado_cptos_principales")) {
//                             for (JsonElement conceptoElement : f.getAsJsonArray("listado_cptos_principales")) {
//                                 JsonObject cObj = conceptoElement.getAsJsonObject();
//                                 ConceptoPrincipal c = new ConceptoPrincipal();
//                                 c.setConceptoFacturacion(cObj.get("conceptoFacturacion").getAsString());
//                                 c.setCantidadUnidades(cObj.get("cantidadUnidades").getAsInt());
//                                 c.setValorUnitario(cObj.get("valorUnitario").getAsLong());
//                                 conceptoPrincipalList.add(c);
//                             }
//                         }

//                         Struct[] conceptosStruct = new Struct[conceptoPrincipalList.size()];
//                         for (int i = 0; i < conceptoPrincipalList.size(); i++) {
//                             ConceptoPrincipal c = conceptoPrincipalList.get(i);
//                             Object[] attrs = new Object[] {
//                                     c.getConceptoFacturacion(),
//                                     c.getCantidadUnidades(),
//                                     c.getValorUnitario()
//                             };
//                             conceptosStruct[i] = createStruct(oracleConnection, "CONCEPTO_PRINCIPAL_OBJ", attrs);
//                         }
//                         java.sql.Array conceptosArray = createArray(oracleConnection, "CONCEPTO_PRINCIPAL_TAB",
//                                 conceptosStruct);

//                         // 2) Tercero (TAB con un solo objeto)
//                         JsonObject tObj = f.getAsJsonObject("tercero");
//                         Tercero t = new Tercero();
//                         t.setClaseIdentificacion(tObj.get("claseIdentificacion").getAsString());
//                         t.setNumeroIdentificacion(tObj.get("numeroIdentificacion").getAsLong());
//                         t.setDescripcionAuxiliar(tObj.get("descripcionAuxiliar").getAsString());
//                         t.setNaturalJuridica(tObj.get("naturalJuridica").getAsString());
//                         t.setTipoAuxiliar(tObj.get("tipoAuxiliar").getAsString());
//                         t.setTipoRetencion(tObj.get("tipoRetencion").getAsString());
//                         t.setCodigoPais(tObj.get("codigoPais").getAsLong());
//                         t.setCodigoDepartamento(tObj.get("codigoDepartamento").getAsLong());
//                         t.setCodigoCiudad(tObj.get("codigoCiudad").getAsInt());
//                         t.setNumeroCelular(tObj.get("numeroCelular").getAsString());
//                         t.setNumeroTelefonico(tObj.get("numeroTelefonico").getAsString());
//                         t.setDireccion(tObj.get("direccion").getAsString());
//                         t.setCorreoElectronico(tObj.get("correoElectronico").getAsString());

//                         Object[] terceroAttrs = new Object[] {
//                                 t.getClaseIdentificacion(),
//                                 t.getNumeroIdentificacion(),
//                                 t.getDescripcionAuxiliar(),
//                                 t.getNaturalJuridica(),
//                                 t.getTipoAuxiliar(),
//                                 t.getTipoRetencion(),
//                                 t.getCodigoPais(),
//                                 t.getCodigoDepartamento(),
//                                 t.getCodigoCiudad(),
//                                 t.getNumeroCelular(),
//                                 t.getNumeroTelefonico(),
//                                 t.getDireccion(),
//                                 t.getCorreoElectronico()
//                         };
//                         Struct terceroStruct = createStruct(oracleConnection, "TERCERO_OBJ", terceroAttrs);
//                         java.sql.Array terceroArray = createArray(oracleConnection, "TERCERO_TAB",
//                                 new Struct[] { terceroStruct });

//                         // 3) Armar FACTURA_OBJ
//                         Object[] facturaAttrs = new Object[] {
//                                 f.get("referencia").getAsString(),
//                                 f.get("cicloLectivo").getAsString(),
//                                 f.get("nit_tercero").getAsLong(),
//                                 f.get("cuentaConsignacion").getAsLong(),
//                                 conceptosArray,
//                                 terceroArray
//                         };
//                         Struct facturaStruct = createStruct(oracleConnection, "FACTURA_OBJ", facturaAttrs);
//                         facturasStructList.add(facturaStruct);
//                     }

//                     java.sql.Array facturasArray = createArray(oracleConnection, "FACTURA_TAB",
//                             facturasStructList.toArray(new Struct[0]));

//                     CallableStatement cs = oracleConnection
//                             .prepareCall("{call VU_SFI.FA_QFAMA.guardaMultiplesFacturas(?)}");
//                     cs.setObject(1, facturasArray);

//                     loggerd.logToS3(name, mensajeBody.getData(), true, null);
//                     cs.execute();
//                     cs.close();
//                     loggerd.log("Procedimiento " + name + " (múltiple) ejecutado con éxito.");
//                     return true;
//                 }

//                 // Fallback: compatibilidad con una sola factura (estructura anterior)
//                 Tercero p_listado_tercero = new Tercero();
//                 p_listado_tercero.setClaseIdentificacion(body.get("claseIdentificacion").getAsString());
//                 p_listado_tercero.setNumeroIdentificacion(body.get("numeroIdentificacion").getAsLong());
//                 p_listado_tercero.setDescripcionAuxiliar(body.get("descripcionAuxiliar").getAsString());
//                 p_listado_tercero.setNaturalJuridica(body.get("naturalJuridica").getAsString());
//                 p_listado_tercero.setTipoAuxiliar(body.get("tipoAuxiliar").getAsString());
//                 p_listado_tercero.setTipoRetencion(body.get("tipoRetencion").getAsString());
//                 p_listado_tercero.setCodigoPais(body.get("codigoPais").getAsLong());
//                 p_listado_tercero.setCodigoDepartamento(body.get("codigoDepartamento").getAsLong());
//                 p_listado_tercero.setCodigoCiudad(body.get("codigoCiudad").getAsInt());
//                 p_listado_tercero.setNumeroCelular(body.get("numeroCelular").getAsString());
//                 p_listado_tercero.setNumeroTelefonico(body.get("numeroTelefonico").getAsString());
//                 p_listado_tercero.setDireccion(body.get("direccion").getAsString());
//                 p_listado_tercero.setCorreoElectronico(body.get("correoElectronico").getAsString());

//                 List<ConceptoPrincipal> conceptoPrincipalList = new ArrayList<>();
//                 if (body.has("conceptosPrincipales")) {
//                     for (JsonElement conceptoElement : body.getAsJsonArray("conceptosPrincipales")) {
//                         JsonObject conceptoObj = conceptoElement.getAsJsonObject();
//                         ConceptoPrincipal concepto = new ConceptoPrincipal();
//                         concepto.setConceptoFacturacion(conceptoObj.get("conceptoFacturacion").getAsString());
//                         concepto.setCantidadUnidades(conceptoObj.get("cantidadUnidades").getAsInt());
//                         concepto.setValorUnitario(conceptoObj.get("valorUnitario").getAsLong());
//                         conceptoPrincipalList.add(concepto);
//                     }
//                 }

//                 List<Tercero> terceroList = new ArrayList<>();
//                 terceroList.add(p_listado_tercero);

//                 Session session = em.unwrap(Session.class);
//                 OracleConnection oracleConnection = session
//                         .doReturningWork(conn -> conn.unwrap(OracleConnection.class));

//                 Struct[] conceptosStruct = new Struct[conceptoPrincipalList.size()];
//                 for (int i = 0; i < conceptoPrincipalList.size(); i++) {
//                     ConceptoPrincipal c = conceptoPrincipalList.get(i);
//                     Object[] attrs = new Object[] {
//                             c.getConceptoFacturacion(),
//                             c.getCantidadUnidades(),
//                             c.getValorUnitario()
//                     };
//                     conceptosStruct[i] = createStruct(oracleConnection, "CONCEPTO_PRINCIPAL_OBJ", attrs);
//                 }
//                 java.sql.Array conceptosArray = createArray(oracleConnection, "CONCEPTO_PRINCIPAL_TAB",
//                         conceptosStruct);

//                 Struct[] terceroStruct = new Struct[terceroList.size()];
//                 for (int i = 0; i < terceroList.size(); i++) {
//                     Tercero t = terceroList.get(i);
//                     Object[] attrs = new Object[] {
//                             t.getClaseIdentificacion(),
//                             t.getNumeroIdentificacion(),
//                             t.getDescripcionAuxiliar(),
//                             t.getNaturalJuridica(),
//                             t.getTipoAuxiliar(),
//                             t.getTipoRetencion(),
//                             t.getCodigoPais(),
//                             t.getCodigoDepartamento(),
//                             t.getCodigoCiudad(),
//                             t.getNumeroCelular(),
//                             t.getNumeroTelefonico(),
//                             t.getDireccion(),
//                             t.getCorreoElectronico()
//                     };
//                     terceroStruct[i] = createStruct(oracleConnection, "TERCERO_OBJ", attrs);
//                 }
//                 java.sql.Array terceroArray = createArray(oracleConnection, "TERCERO_TAB", terceroStruct);

//                 CallableStatement cs = oracleConnection.prepareCall(
//                         "{call VU_SFI.FA_QFAMA.guardaFactura(?, ?, ?, ?, ?, ?)}");
//                 cs.setString(1, body.get("referencia").getAsString());
//                 cs.setString(2, body.get("cicloLectivo").getAsString());
//                 cs.setLong(3, body.get("nit_tercero").getAsLong());
//                 cs.setLong(4, body.get("cuentaConsignacion").getAsLong());
//                 cs.setObject(5, conceptosArray);
//                 cs.setObject(6, terceroArray);

//                 loggerd.logToS3(name, mensajeBody.getData(), true, null);
//                 cs.execute();
//                 cs.close();
//                 loggerd.log("Procedimiento " + name + " ejecutado con éxito.");

//                 return true;
//             } catch (Exception e) {
//                 String extra = "";
//                 if (e instanceof java.sql.SQLException sqlEx) {
//                     extra = " SQLState=" + sqlEx.getSQLState() + ", VendorCode=" + sqlEx.getErrorCode();
//                 }
//                 logger.errorLog("Error al llamar el procedimiento " + name);
//                 logger.errorLog("Error: " + e.getClass().getName() + ": " + e.getMessage() + extra);
//                 loggerd.logToS3(name, mensajeBody.getData(), false, e.getMessage());
//                 return false;
//             }
//         } else if ("PAGO".equals(name)) {
//             return false;
//         } else if ("PAGO-FACTURA".equals(name)) {
//             return false;
//         } else if ("NOTA-CREDITO".equals(name)) {
//             return false;
//         } else if ("NOTA-DEBITO".equals(name)) {
//             try {
//                 // Caso nuevo: arreglo de facturas => FA_QFAMA.guardaMultiplesFacturas
//                 if (body.has("notasdebito") && body.get("notasdebito").isJsonArray()) {
//                     Session session = em.unwrap(Session.class);
//                     OracleConnection oracleConnection = session
//                             .doReturningWork(conn -> conn.unwrap(OracleConnection.class));

//                     List<Struct> facturasStructList = new ArrayList<>();

//                     for (JsonElement facturaEl : body.getAsJsonArray("notasdebito")) {
//                         JsonObject f = facturaEl.getAsJsonObject();

//                         // 1) Conceptos principales
//                         List<ConceptoPrincipal> conceptoPrincipalList = new ArrayList<>();
//                         if (f.has("listado_cptos_principales")) {
//                             for (JsonElement conceptoElement : f.getAsJsonArray("listado_cptos_principales")) {
//                                 JsonObject cObj = conceptoElement.getAsJsonObject();
//                                 ConceptoPrincipal c = new ConceptoPrincipal();
//                                 c.setConceptoFacturacion(cObj.get("conceptoFacturacion").getAsString());
//                                 c.setCantidadUnidades(cObj.get("cantidadUnidades").getAsInt());
//                                 c.setValorUnitario(cObj.get("valorUnitario").getAsLong());
//                                 conceptoPrincipalList.add(c);
//                             }
//                         }

//                         Struct[] conceptosStruct = new Struct[conceptoPrincipalList.size()];
//                         for (int i = 0; i < conceptoPrincipalList.size(); i++) {
//                             ConceptoPrincipal c = conceptoPrincipalList.get(i);
//                             Object[] attrs = new Object[] {
//                                     c.getConceptoFacturacion(),
//                                     c.getCantidadUnidades(),
//                                     c.getValorUnitario()
//                             };
//                             conceptosStruct[i] = createStruct(oracleConnection, "CONCEPTO_PRINCIPAL_OBJ", attrs);
//                         }
//                         java.sql.Array conceptosArray = createArray(oracleConnection, "CONCEPTO_PRINCIPAL_TAB",
//                                 conceptosStruct);

//                         // 2) Tercero (TAB con un solo objeto)
//                         JsonObject tObj = f.getAsJsonObject("tercero");
//                         Tercero t = new Tercero();
//                         t.setClaseIdentificacion(tObj.get("claseIdentificacion").getAsString());
//                         t.setNumeroIdentificacion(tObj.get("numeroIdentificacion").getAsLong());
//                         t.setDescripcionAuxiliar(tObj.get("descripcionAuxiliar").getAsString());
//                         t.setNaturalJuridica(tObj.get("naturalJuridica").getAsString());
//                         t.setTipoAuxiliar(tObj.get("tipoAuxiliar").getAsString());
//                         t.setTipoRetencion(tObj.get("tipoRetencion").getAsString());
//                         t.setCodigoPais(tObj.get("codigoPais").getAsLong());
//                         t.setCodigoDepartamento(tObj.get("codigoDepartamento").getAsLong());
//                         t.setCodigoCiudad(tObj.get("codigoCiudad").getAsInt());
//                         t.setNumeroCelular(tObj.get("numeroCelular").getAsString());
//                         t.setNumeroTelefonico(tObj.get("numeroTelefonico").getAsString());
//                         t.setDireccion(tObj.get("direccion").getAsString());
//                         t.setCorreoElectronico(tObj.get("correoElectronico").getAsString());

//                         Object[] terceroAttrs = new Object[] {
//                                 t.getClaseIdentificacion(),
//                                 t.getNumeroIdentificacion(),
//                                 t.getDescripcionAuxiliar(),
//                                 t.getNaturalJuridica(),
//                                 t.getTipoAuxiliar(),
//                                 t.getTipoRetencion(),
//                                 t.getCodigoPais(),
//                                 t.getCodigoDepartamento(),
//                                 t.getCodigoCiudad(),
//                                 t.getNumeroCelular(),
//                                 t.getNumeroTelefonico(),
//                                 t.getDireccion(),
//                                 t.getCorreoElectronico()
//                         };
//                         Struct terceroStruct = createStruct(oracleConnection, "TERCERO_OBJ", terceroAttrs);
//                         java.sql.Array terceroArray = createArray(oracleConnection, "TERCERO_TAB",
//                                 new Struct[] { terceroStruct });

//                         // 3) Armar FACTURA_OBJ
//                         Object[] facturaAttrs = new Object[] {
//                                 f.get("referencia").getAsString(),
//                                 f.get("cicloLectivo").getAsString(),
//                                 f.get("nit_tercero").getAsLong(),
//                                 f.get("cuentaConsignacion").getAsLong(),
//                                 conceptosArray,
//                                 terceroArray
//                         };
//                         Struct facturaStruct = createStruct(oracleConnection, "FACTURA_OBJ", facturaAttrs);
//                         facturasStructList.add(facturaStruct);
//                     }

//                     java.sql.Array facturasArray = createArray(oracleConnection, "FACTURA_TAB",
//                             facturasStructList.toArray(new Struct[0]));

//                     CallableStatement cs = oracleConnection
//                             .prepareCall("{call VU_SFI.FA_QFAMA.guardaMultiplesFacturas(?, ?)}");
//                     cs.setObject(1, facturasArray);
//                     cs.setInt(2, 1);

//                     loggerd.logToS3(name, mensajeBody.getData(), true, null);
//                     cs.execute();
//                     cs.close();
//                     loggerd.log("Procedimiento " + name + " (múltiple) ejecutado con éxito.");
//                     return true;
//                 }

//                 // Fallback: compatibilidad con una sola factura (estructura anterior)
//                 Tercero p_listado_tercero = new Tercero();
//                 p_listado_tercero.setClaseIdentificacion(body.get("claseIdentificacion").getAsString());
//                 p_listado_tercero.setNumeroIdentificacion(body.get("numeroIdentificacion").getAsLong());
//                 p_listado_tercero.setDescripcionAuxiliar(body.get("descripcionAuxiliar").getAsString());
//                 p_listado_tercero.setNaturalJuridica(body.get("naturalJuridica").getAsString());
//                 p_listado_tercero.setTipoAuxiliar(body.get("tipoAuxiliar").getAsString());
//                 p_listado_tercero.setTipoRetencion(body.get("tipoRetencion").getAsString());
//                 p_listado_tercero.setCodigoPais(body.get("codigoPais").getAsLong());
//                 p_listado_tercero.setCodigoDepartamento(body.get("codigoDepartamento").getAsLong());
//                 p_listado_tercero.setCodigoCiudad(body.get("codigoCiudad").getAsInt());
//                 p_listado_tercero.setNumeroCelular(body.get("numeroCelular").getAsString());
//                 p_listado_tercero.setNumeroTelefonico(body.get("numeroTelefonico").getAsString());
//                 p_listado_tercero.setDireccion(body.get("direccion").getAsString());
//                 p_listado_tercero.setCorreoElectronico(body.get("correoElectronico").getAsString());

//                 List<ConceptoPrincipal> conceptoPrincipalList = new ArrayList<>();
//                 if (body.has("conceptosPrincipales")) {
//                     for (JsonElement conceptoElement : body.getAsJsonArray("conceptosPrincipales")) {
//                         JsonObject conceptoObj = conceptoElement.getAsJsonObject();
//                         ConceptoPrincipal concepto = new ConceptoPrincipal();
//                         concepto.setConceptoFacturacion(conceptoObj.get("conceptoFacturacion").getAsString());
//                         concepto.setCantidadUnidades(conceptoObj.get("cantidadUnidades").getAsInt());
//                         concepto.setValorUnitario(conceptoObj.get("valorUnitario").getAsLong());
//                         conceptoPrincipalList.add(concepto);
//                     }
//                 }

//                 List<Tercero> terceroList = new ArrayList<>();
//                 terceroList.add(p_listado_tercero);

//                 Session session = em.unwrap(Session.class);
//                 OracleConnection oracleConnection = session
//                         .doReturningWork(conn -> conn.unwrap(OracleConnection.class));

//                 Struct[] conceptosStruct = new Struct[conceptoPrincipalList.size()];
//                 for (int i = 0; i < conceptoPrincipalList.size(); i++) {
//                     ConceptoPrincipal c = conceptoPrincipalList.get(i);
//                     Object[] attrs = new Object[] {
//                             c.getConceptoFacturacion(),
//                             c.getCantidadUnidades(),
//                             c.getValorUnitario()
//                     };
//                     conceptosStruct[i] = createStruct(oracleConnection, "CONCEPTO_PRINCIPAL_OBJ", attrs);
//                 }
//                 java.sql.Array conceptosArray = createArray(oracleConnection, "CONCEPTO_PRINCIPAL_TAB",
//                         conceptosStruct);

//                 Struct[] terceroStruct = new Struct[terceroList.size()];
//                 for (int i = 0; i < terceroList.size(); i++) {
//                     Tercero t = terceroList.get(i);
//                     Object[] attrs = new Object[] {
//                             t.getClaseIdentificacion(),
//                             t.getNumeroIdentificacion(),
//                             t.getDescripcionAuxiliar(),
//                             t.getNaturalJuridica(),
//                             t.getTipoAuxiliar(),
//                             t.getTipoRetencion(),
//                             t.getCodigoPais(),
//                             t.getCodigoDepartamento(),
//                             t.getCodigoCiudad(),
//                             t.getNumeroCelular(),
//                             t.getNumeroTelefonico(),
//                             t.getDireccion(),
//                             t.getCorreoElectronico()
//                     };
//                     terceroStruct[i] = createStruct(oracleConnection, "TERCERO_OBJ", attrs);
//                 }
//                 java.sql.Array terceroArray = createArray(oracleConnection, "TERCERO_TAB", terceroStruct);

//                 CallableStatement cs = oracleConnection.prepareCall(
//                         "{call VU_SFI.FA_QFAMA.guardaFactura(?, ?, ?, ?, ?, ?, ? )}");
//                 cs.setString(1, body.get("referencia").getAsString());
//                 cs.setString(2, body.get("cicloLectivo").getAsString());
//                 cs.setLong(3, body.get("nit_tercero").getAsLong());
//                 cs.setLong(4, body.get("cuentaConsignacion").getAsLong());
//                 cs.setObject(5, conceptosArray);
//                 cs.setObject(6, terceroArray);
//                 cs.setInt(7, 1);

//                 loggerd.logToS3(name, mensajeBody.getData(), true, null);
//                 cs.execute();
//                 cs.close();
//                 loggerd.log("Procedimiento " + name + " ejecutado con éxito.");

//                 return true;
//             } catch (Exception e) {
//                 String extra = "";
//                 if (e instanceof java.sql.SQLException sqlEx) {
//                     extra = " SQLState=" + sqlEx.getSQLState() + ", VendorCode=" + sqlEx.getErrorCode();
//                 }
//                 logger.errorLog("Error al llamar el procedimiento " + name);
//                 logger.errorLog("Error: " + e.getClass().getName() + ": " + e.getMessage() + extra);
//                 loggerd.logToS3(name, mensajeBody.getData(), false, e.getMessage());
//                 return false;
//             }
//         } else {
//             throw new IllegalArgumentException("Procedure name " + name + " is not recognized.");
//         }
//     }
// }
