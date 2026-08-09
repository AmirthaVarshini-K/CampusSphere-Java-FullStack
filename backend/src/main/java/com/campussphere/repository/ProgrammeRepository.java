package com.campussphere.repository;

import com.campussphere.entity.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProgrammeRepository extends JpaRepository<Programme, Long>, JpaSpecificationExecutor<Programme> {

    Optional<Programme> findByIdAndDeletedFalse(Long id);

    List<Programme> findByInstitution_IdAndDeletedFalse(Long institutionId);

    boolean existsByInstitution_IdAndProgrammeCodeIgnoreCaseAndDeletedFalse(Long institutionId, String programmeCode);

    boolean existsByInstitution_IdAndProgrammeNameIgnoreCaseAndDeletedFalse(Long institutionId, String programmeName);

    boolean existsByInstitution_IdAndProgrammeCodeIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String programmeCode, Long id);

    boolean existsByInstitution_IdAndProgrammeNameIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String programmeName, Long id);
}
