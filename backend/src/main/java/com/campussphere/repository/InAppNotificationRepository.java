package com.campussphere.repository;

import com.campussphere.entity.registration.InAppNotification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {

    @EntityGraph(attributePaths = {"recipient"})
    Optional<InAppNotification> findByIdAndDeletedFalse(Long id);

    List<InAppNotification> findByRecipient_IdAndDeletedFalseOrderByCreatedAtDesc(Long recipientId);

    long countByRecipient_IdAndReadAtIsNullAndDeletedFalse(Long recipientId);
}
