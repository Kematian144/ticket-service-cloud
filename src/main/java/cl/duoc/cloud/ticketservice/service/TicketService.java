package cl.duoc.cloud.ticketservice.service;

import cl.duoc.cloud.ticketservice.dto.*;
import cl.duoc.cloud.ticketservice.model.Ticket;
import cl.duoc.cloud.ticketservice.model.TicketStatus;
import cl.duoc.cloud.ticketservice.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final EfsStorageService efsStorageService;
    private final S3StorageService s3StorageService;
    private final PdfTicketGeneratorService pdfTicketGeneratorService;

    public TicketService(TicketRepository ticketRepository,
                         EfsStorageService efsStorageService,
                         S3StorageService s3StorageService,
                         PdfTicketGeneratorService pdfTicketGeneratorService) {
        this.ticketRepository = ticketRepository;
        this.efsStorageService = efsStorageService;
        this.s3StorageService = s3StorageService;
        this.pdfTicketGeneratorService = pdfTicketGeneratorService;
    }

    // Genera el ticket, lo guarda en EFS y lo sube automatico a S3
    public TicketResponseDTO generateTicket(TicketRequestDTO request) {
        String ticketId = "tkt-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketId);
        ticket.setEventId(request.getEventId());
        ticket.setEventName(request.getEventName());
        ticket.setUserId(request.getUserId());
        ticket.setUserName(request.getUserName());
        ticket.setUserEmail(request.getUserEmail());
        ticket.setSeatNumber(request.getSeatNumber());
        ticket.setPrice(request.getPrice());
        ticket.setStatus(TicketStatus.RESERVED);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        try {
            // 1. Generar contenido del PDF
            byte[] pdfBytes = pdfTicketGeneratorService.generateTicketPdf(ticket);

            // 2. Guardar en almacenamiento temporal EFS (/app/efs)
            String filename = ticketId + ".pdf";
            String efsPath = efsStorageService.saveTicketFile(filename, pdfBytes);
            ticket.setEfsPath(efsPath);

            // 3. Subir de forma automatica a S3 con la ruta por evento y usuario
            String s3Key = s3StorageService.buildS3Key(request.getEventId(), request.getUserId(), ticketId);
            s3StorageService.uploadFile(s3Key, pdfBytes, "application/pdf");
            ticket.setS3Key(s3Key);

            // 4. Guardar en repositorio
            ticketRepository.save(ticket);

            TicketResponseDTO response = toResponseDTO(ticket);
            response.setMessage("Ticket generado con exito en EFS y subido a S3");
            return response;

        } catch (Exception e) {
            log.error("Error al generar ticket: {}", e.getMessage());
            throw new RuntimeException("Error al procesar ticket: " + e.getMessage(), e);
        }
    }

    // Sube o resincroniza un ticket a S3 manualmente
    public TicketResponseDTO uploadTicketToS3(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketId));

        try {
            byte[] fileBytes;
            String filename = ticketId + ".pdf";
            if (efsStorageService.fileExists(filename)) {
                fileBytes = efsStorageService.readTicketFile(filename);
            } else {
                fileBytes = pdfTicketGeneratorService.generateTicketPdf(ticket);
                efsStorageService.saveTicketFile(filename, fileBytes);
            }

            String s3Key = s3StorageService.buildS3Key(ticket.getEventId(), ticket.getUserId(), ticketId);
            s3StorageService.uploadFile(s3Key, fileBytes, "application/pdf");

            ticket.setS3Key(s3Key);
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);

            TicketResponseDTO response = toResponseDTO(ticket);
            response.setMessage("Ticket subido a S3 con exito");
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a S3: " + e.getMessage(), e);
        }
    }

    // Descarga el archivo PDF buscando por ID o por evento y usuario
    public byte[] downloadTicketFile(String ticketId, String eventId, String userId) {
        Ticket ticket = null;
        if (ticketId != null && !ticketId.isBlank()) {
            ticket = ticketRepository.findById(ticketId).orElse(null);
        } else if (eventId != null && userId != null) {
            ticket = ticketRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
        }

        if (ticket == null) {
            throw new IllegalArgumentException("No se encontro ningun ticket con los parametros dados");
        }

        try {
            return s3StorageService.downloadFile(ticket.getS3Key());
        } catch (Exception e) {
            try {
                return efsStorageService.readTicketFile(ticket.getTicketId() + ".pdf");
            } catch (IOException ioException) {
                throw new RuntimeException("Archivo no disponible en S3 ni en EFS");
            }
        }
    }

    // Modifica y actualiza el ticket en S3 y EFS
    public TicketResponseDTO updateTicket(String ticketId, TicketUpdateDTO updateDTO) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketId));

        if (updateDTO.getSeatNumber() != null) ticket.setSeatNumber(updateDTO.getSeatNumber());
        if (updateDTO.getPrice() != null) ticket.setPrice(updateDTO.getPrice());
        if (updateDTO.getStatus() != null) ticket.setStatus(updateDTO.getStatus());
        if (updateDTO.getUserName() != null) ticket.setUserName(updateDTO.getUserName());
        if (updateDTO.getUserEmail() != null) ticket.setUserEmail(updateDTO.getUserEmail());
        ticket.setUpdatedAt(LocalDateTime.now());

        try {
            byte[] updatedPdf = pdfTicketGeneratorService.generateTicketPdf(ticket);

            // Actualizar en EFS
            String filename = ticketId + ".pdf";
            String efsPath = efsStorageService.saveTicketFile(filename, updatedPdf);
            ticket.setEfsPath(efsPath);

            // Actualizar en S3
            s3StorageService.updateFile(ticket.getS3Key(), updatedPdf, "application/pdf");

            ticketRepository.save(ticket);

            TicketResponseDTO response = toResponseDTO(ticket);
            response.setMessage("Ticket modificado y actualizado en S3 y EFS");
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar ticket: " + e.getMessage(), e);
        }
    }

    // Elimina el ticket de S3, EFS y la base de datos
    public void deleteTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketId));

        if (ticket.getS3Key() != null) {
            s3StorageService.deleteFile(ticket.getS3Key());
        }

        efsStorageService.deleteTicketFile(ticketId + ".pdf");
        ticketRepository.deleteById(ticketId);
    }

    // Consulta el historial con filtros
    public List<TicketResponseDTO> getTicketHistory(String eventId, String userId, TicketStatus status) {
        return ticketRepository.findByFilters(eventId, userId, status).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Reporta estadisticas por evento
    public EventStatisticsDTO getEventStatistics(String eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        EventStatisticsDTO stats = new EventStatisticsDTO();
        stats.setEventId(eventId);

        if (tickets.isEmpty()) {
            stats.setEventName("Evento sin registros");
            return stats;
        }

        stats.setEventName(tickets.get(0).getEventName());
        stats.setTotalTickets(tickets.size());

        long reserved = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESERVED).count();
        long confirmed = tickets.stream().filter(t -> t.getStatus() == TicketStatus.CONFIRMED).count();
        long cancelled = tickets.stream().filter(t -> t.getStatus() == TicketStatus.CANCELLED).count();
        double revenue = tickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                .mapToDouble(Ticket::getPrice)
                .sum();

        stats.setTotalReserved(reserved);
        stats.setTotalConfirmed(confirmed);
        stats.setTotalCancelled(cancelled);
        stats.setTotalRevenue(revenue);
        stats.setAverageTicketPrice(tickets.size() > 0 ? revenue / Math.max(1, (reserved + confirmed)) : 0.0);
        stats.setOccupancyPercentage(Math.min(100.0, ((reserved + confirmed) / 100.0) * 100.0));

        return stats;
    }

    private TicketResponseDTO toResponseDTO(Ticket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setEventId(ticket.getEventId());
        dto.setEventName(ticket.getEventName());
        dto.setUserId(ticket.getUserId());
        dto.setUserName(ticket.getUserName());
        dto.setUserEmail(ticket.getUserEmail());
        dto.setSeatNumber(ticket.getSeatNumber());
        dto.setPrice(ticket.getPrice());
        dto.setStatus(ticket.getStatus());
        dto.setEfsPath(ticket.getEfsPath());
        dto.setS3Key(ticket.getS3Key());
        dto.setS3Bucket(s3StorageService.getBucketName());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        return dto;
    }
}
