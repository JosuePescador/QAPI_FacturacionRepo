package cc.nuvu.qapi.infraestructure.Oracle.mapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.JsonNode;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import org.springframework.stereotype.Component;

@Component
public class OracleMapper {

    private final DataSource dataSource;

    public OracleMapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Convierte un JSON array de facturas en FACTURA_TAB (colección de FACTURA_OBJ)
     */
    public ARRAY jsonArrayToFacturaTab(JsonNode jsonArray) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            oracle.jdbc.OracleConnection oracleConn = conn.unwrap(oracle.jdbc.OracleConnection.class);

            Struct[] facturasStruct = new Struct[jsonArray.size()];
            int i = 0;
            for (JsonNode jsonFactura : jsonArray) {
                facturasStruct[i++] = jsonToFacturaObj(oracleConn, jsonFactura);
            }

            ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor("FACTURA_TAB", oracleConn);
            return new ARRAY(descriptor, oracleConn, facturasStruct);
        }
    }

    /**
     * Convierte un JSON individual en un FACTURA_OBJ
     */
    private Struct jsonToFacturaObj(oracle.jdbc.OracleConnection conn, JsonNode json) throws SQLException {
        // 1️⃣ Convertir conceptos
        Struct[] cptosStructs = jsonToConceptosStructs(conn, json.get("listado_cptos_principales"));
        ARRAY cptosArray = new ARRAY(
                ArrayDescriptor.createDescriptor("CONCEPTO_PRINCIPAL_TAB", conn),
                conn,
                cptosStructs
        );

        // 2️⃣ Convertir tercero en TERCERO_TAB (colección de 1)
        ARRAY terceroArray = jsonToTerceroTab(conn, json.get("tercero"));

        // 3️⃣ Crear FACTURA_OBJ (orden debe coincidir con Oracle)
        return conn.createStruct(
                "FACTURA_OBJ",
                new Object[]{
                        json.get("referencia").asText(),
                        json.get("cicloLectivo").asText(),
                        json.get("nit_tercero").asLong(),
                        json.get("cuentaConsignacion").asLong(),
                        cptosArray,
                        terceroArray
                }
        );
    }

    /**
     * Envuelve un TERCERO_OBJ dentro de un TERCERO_TAB (colección)
     */
    private ARRAY jsonToTerceroTab(oracle.jdbc.OracleConnection conn, JsonNode tercero) throws SQLException {
        if (tercero == null || tercero.isNull())
            return null;

        Struct terceroStruct = conn.createStruct(
                "TERCERO_OBJ",
                new Object[]{
                        tercero.get("claseIdentificacion").asText(),
                        tercero.get("numeroIdentificacion").asLong(),
                        tercero.get("descripcionAuxiliar").asText(),
                        tercero.get("naturalJuridica").asText(),
                        tercero.get("tipoAuxiliar").asText(),
                        tercero.get("tipoRetencion").asText(),
                        tercero.get("codigoPais").asLong(),
                        tercero.get("codigoDepartamento").asLong(),
                        tercero.get("codigoCiudad").asLong(),
                        tercero.get("celular").asText(),
                        tercero.get("telefono").asText(),
                        tercero.get("direccion").asText(),
                        tercero.get("correoElectronico").asText()
                }
        );

        Object[] data = new Object[]{terceroStruct};

        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor("TERCERO_TAB", conn);
        return new ARRAY(descriptor, conn, data);
    }

    /**
     * Convierte listado_cptos_principales → CONCEPTO_PRINCIPAL_TAB
     */
    private Struct[] jsonToConceptosStructs(oracle.jdbc.OracleConnection conn, JsonNode lista) throws SQLException {
        Struct[] array = new Struct[lista.size()];
        int i = 0;
        for (JsonNode item : lista) {
            array[i++] = conn.createStruct(
                    "CONCEPTO_PRINCIPAL_OBJ",
                    new Object[]{
                            item.get("conceptoFacturacion").asText(),
                            item.get("cantidadUnidades").asLong(),
                            item.get("valorUnitario").asDouble()
                    }
            );
        }
        return array;
    }
}
