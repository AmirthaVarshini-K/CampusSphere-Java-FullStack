package com.campussphere.repository;

import com.campussphere.entity.event.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Long>, JpaSpecificationExecutor<EventCategory> {

    Optional<EventCategory> findByIdAndDeletedFalse(Long id);

    boolean existsByInstitution_IdAndCategoryCodeIgnoreCaseAndDeletedFalse(Long institutionId, String categoryCode);

    boolean existsByInstitution_IdAndCategoryNameIgnoreCaseAndDeletedFalse(Long institutionId, String categoryName);

    boolean existsByInstitution_IdAndCategoryCodeIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String categoryCode, Long id);

    boolean existsByInstitution_IdAndCategoryNameIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String categoryName, Long id);
}
