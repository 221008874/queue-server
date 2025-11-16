// File: org/boda/smartqueue/queue_server/Repo/UserRepository.java
package org.boda.smartqueue.queue_server.Repo;

import org.boda.smartqueue.queue_server.model.userDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<userDataModel, Long> {
    Optional<userDataModel> findByResetPasswordToken(String token);
    Optional<userDataModel> findByEmail(String email);
    Optional<userDataModel> findByEmailAndServiceType(String email, String serviceType);
    List<userDataModel> findByServiceType(String serviceType);

    // ✅ CHANGE: Method signature for String customerNumber
    Optional<userDataModel> findByCustomerNumber(String customerNumber);

    // ✅ CHANGE: Method signature for String customerNumber
    boolean existsByCustomerNumber(String customerNumber);

    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    // ✅ SEARCH METHOD - Updated to handle String customerNumber in query
    @Query("SELECT u FROM userDataModel u WHERE " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.customerNumber) LIKE LOWER(CONCAT('%', :query, '%'))") // Changed CAST(u.customerNumber AS string) to LOWER(u.customerNumber)
    List<userDataModel> searchUsers(@Param("query") String query);

    // ✅ ACTIVE CUSTOMERS BY TICKET STATUS
    @Query("SELECT u FROM userDataModel u WHERE u.ticketStatus = :ticketStatus")
    List<userDataModel> findByTicketStatus(@Param("ticketStatus") String ticketStatus);

    // ✅ ACTIVE CUSTOMERS BY SERVICE TYPE AND TICKET STATUS
    @Query("SELECT u FROM userDataModel u WHERE u.serviceType = :serviceType AND u.ticketStatus = :ticketStatus")
    List<userDataModel> findByServiceTypeAndTicketStatus(
            @Param("serviceType") String serviceType,
            @Param("ticketStatus") String ticketStatus);
}