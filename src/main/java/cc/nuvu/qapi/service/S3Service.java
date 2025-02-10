package cc.nuvu.qapi.service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName = "uca-test-facturacionmasiva-auditory";
    

    public S3Service() {
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1) // Cambia según la región de tu bucket
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void uploadJson(String json, String operacion, String messageId) {

        // Generar timestamp con formato AAAAMMDDHHMMSS
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Si no se proporciona un ID de mensaje, generar un UUID
        if (messageId == null || messageId.isEmpty()) {
            messageId = UUID.randomUUID().toString();
        }

        // 3. convención AAAAMMDDHHMMSS-{uuid}-{operacion}.json
        String fileName = String.format("%s-%s-%s.json", timestamp, messageId, operacion);

        // prefijo requests/AAAA/MM/DD/
        String s3Key = String.format("requests/%s/%s/%s/%s", 
                                     now.getYear(), 
                                     String.format("%02d", now.getMonthValue()), 
                                     String.format("%02d", now.getDayOfMonth()), 
                                     fileName);

        //Subir a S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("application/json")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(json, StandardCharsets.UTF_8));

        System.out.println("Archivo guardado en S3: " + s3Key);
    }
}
