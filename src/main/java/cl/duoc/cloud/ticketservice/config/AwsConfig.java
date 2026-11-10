package cl.duoc.cloud.ticketservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class AwsConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsConfig.class);

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.session-token:}")
    private String sessionToken;

    @Bean
    public S3Client s3Client() {
        try {
            S3ClientBuilder builder = S3Client.builder()
                    .region(Region.of(awsRegion));

            // Si se entregan credenciales explicitas de AWS Academy
            if (accessKeyId != null && !accessKeyId.isBlank() &&
                secretAccessKey != null && !secretAccessKey.isBlank()) {
                
                AwsCredentials credentials;
                if (sessionToken != null && !sessionToken.isBlank()) {
                    credentials = AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken);
                    log.info("S3Client configurado con Session Token de AWS");
                } else {
                    credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
                    log.info("S3Client configurado con credenciales basicas");
                }
                builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
            } else {
                // Credenciales por defecto del entorno o rol IAM
                builder.credentialsProvider(DefaultCredentialsProvider.create());
                log.info("S3Client configurado con DefaultCredentialsProvider");
            }

            return builder.build();
        } catch (Exception e) {
            log.warn("S3Client nativo no disponible, se usara modo simulado: {}", e.getMessage());
            return null;
        }
    }
}
