// File: org/boda/smartqueue/queue_server/DTO/UserDTO.java
package org.boda.smartqueue.queue_server.DTO;

import org.boda.smartqueue.queue_server.model.userDataModel;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String serviceType;
    private String phoneNumber;
    // ✅ CHANGE: Make customerNumber a String to match Flutter client and local server
    private String customerNumber;
    private String ticketStatus;
    private LocalDateTime serviceCompletedAt;
    private LocalDateTime serviceCancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor from userDataModel
    public UserDTO(userDataModel user) {
        this.id = user.getId();
        this.name = user.getName();
        // ✅ VALIDATION: Ensure email is not null
        this.email = user.getEmail() != null ? user.getEmail() : "";
        this.serviceType = user.getServiceType();
        this.phoneNumber = user.getPhoneNumber();
        // ✅ CHANGE: Convert Integer to String when mapping from userDataModel (which might have Integer)
        // This handles the potential type difference between AWS DB (Integer) and desired API response (String)
        this.customerNumber = user.getCustomerNumber() != null ? user.getCustomerNumber().toString() : null;
        this.ticketStatus = user.getTicketStatus();
        this.serviceCompletedAt = user.getServiceCompletedAt();
        this.serviceCancelledAt = user.getServiceCancelledAt();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    // Default constructor (if needed by frameworks)
    public UserDTO() {}

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

    // ✅ CHANGE: Getter and setter for String customerNumber
    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

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