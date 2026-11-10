package cl.duoc.cloud.ticketservice.model;

import java.time.LocalDateTime;

public class Ticket {
    private String ticketId;
    private String eventId;
    private String eventName;
    private String userId;
    private String userName;
    private String userEmail;
    private String seatNumber;
    private Double price;
    private TicketStatus status;
    private String efsPath;
    private String s3Key;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Ticket() {
    }

    public Ticket(String ticketId, String eventId, String eventName, String userId, 
                  String userName, String userEmail, String seatNumber, Double price, 
                  TicketStatus status, String efsPath, String s3Key, 
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
        this.efsPath = efsPath;
        this.s3Key = s3Key;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getEfsPath() {
        return efsPath;
    }

    public void setEfsPath(String efsPath) {
        this.efsPath = efsPath;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
