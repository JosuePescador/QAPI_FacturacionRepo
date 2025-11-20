package cc.nuvu.qapi.infraestructure.Oracle.service;

import java.sql.Types;
import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;

import oracle.sql.ARRAY;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

@Service
public class OraclePackageService {

    private final JdbcTemplate jdbcTemplate;

    public OraclePackageService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> llamarGuardarMultiples(ARRAY facturasArray, int esCredito) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("FA_QFAMA") // Nombre del package
                    .withProcedureName("GUARDAMULTIPLESFACTURAS")

                    .declareParameters(
                            // P_FACTURAS -> FACTURA_TAB
                            new SqlParameter("P_FACTURAS", oracle.jdbc.OracleTypes.ARRAY, "FACTURA_TAB"));

            MapSqlParameterSource inParams = new MapSqlParameterSource()
                    .addValue("P_FACTURAS", facturasArray)
                    .addValue("P_ES_CREDITO", esCredito);

            return jdbcCall.execute(inParams);

        } catch (Exception e) {
            throw new RuntimeException("❌ Error llamando FA_QFAMA.guardaMultiplesFacturas", e);
        }
    }

    public Map<String, Object> llamarNotaDebito(ARRAY facturasArray, int esCredito) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("FA_QFAMA") // Nombre del package
                    .withProcedureName("GUARDAMULTIPLESFACTURAS")

                    .declareParameters(
                            // P_FACTURAS -> FACTURA_TAB
                            new SqlParameter("P_FACTURAS", oracle.jdbc.OracleTypes.ARRAY, "FACTURA_TAB"));

            MapSqlParameterSource inParams = new MapSqlParameterSource()
                    .addValue("P_FACTURAS", facturasArray)
                    .addValue("P_ES_CREDITO", esCredito);

            return jdbcCall.execute(inParams);

        } catch (Exception e) {
            throw new RuntimeException("❌ Error llamando FA_QFAMA.guardaMultiplesFacturas", e);
        }
    }

    public Map<String, Object> llamarProcedimientoAlmacenado(String packageName, String procedureName, Map<String,Object> parameters) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName(packageName)
                    .withProcedureName(procedureName);

            MapSqlParameterSource inParams = new MapSqlParameterSource()
            .addValues(parameters);

            return jdbcCall.execute(inParams);

        } catch (Exception e) {
            throw new RuntimeException("Error llamando al procedure: " + procedureName + " del package: " + packageName, e);
        }
    }
}
