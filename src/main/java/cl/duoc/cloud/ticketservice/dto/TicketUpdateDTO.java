package cl.duoc.cloud.ticketservice.dto;

import cl.duoc.cloud.ticketservice.model.TicketStatus;

public class TicketUpdateDTO {
    private String userName;
    private String userEmail;
    private String seatNumber;
    private Double price;
    private TicketStatus status;

    public TicketUpdateDTO() {
    }

    public TicketUpdateDTO(String userName, String userEmail, String seatNumber, Double price, TicketStatus status) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
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
}
