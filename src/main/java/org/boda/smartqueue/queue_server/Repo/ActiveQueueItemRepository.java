// File: org/boda/smartqueue/queue_server/Repo/ActiveQueueItemRepository.java
package org.boda.smartqueue.queue_server.Repo;

import org.boda.smartqueue.queue_server.model.ActiveQueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ActiveQueueItemRepository extends JpaRepository<ActiveQueueItem, Long> {

    // Find all active items for a specific service, ordered by customer number (or assigned time)
    List<ActiveQueueItem> findByServiceTypeOrderByCustomerNumberAsc(String serviceType);

    // Find all active items for a specific user (useful for checking if user is in any queue)
    List<ActiveQueueItem> findByUserEmail(String userEmail);

    // Find a specific active item by user email and service type
    ActiveQueueItem findByUserEmailAndServiceType(String userEmail, String serviceType);

    // Find a specific active item by ticket number and service type
    ActiveQueueItem findByCustomerNumberAndServiceType(String customerNumber, String serviceType);

    // Delete an item by user email and service type (for cancelling/completing)
    @Modifying
    @Transactional
    @Query("DELETE FROM ActiveQueueItem a WHERE a.userEmail = :userEmail AND a.serviceType = :serviceType")
    void deleteByUserEmailAndServiceType(@Param("userEmail") String userEmail, @Param("serviceType") String serviceType);

    // Delete an item by ticket number and service type (alternative)
    @Modifying
    @Transactional
    @Query("DELETE FROM ActiveQueueItem a WHERE a.customerNumber = :customerNumber AND a.serviceType = :serviceType")
    void deleteByCustomerNumberAndServiceType(@Param("customerNumber") String customerNumber, @Param("serviceType") String serviceType);
}