package cc.nuvu.qapi.service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    public void uploadJson(String json, String tipo) {
        // Obtener la fecha actual en formato YYYYMMDD
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Generar timestamp único en milisegundos
        String timestamp = String.valueOf(System.currentTimeMillis());
        // Nombre del archivo en formato: YYYYMMDDHHMMSSSSS-tipo.json
        String fileName = fecha + timestamp + "-" + tipo + ".json";
        // Ruta completa en S3
        String s3Key = "requests/" + fecha + "/" + fileName;

        // Subir archivo a S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("application/json")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(json, StandardCharsets.UTF_8));

        System.out.println("Archivo guardado en S3: " + s3Key);
    }
}
