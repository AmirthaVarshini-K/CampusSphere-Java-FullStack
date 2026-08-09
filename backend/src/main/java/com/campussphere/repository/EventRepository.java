package com.campussphere.repository;

import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @EntityGraph(attributePaths = {"institution", "eventCategory", "eventType", "organizingDepartment", "academicYear", "venue"})
    Optional<Event> findByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"institution", "eventCategory", "eventType", "organizingDepartment", "academicYear", "venue"})
    List<Event> findByInstitution_IdAndDeletedFalse(Long institutionId);

    boolean existsByInstitution_IdAndEventCodeIgnoreCaseAndDeletedFalse(Long institutionId, String eventCode);

    boolean existsByInstitution_IdAndSlugIgnoreCaseAndDeletedFalse(Long institutionId, String slug);

    boolean existsByInstitution_IdAndEventCodeIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String eventCode, Long id);

    boolean existsByInstitution_IdAndSlugIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String slug, Long id);

    long countByInstitution_IdAndEventStatusAndDeletedFalse(Long institutionId, EventStatus eventStatus);

    long countByInstitution_IdAndEventStatusInAndDeletedFalse(Long institutionId, List<EventStatus> eventStatuses);

    long countByInstitution_IdAndDeletedFalse(Long institutionId);
}
