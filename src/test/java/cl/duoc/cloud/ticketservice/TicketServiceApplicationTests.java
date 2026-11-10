package cl.duoc.cloud.ticketservice;

import cl.duoc.cloud.ticketservice.dto.EventStatisticsDTO;
import cl.duoc.cloud.ticketservice.dto.TicketRequestDTO;
import cl.duoc.cloud.ticketservice.dto.TicketResponseDTO;
import cl.duoc.cloud.ticketservice.dto.TicketUpdateDTO;
import cl.duoc.cloud.ticketservice.model.TicketStatus;
import cl.duoc.cloud.ticketservice.service.TicketService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class TicketServiceApplicationTests {

    @Autowired
    private TicketService ticketService;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(ticketService);
    }

    @Test
    void testFullTicketLifecycle() {
        // 1. Generar ticket
        TicketRequestDTO request = new TicketRequestDTO();
        request.setEventId("evento123");
        request.setEventName("Concierto Cloud Native");
        request.setUserId("usuarioX");
        request.setUserName("Juan Perez");
        request.setUserEmail("juan.perez@alumnos.duoc.cl");
        request.setSeatNumber("A-42");
        request.setPrice(45000.0);

        TicketResponseDTO generated = ticketService.generateTicket(request);
        Assertions.assertNotNull(generated);
        Assertions.assertNotNull(generated.getTicketId());
        Assertions.assertEquals("evento123", generated.getEventId());
        Assertions.assertEquals("usuarioX", generated.getUserId());
        Assertions.assertTrue(generated.getS3Key().contains("evento123/usuarioX"));

        // 2. Descargar ticket
        byte[] pdf = ticketService.downloadTicketFile(generated.getTicketId(), null, null);
        Assertions.assertNotNull(pdf);
        Assertions.assertTrue(pdf.length > 0);

        // 3. Modificar ticket
        TicketUpdateDTO updateDTO = new TicketUpdateDTO();
        updateDTO.setSeatNumber("VIP-01");
        updateDTO.setPrice(65000.0);
        updateDTO.setStatus(TicketStatus.CONFIRMED);

        TicketResponseDTO updated = ticketService.updateTicket(generated.getTicketId(), updateDTO);
        Assertions.assertEquals("VIP-01", updated.getSeatNumber());
        Assertions.assertEquals(65000.0, updated.getPrice());
        Assertions.assertEquals(TicketStatus.CONFIRMED, updated.getStatus());

        // 4. Historial con filtros
        List<TicketResponseDTO> history = ticketService.getTicketHistory("evento123", null, null);
        Assertions.assertFalse(history.isEmpty());

        // 5. Estadisticas
        EventStatisticsDTO stats = ticketService.getEventStatistics("evento123");
        Assertions.assertNotNull(stats);
        Assertions.assertEquals("evento123", stats.getEventId());
        Assertions.assertTrue(stats.getTotalTickets() >= 1);
        Assertions.assertEquals(1, stats.getTotalConfirmed());

        // 6. Eliminar ticket
        ticketService.deleteTicket(generated.getTicketId());
        List<TicketResponseDTO> historyAfterDelete = ticketService.getTicketHistory(null, null, null);
        boolean stillPresent = historyAfterDelete.stream().anyMatch(t -> t.getTicketId().equals(generated.getTicketId()));
        Assertions.assertFalse(stillPresent);
    }
}
