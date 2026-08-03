package com.campussphere.repository;

import com.campussphere.entity.event.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventSessionRepository extends JpaRepository<EventSession, Long> {

    List<EventSession> findByEvent_IdAndDeletedFalseOrderBySequenceNumberAsc(Long eventId);

    Optional<EventSession> findByIdAndDeletedFalse(Long id);

    boolean existsByEvent_IdAndSequenceNumberAndDeletedFalse(Long eventId, int sequenceNumber);
}
