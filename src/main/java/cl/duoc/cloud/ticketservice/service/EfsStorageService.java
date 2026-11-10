package cl.duoc.cloud.ticketservice.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class EfsStorageService {

    private static final Logger log = LoggerFactory.getLogger(EfsStorageService.class);

    @Value("${storage.efs.path:/app/efs}")
    private String configuredEfsPath;

    @Value("${storage.efs.local-fallback:./efs_storage}")
    private String localFallbackPath;

    private Path activeEfsDirectory;

    @PostConstruct
    public void init() {
        try {
            Path targetPath = Paths.get(configuredEfsPath);
            if (isPathUsable(targetPath)) {
                activeEfsDirectory = targetPath;
            } else {
                // Modo local de respaldo cuando no existe /app/efs
                activeEfsDirectory = Paths.get(localFallbackPath).toAbsolutePath().normalize();
                Files.createDirectories(activeEfsDirectory);
            }

            if (!Files.exists(activeEfsDirectory)) {
                Files.createDirectories(activeEfsDirectory);
            }
            log.info("Directorio EFS listo en: {}", activeEfsDirectory.toAbsolutePath());
        } catch (Exception e) {
            log.error("Error al iniciar EFS: {}", e.getMessage());
            activeEfsDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "efs_storage");
            try {
                Files.createDirectories(activeEfsDirectory);
            } catch (IOException ignored) {}
        }
    }

    private boolean isPathUsable(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            return Files.isWritable(path);
        } catch (Exception e) {
            return false;
        }
    }

    // Guarda el archivo de ticket temporalmente en EFS
    public String saveTicketFile(String filename, byte[] content) throws IOException {
        Path filePath = activeEfsDirectory.resolve(filename);
        Files.write(filePath, content);
        log.info("Ticket guardado temporalmente en EFS: {}", filePath.toAbsolutePath());
        return filePath.toAbsolutePath().toString();
    }

    // Lee el archivo de ticket desde EFS
    public byte[] readTicketFile(String filename) throws IOException {
        Path filePath = activeEfsDirectory.resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IOException("El archivo " + filename + " no existe en EFS");
        }
        return Files.readAllBytes(filePath);
    }

    // Elimina el archivo de EFS
    public boolean deleteTicketFile(String filename) {
        try {
            Path filePath = activeEfsDirectory.resolve(filename);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Archivo {} borrado de EFS", filename);
            }
            return deleted;
        } catch (IOException e) {
            log.warn("No se pudo borrar el archivo {} de EFS: {}", filename, e.getMessage());
            return false;
        }
    }

    public boolean fileExists(String filename) {
        return Files.exists(activeEfsDirectory.resolve(filename));
    }

    public String getEfsDirectoryPath() {
        return activeEfsDirectory != null ? activeEfsDirectory.toAbsolutePath().toString() : configuredEfsPath;
    }
}
