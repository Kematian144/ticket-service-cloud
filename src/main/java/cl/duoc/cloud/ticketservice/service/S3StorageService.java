package cl.duoc.cloud.ticketservice.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    @Autowired(required = false)
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name:duoc-cloud-tickets-bucket}")
    private String bucketName;

    @Value("${storage.s3.simulated-path:./s3_simulated_storage}")
    private String simulatedStoragePath;

    private boolean isSimulatedMode = false;
    private Path simulatedRoot;

    @PostConstruct
    public void init() {
        if (s3Client == null) {
            log.warn("S3Client no disponible. Se usa modo local simulado en: {}", simulatedStoragePath);
            isSimulatedMode = true;
        } else {
            try {
                // Verificar si existe el bucket
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
                log.info("Conexion exitosa con AWS S3 en el bucket: {}", bucketName);
            } catch (NoSuchBucketException e) {
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    log.info("Bucket {} creado en AWS S3", bucketName);
                } catch (Exception ex) {
                    log.warn("No se pudo crear bucket en AWS S3: {}. Usando modo simulado.", ex.getMessage());
                    isSimulatedMode = true;
                }
            } catch (Exception e) {
                log.warn("No se pudo conectar a AWS S3 ({}). Usando modo simulado.", e.getMessage());
                isSimulatedMode = true;
            }
        }

        if (isSimulatedMode) {
            try {
                simulatedRoot = Paths.get(simulatedStoragePath).toAbsolutePath().normalize();
                Files.createDirectories(simulatedRoot);
            } catch (IOException e) {
                log.error("Error al crear carpeta S3 simulada: {}", e.getMessage());
            }
        }
    }

    // Construye la clave con la estructura requerida: evento123/usuarioX/ticket001.pdf
    public String buildS3Key(String eventId, String userId, String ticketId) {
        return cleanText(eventId) + "/" + cleanText(userId) + "/" + cleanText(ticketId) + ".pdf";
    }

    private String cleanText(String text) {
        if (text == null) return "general";
        return text.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    // Sube el archivo a AWS S3
    public void uploadFile(String s3Key, byte[] data, String contentType) throws IOException {
        if (isSimulatedMode || s3Client == null) {
            Path targetFile = simulatedRoot.resolve(s3Key);
            Files.createDirectories(targetFile.getParent());
            Files.write(targetFile, data);
            log.info("[S3 SIMULADO] Archivo guardado: {}", s3Key);
            return;
        }

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType != null ? contentType : "application/pdf")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));
            log.info("[AWS S3] Archivo subido a s3://{}/{}", bucketName, s3Key);
        } catch (Exception e) {
            log.error("Error al subir a AWS S3, guardando en respaldo local: {}", e.getMessage());
            Path targetFile = Paths.get(simulatedStoragePath).resolve(s3Key);
            Files.createDirectories(targetFile.getParent());
            Files.write(targetFile, data);
        }
    }

    // Descarga el archivo desde AWS S3
    public byte[] downloadFile(String s3Key) throws IOException {
        if (isSimulatedMode || s3Client == null) {
            Path targetFile = simulatedRoot.resolve(s3Key);
            if (!Files.exists(targetFile)) {
                throw new IOException("El archivo " + s3Key + " no existe en el almacenamiento S3");
            }
            return Files.readAllBytes(targetFile);
        }

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getRequest);
            log.info("[AWS S3] Archivo descargado de s3://{}/{}", bucketName, s3Key);
            return objectBytes.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new IOException("El archivo " + s3Key + " no existe en el bucket: " + bucketName);
        } catch (Exception e) {
            Path fallbackFile = Paths.get(simulatedStoragePath).resolve(s3Key);
            if (Files.exists(fallbackFile)) {
                return Files.readAllBytes(fallbackFile);
            }
            throw new IOException("Error al descargar de S3: " + e.getMessage(), e);
        }
    }

    // Modifica o actualiza el archivo en S3
    public void updateFile(String s3Key, byte[] newContent, String contentType) throws IOException {
        uploadFile(s3Key, newContent, contentType);
    }

    // Elimina el archivo de S3
    public boolean deleteFile(String s3Key) {
        boolean deleted = false;
        if (isSimulatedMode || s3Client == null) {
            try {
                Path targetFile = simulatedRoot.resolve(s3Key);
                deleted = Files.deleteIfExists(targetFile);
                return deleted;
            } catch (IOException e) {
                return false;
            }
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("[AWS S3] Archivo eliminado de s3://{}/{}", bucketName, s3Key);
            deleted = true;
        } catch (Exception e) {
            log.error("Error al eliminar de AWS S3: {}", e.getMessage());
        }

        try {
            Files.deleteIfExists(Paths.get(simulatedStoragePath).resolve(s3Key));
        } catch (Exception ignored) {}

        return deleted;
    }

    public boolean isSimulatedMode() {
        return isSimulatedMode;
    }

    public String getBucketName() {
        return bucketName;
    }
}
