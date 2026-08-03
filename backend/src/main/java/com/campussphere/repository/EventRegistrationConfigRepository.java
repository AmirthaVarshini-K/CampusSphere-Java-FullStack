package com.campussphere.repository;

import com.campussphere.entity.event.EventRegistrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRegistrationConfigRepository extends JpaRepository<EventRegistrationConfig, Long> {

    Optional<EventRegistrationConfig> findByEvent_IdAndDeletedFalse(Long eventId);
}
