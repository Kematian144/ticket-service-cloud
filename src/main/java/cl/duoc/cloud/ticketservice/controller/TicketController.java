package cl.duoc.cloud.ticketservice.controller;

import cl.duoc.cloud.ticketservice.dto.*;
import cl.duoc.cloud.ticketservice.model.TicketStatus;
import cl.duoc.cloud.ticketservice.service.EfsStorageService;
import cl.duoc.cloud.ticketservice.service.S3StorageService;
import cl.duoc.cloud.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;
    private final EfsStorageService efsStorageService;
    private final S3StorageService s3StorageService;

    public TicketController(TicketService ticketService, 
                            EfsStorageService efsStorageService, 
                            S3StorageService s3StorageService) {
        this.ticketService = ticketService;
        this.efsStorageService = efsStorageService;
        this.s3StorageService = s3StorageService;
    }

    // 1. Generar tickets: guarda en EFS y sube automatico a S3
    @PostMapping("/generate")
    public ResponseEntity<TicketResponseDTO> generateTicket(@Valid @RequestBody TicketRequestDTO request) {
        TicketResponseDTO response = ticketService.generateTicket(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Subir o resincronizar ticket a S3 manualmente
    @PostMapping("/{ticketId}/upload-s3")
    public ResponseEntity<TicketResponseDTO> uploadTicketToS3(@PathVariable String ticketId) {
        TicketResponseDTO response = ticketService.uploadTicketToS3(ticketId);
        return ResponseEntity.ok(response);
    }

    // 3. Descargar ticket por ID
    @GetMapping("/{ticketId}/download")
    public ResponseEntity<byte[]> downloadTicketById(@PathVariable String ticketId) {
        byte[] pdfBytes = ticketService.downloadTicketFile(ticketId, null, null);
        return buildDownloadResponse(pdfBytes, ticketId + ".pdf");
    }

    // 4. Descargar ticket por evento y usuario
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadTicketByParams(
            @RequestParam(required = false) String ticketId,
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String userId) {
        
        byte[] pdfBytes = ticketService.downloadTicketFile(ticketId, eventId, userId);
        String filename = (ticketId != null ? ticketId : (eventId + "_" + userId)) + ".pdf";
        return buildDownloadResponse(pdfBytes, filename);
    }

    // 5. Modificar detalles del ticket (actualiza en S3 y EFS)
    @PutMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable String ticketId,
            @RequestBody TicketUpdateDTO updateDTO) {
        TicketResponseDTO updated = ticketService.updateTicket(ticketId, updateDTO);
        return ResponseEntity.ok(updated);
    }

    // 6. Eliminar tickets especificos
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> deleteTicket(@PathVariable String ticketId) {
        ticketService.deleteTicket(ticketId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Ticket " + ticketId + " eliminado de S3, EFS y base de datos");
        return ResponseEntity.ok(response);
    }

    // 7. Consultar historial con filtros
    @GetMapping("/history")
    public ResponseEntity<List<TicketResponseDTO>> getTicketHistory(
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) TicketStatus status) {
        List<TicketResponseDTO> history = ticketService.getTicketHistory(eventId, userId, status);
        return ResponseEntity.ok(history);
    }

    // 8. Reportar estadisticas de ventas y reservas
    @GetMapping("/statistics/{eventId}")
    public ResponseEntity<EventStatisticsDTO> getEventStatistics(@PathVariable String eventId) {
        EventStatisticsDTO stats = ticketService.getEventStatistics(eventId);
        return ResponseEntity.ok(stats);
    }

    // 9. Estado y diagnostico para Postman
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ticket-service");
        health.put("efsDirectory", efsStorageService.getEfsDirectoryPath());
        health.put("s3Bucket", s3StorageService.getBucketName());
        health.put("s3SimulatedMode", s3StorageService.isSimulatedMode());
        return ResponseEntity.ok(health);
    }

    private ResponseEntity<byte[]> buildDownloadResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
