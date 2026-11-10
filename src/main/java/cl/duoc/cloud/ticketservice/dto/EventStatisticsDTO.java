package cl.duoc.cloud.ticketservice.dto;

public class EventStatisticsDTO {
    private String eventId;
    private String eventName;
    private long totalTickets;
    private long totalReserved;
    private long totalConfirmed;
    private long totalCancelled;
    private double totalRevenue;
    private double averageTicketPrice;
    private double occupancyPercentage;

    public EventStatisticsDTO() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getTotalReserved() {
        return totalReserved;
    }

    public void setTotalReserved(long totalReserved) {
        this.totalReserved = totalReserved;
    }

    public long getTotalConfirmed() {
        return totalConfirmed;
    }

    public void setTotalConfirmed(long totalConfirmed) {
        this.totalConfirmed = totalConfirmed;
    }

    public long getTotalCancelled() {
        return totalCancelled;
    }

    public void setTotalCancelled(long totalCancelled) {
        this.totalCancelled = totalCancelled;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getAverageTicketPrice() {
        return averageTicketPrice;
    }

    public void setAverageTicketPrice(double averageTicketPrice) {
        this.averageTicketPrice = averageTicketPrice;
    }

    public double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }
}
