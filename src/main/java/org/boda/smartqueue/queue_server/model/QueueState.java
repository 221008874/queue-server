package org.boda.smartqueue.queue_server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_state") // Table to store current queue state per service
public class QueueState {

    @Id
    @Column(name = "service_type", unique = true, nullable = false) // Primary key is the service type
    private String serviceType;

    @Column(name = "current_ticket_number", nullable = true) // The ticket currently being served
    private String currentTicketNumber; // e.g., "L005"

    @Column(name = "next_ticket_number", nullable = true) // The next ticket to be called
    private String nextTicketNumber; // e.g., "L006"

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    // Constructors
    public QueueState() {}

    public QueueState(String serviceType) {
        this.serviceType = serviceType;
    }

    // Getters and Setters
    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getCurrentTicketNumber() {
        return currentTicketNumber;
    }

    public void setCurrentTicketNumber(String currentTicketNumber) {
        this.currentTicketNumber = currentTicketNumber;
        this.lastUpdatedAt = LocalDateTime.now(); // Update timestamp when state changes
    }

    public String getNextTicketNumber() {
        return nextTicketNumber;
    }

    public void setNextTicketNumber(String nextTicketNumber) {
        this.nextTicketNumber = nextTicketNumber;
        this.lastUpdatedAt = LocalDateTime.now(); // Update timestamp when state changes
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}