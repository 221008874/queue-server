// File: org/boda/smartqueue/queue_server/model/ActiveQueueItem.java
package org.boda.smartqueue.queue_server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "active_queue_items") // Table for active tickets
public class ActiveQueueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique ID for the queue item record

    @Column(name = "user_email", nullable = false) // Link back to the user
    private String userEmail;

    @Column(name = "service_type", nullable = false) // Service type (Loans, Banking etc.)
    private String serviceType;

    @Column(name = "customer_number", nullable = false) // The ticket number (e.g., L015)
    private String customerNumber;

    @Column(name = "assigned_at", nullable = false) // When the ticket was assigned
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "last_updated_at", nullable = false) // When the record was last modified
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    // Constructors
    public ActiveQueueItem() {}

    public ActiveQueueItem(String userEmail, String serviceType, String customerNumber) {
        this.userEmail = userEmail;
        this.serviceType = serviceType;
        this.customerNumber = customerNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}