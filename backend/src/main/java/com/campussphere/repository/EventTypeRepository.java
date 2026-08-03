package com.campussphere.repository;

import com.campussphere.entity.event.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Long>, JpaSpecificationExecutor<EventType> {

    Optional<EventType> findByIdAndDeletedFalse(Long id);

    boolean existsByInstitution_IdAndTypeCodeIgnoreCaseAndDeletedFalse(Long institutionId, String typeCode);

    boolean existsByInstitution_IdAndTypeNameIgnoreCaseAndDeletedFalse(Long institutionId, String typeName);

    boolean existsByInstitution_IdAndTypeCodeIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String typeCode, Long id);

    boolean existsByInstitution_IdAndTypeNameIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String typeName, Long id);
}
