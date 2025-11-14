package org.boda.smartqueue.queue_server.DTO;

import org.boda.smartqueue.queue_server.model.userDataModel;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String serviceType;
    private String phoneNumber;
    private Integer customerNumber;
    private String ticketStatus;
    private LocalDateTime serviceCompletedAt;
    private LocalDateTime serviceCancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserDTO(userDataModel user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.serviceType = user.getServiceType();
        this.phoneNumber = user.getPhoneNumber();
        this.customerNumber = user.getCustomerNumber();
        this.ticketStatus = user.getTicketStatus();
        this.serviceCompletedAt = user.getServiceCompletedAt();
        this.serviceCancelledAt = user.getServiceCancelledAt();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Integer getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(Integer customerNumber) { this.customerNumber = customerNumber; }
    public String getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }
    public LocalDateTime getServiceCompletedAt() { return serviceCompletedAt; }
    public void setServiceCompletedAt(LocalDateTime serviceCompletedAt) { this.serviceCompletedAt = serviceCompletedAt; }
    public LocalDateTime getServiceCancelledAt() { return serviceCancelledAt; }
    public void setServiceCancelledAt(LocalDateTime serviceCancelledAt) { this.serviceCancelledAt = serviceCancelledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

