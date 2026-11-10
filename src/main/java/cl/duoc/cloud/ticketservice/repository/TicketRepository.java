package cl.duoc.cloud.ticketservice.repository;

import cl.duoc.cloud.ticketservice.model.Ticket;
import cl.duoc.cloud.ticketservice.model.TicketStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TicketRepository {

    private final Map<String, Ticket> storage = new ConcurrentHashMap<>();

    public Ticket save(Ticket ticket) {
        storage.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    public Optional<Ticket> findById(String ticketId) {
        return Optional.ofNullable(storage.get(ticketId));
    }

    public Optional<Ticket> findByEventIdAndUserId(String eventId, String userId) {
        return storage.values().stream()
                .filter(t -> t.getEventId().equalsIgnoreCase(eventId) && t.getUserId().equalsIgnoreCase(userId))
                .findFirst();
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<Ticket> findByFilters(String eventId, String userId, TicketStatus status) {
        return storage.values().stream()
                .filter(t -> eventId == null || t.getEventId().equalsIgnoreCase(eventId))
                .filter(t -> userId == null || t.getUserId().equalsIgnoreCase(userId))
                .filter(t -> status == null || t.getStatus() == status)
                .sorted(Comparator.comparing(Ticket::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Ticket> findByEventId(String eventId) {
        return storage.values().stream()
                .filter(t -> t.getEventId().equalsIgnoreCase(eventId))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String ticketId) {
        return storage.remove(ticketId) != null;
    }

    public boolean existsById(String ticketId) {
        return storage.containsKey(ticketId);
    }
}
