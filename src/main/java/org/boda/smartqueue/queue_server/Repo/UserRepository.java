package org.boda.smartqueue.queue_server.Repo;


import org.boda.smartqueue.queue_server.model.userDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<userDataModel, Long> {
    Optional<userDataModel> findByEmail(String email);
    Optional<userDataModel> findByPhoneNumber(String phoneNumber);
    Optional<userDataModel> findByCustomerNumber(Integer customerNumber);
    Optional<userDataModel> findByResetPasswordToken(String token);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    List<userDataModel> findByTicketStatus(String status);
    List<userDataModel> findByServiceType(String serviceType);
}