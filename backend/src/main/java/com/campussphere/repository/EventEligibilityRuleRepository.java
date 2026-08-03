package com.campussphere.repository;

import com.campussphere.entity.event.EventEligibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventEligibilityRuleRepository extends JpaRepository<EventEligibilityRule, Long> {

    List<EventEligibilityRule> findByEvent_IdAndDeletedFalse(Long eventId);

    Optional<EventEligibilityRule> findByIdAndDeletedFalse(Long id);
}
