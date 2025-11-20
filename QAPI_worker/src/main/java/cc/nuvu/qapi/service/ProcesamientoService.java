package cc.nuvu.qapi.service;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.nuvu.qapi.infraestructure.Oracle.mapper.OracleMapper;
import cc.nuvu.qapi.infraestructure.Oracle.service.OraclePackageService;
import cc.nuvu.qapi.infraestructure.dynamoDB.service.InfoRequestService;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.ARRAY;

@Service
@Slf4j
public class ProcesamientoService {

    private final InfoRequestService infoRequestService;
    private final OraclePackageService oraclePackageService;
    private final OracleMapper oracleMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProcesamientoService(
            InfoRequestService infoRequestService,
            OraclePackageService oraclePackageService,
            OracleMapper oracleMapper) {
        this.oraclePackageService = oraclePackageService;
        this.oracleMapper = oracleMapper;
        this.infoRequestService = infoRequestService;
    }

    // TODO: Implementar el llamado a los procedimientos restantes
    public void procesarServicio(String body, String messageId) throws JsonMappingException, JsonProcessingException {

        JsonNode jsonNode = objectMapper.readTree(body);

        log.info("📩 Recibido lote de facturas: {} | MessageId: {}", jsonNode.size(), messageId);
        log.info("Jsonnode" + jsonNode);

        JsonNode firstNode = jsonNode.get(0);

        String tipoServicioNodo = firstNode.get("servicio").asText();
        switch (tipoServicioNodo) {
            case "FACTURA" -> guardarMultiplesFacturas(jsonNode, messageId);
            case "PAGO" -> pagoDeFactura(jsonNode, messageId);
            case "PAGO-FACTURA" -> pagoYFactura(jsonNode, messageId);
            case "NOTA-DEBITO" -> notaDebito(jsonNode, messageId);
            case "NOTA-CREDITO" -> notaCredito(jsonNode, messageId);
        };
    }

    
    private void notaDebito(JsonNode notaDebitoNode, String messageId) {
        try {
            ARRAY facturaArray = oracleMapper.jsonArrayToFacturaTab(notaDebitoNode);
            oraclePackageService.llamarGuardarMultiples(facturaArray, 1);
            log.info("✅ Procesado correctamente. MessageId: {}", messageId);
            infoRequestService.guardarEstado(
                messageId,
                notaDebitoNode,
                "Exito procesando batch con ID: " + messageId);
                
            } catch (SQLException e) {
                
                log.error("❌ Error procesando mensaje SQS {}:", messageId, e);
                infoRequestService.guardarEstado(
                    messageId,
                    notaDebitoNode,
                    "Error procesando batch con ID: " + messageId + " por " + e.getCause().getMessage());
                    throw new RuntimeException(e);
                }
            }
            
            private void guardarMultiplesFacturas(JsonNode notaDebitoNode, String messageId) {
                try {
                    ARRAY facturaArray = oracleMapper.jsonArrayToFacturaTab(notaDebitoNode);
                    oraclePackageService.llamarGuardarMultiples(facturaArray, 0);
                    log.info("✅ Procesado correctamente. MessageId: {}", messageId);
                    infoRequestService.guardarEstado(
                        messageId,
                        notaDebitoNode,
                        "Exito procesando batch con ID: " + messageId);
                        
                    } catch (SQLException e) {
                        
                        log.error("❌ Error procesando mensaje SQS {}:", messageId, e);
                        infoRequestService.guardarEstado(
                            messageId,
                            notaDebitoNode,
                            "Error procesando batch con ID: " + messageId + " por " + e.getCause().getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                    
                    private void pagoDeFactura(JsonNode pagoDeFacturaNode, String messageId) {
                        
                    }

                    private void pagoYFactura(JsonNode pagoYFacturaNode, String messageId) {
                    }

                    private void notaCredito(JsonNode notaCreditoNode, String messageId) {

                    }
                }
                