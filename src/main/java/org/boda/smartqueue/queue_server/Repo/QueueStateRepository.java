package org.boda.smartqueue.queue_server.Repo;

import org.boda.smartqueue.queue_server.model.QueueState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueueStateRepository extends JpaRepository<QueueState, String> { // String because serviceType is the ID

    Optional<QueueState> findByServiceType(String serviceType);
}