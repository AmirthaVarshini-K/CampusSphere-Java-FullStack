package com.campussphere.repository;

import com.campussphere.entity.event.EventCoordinator;
import com.campussphere.entity.event.CoordinatorRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventCoordinatorRepository extends JpaRepository<EventCoordinator, Long> {

    List<EventCoordinator> findByEvent_IdAndDeletedFalse(Long eventId);

    List<EventCoordinator> findByEvent_Institution_IdAndDeletedFalse(Long institutionId);

    List<EventCoordinator> findByUser_IdAndDeletedFalse(Long userId);

    Optional<EventCoordinator> findByIdAndDeletedFalse(Long id);

    boolean existsByEvent_IdAndUser_IdAndDeletedFalse(Long eventId, Long userId);

    boolean existsByEvent_IdAndUser_IdAndCoordinatorRoleAndDeletedFalse(Long eventId, Long userId, CoordinatorRole coordinatorRole);
}
