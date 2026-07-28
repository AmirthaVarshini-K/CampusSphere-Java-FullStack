package com.campussphere.repository;

import com.campussphere.entity.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProgrammeRepository extends JpaRepository<Programme, Long>, JpaSpecificationExecutor<Programme> {

    Optional<Programme> findByIdAndDeletedFalse(Long id);

    boolean existsByInstitution_IdAndProgrammeCodeIgnoreCaseAndDeletedFalse(Long institutionId, String programmeCode);

    boolean existsByInstitution_IdAndProgrammeNameIgnoreCaseAndDeletedFalse(Long institutionId, String programmeName);
}
