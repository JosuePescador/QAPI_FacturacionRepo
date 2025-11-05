package cc.nuvu.qapi.infraestructure.dynamoDB.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.nuvu.qapi.presentation.dto.Request;

import java.io.IOException;

public class RequestMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Request request) {
        try {
            return mapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar Request a JSON", e);
        }
    }

    public static <T extends Request> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Error al deserializar Request desde JSON", e);
        }
    }
}
