package com.campussphere.repository;

import com.campussphere.entity.DepartmentProgramme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DepartmentProgrammeRepository extends JpaRepository<DepartmentProgramme, Long>, JpaSpecificationExecutor<DepartmentProgramme> {

    Optional<DepartmentProgramme> findByIdAndDeletedFalse(Long id);

    boolean existsByInstitution_IdAndDepartment_IdAndProgramme_IdAndDeletedFalse(Long institutionId, Long departmentId, Long programmeId);

    boolean existsByDepartment_IdAndProgramme_IdAndDeletedFalse(Long departmentId, Long programmeId);

    boolean existsByDepartment_IdAndProgramme_IdAndIdNotAndDeletedFalse(Long departmentId, Long programmeId, Long id);
}
