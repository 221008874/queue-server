// File: org/boda/smartqueue/queue_server/model/userDataModel.java
package org.boda.smartqueue.queue_server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class userDataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private LocalDateTime serviceCompletedAt;

    @Column(nullable = true)
    private LocalDateTime serviceCancelledAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String serviceType;

    // ✅ phoneNumber is required at registration
    @NotBlank(message = "Phone number is required")
    @Column(unique = true, nullable = false)
    private String phoneNumber;

    // ✅ CRITICAL CHANGE: Make customerNumber a String to match local server and client expectation
    @Column(unique = true, nullable = true) // Can be null initially
    private String customerNumber; // Changed from Integer to String

    private String resetPasswordToken; // Token for reset
    private LocalDateTime resetPasswordExpiresAt; // Token expiry

    // Constructors
    public userDataModel() {}

    public userDataModel(String name, String email, String password, String serviceType, String phoneNumber, String customerNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.serviceType = serviceType;
        this.phoneNumber = phoneNumber;
        this.customerNumber = customerNumber; // Now accepts String
    }

    // Getters and Setters (updated for String customerNumber)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    // ✅ CRITICAL CHANGE: Getter and setter for String customerNumber
    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public LocalDateTime getResetPasswordExpiresAt() { return resetPasswordExpiresAt; }
    public void setResetPasswordExpiresAt(LocalDateTime resetPasswordExpiresAt) { this.resetPasswordExpiresAt = resetPasswordExpiresAt; }

    public LocalDateTime getServiceCompletedAt() { return serviceCompletedAt; }
    public void setServiceCompletedAt(LocalDateTime serviceCompletedAt) { this.serviceCompletedAt = serviceCompletedAt; }

    public LocalDateTime getServiceCancelledAt() { return serviceCancelledAt; }
    public void setServiceCancelledAt(LocalDateTime serviceCancelledAt) { this.serviceCancelledAt = serviceCancelledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", customerNumber='" + customerNumber + '\'' + // Updated in toString
                '}';
    }

    @Column(nullable = false)
    private String ticketStatus = "INACTIVE"; // INACTIVE, ACTIVE, CANCELLED

    public String getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }
}