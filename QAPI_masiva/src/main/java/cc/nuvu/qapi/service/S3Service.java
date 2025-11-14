// package cc.nuvu.qapi.service;

// import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
// import software.amazon.awssdk.core.sync.RequestBody;
// import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.s3.S3Client;
// import software.amazon.awssdk.services.s3.model.PutObjectRequest;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import java.nio.charset.StandardCharsets;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;

// @Service
// public class S3Service {

//     private S3Client s3Client;

//     @Value("${aws.s3.bucketname}")
//     private String bucketName;

//     public S3Service() {
//         this.s3Client = S3Client.builder()
//                 .region(Region.US_EAST_1)
//                 .credentialsProvider(DefaultCredentialsProvider.create())
//                 .build();
//     }

//     public void uploadJson(String json, String tipo, String messageId) {

//         String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//         LocalDateTime now = LocalDateTime.now();
//         String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

//         // El ID ya viene de SQS, aseguramos que es el correcto
//         String fileName = timestamp + "-" + messageId + "-" + tipo + ".json";
//         String s3Key = "requests/" + fecha + "/" + fileName;

//         // Subir a S3
//         PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                 .bucket(bucketName)
//                 .key(s3Key)
//                 .contentType("application/json")
//                 .build();

//         s3Client.putObject(putObjectRequest, RequestBody.fromString(json, StandardCharsets.UTF_8));

//         System.out.println("Archivo guardado en S3: " + s3Key);
//     }
// }
