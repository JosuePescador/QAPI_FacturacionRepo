package cc.nuvu.qapi.service;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SecretService {
    private static final String SECRET_NAME = "uca/test/factmasiva/api/jwt-secret";
    private String cachedSecret;
    private LocalDate lastFetchDate;
    private final ReentrantLock lock = new ReentrantLock();

    public String getSecret() {
        LocalDate today = LocalDate.now();
        if (cachedSecret == null || lastFetchDate == null || !lastFetchDate.equals(today)) {
            lock.lock();
            try {
                if (cachedSecret == null || lastFetchDate == null || !lastFetchDate.equals(today)) {
                    cachedSecret = fetchSecretFromAWS();
                    lastFetchDate = today;
                }
            } finally {
                lock.unlock();
            }
        }

        return cachedSecret;
    }

    private String fetchSecretFromAWS() {
        SecretsManagerClient client = SecretsManagerClient.create();
        GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(SECRET_NAME).build();
        GetSecretValueResponse response = client.getSecretValue(request);

        String secretJson = response.secretString();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(secretJson);
            String secret = jsonNode.get("secret").asText();
            
            
            return secret;
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear el secreto JSON", e);
        }
    }

}































/* package cc.nuvu.qapi.service;
import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
@Service
public class SecretService {
    @Value("${secret.jwt}")
    private String cachedSecret;
    private static final String SECRET_NAME = "uca/test/factmasiva/api/jwt-secret";
    //private String cachedSecret;
    private LocalDate lastFetchDate;
    private final ReentrantLock lock = new ReentrantLock();
    public String getSecret() {
         LocalDate today = LocalDate.now();
        if (cachedSecret == null || lastFetchDate == null || !lastFetchDate.equals(today)) {
            lock.lock();
            try {
                if (cachedSecret == null || lastFetchDate == null || !lastFetchDate.equals(today)) {
                    cachedSecret = fetchSecretFromAWS();
                    lastFetchDate = today;
                }
            } finally {
                lock.unlock();
            }
        } 
        return cachedSecret;
    }
    private String fetchSecretFromAWS() {
        SecretsManagerClient client = SecretsManagerClient.create();
        GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(SECRET_NAME).build();
        GetSecretValueResponse response = client.getSecretValue(request);
        return response.secretString();
    }
} 
*/