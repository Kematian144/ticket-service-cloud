package cl.duoc.cloud.ticketservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TicketRequestDTO {

    @NotBlank(message = "El eventId es obligatorio")
    private String eventId;

    @NotBlank(message = "El eventName es obligatorio")
    private String eventName;

    @NotBlank(message = "El userId es obligatorio")
    private String userId;

    @NotBlank(message = "El userName es obligatorio")
    private String userName;

    private String userEmail;

    @NotBlank(message = "El seatNumber es obligatorio")
    private String seatNumber;

    @NotNull(message = "El price es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double price;

    public TicketRequestDTO() {
    }

    public TicketRequestDTO(String eventId, String eventName, String userId, 
                            String userName, String userEmail, String seatNumber, Double price) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.seatNumber = seatNumber;
        this.price = price;
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
}
