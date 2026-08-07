package com.campussphere.repository;

import com.campussphere.entity.registration.EventRegistration;
import com.campussphere.entity.registration.RegistrationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    @EntityGraph(attributePaths = {"institution", "event", "participant", "approvedBy", "team"})
    Optional<EventRegistration> findByIdAndDeletedFalse(Long id);

    List<EventRegistration> findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(Long participantId);

    List<EventRegistration> findByEvent_IdAndDeletedFalseOrderByRegistrationDateDesc(Long eventId);

    List<EventRegistration> findByEvent_IdAndParticipant_IdAndDeletedFalse(Long eventId, Long participantId);

    List<EventRegistration> findByEvent_IdAndRegistrationStatusAndDeletedFalseOrderByWaitlistPositionAsc(Long eventId, RegistrationStatus registrationStatus);

    boolean existsByEvent_IdAndParticipant_IdAndDeletedFalse(Long eventId, Long participantId);

    long countByEvent_IdAndRegistrationStatusAndDeletedFalse(Long eventId, RegistrationStatus registrationStatus);

    long countByEvent_Institution_IdAndDeletedFalse(Long institutionId);
}
