package cc.nuvu.qapi.service;
import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
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
        return response.secretString();
    }
}