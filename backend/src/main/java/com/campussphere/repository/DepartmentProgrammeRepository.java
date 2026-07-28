package com.campussphere.repository;

import com.campussphere.entity.DepartmentProgramme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DepartmentProgrammeRepository extends JpaRepository<DepartmentProgramme, Long>, JpaSpecificationExecutor<DepartmentProgramme> {

    Optional<DepartmentProgramme> findByIdAndDeletedFalse(Long id);

    boolean existsByDepartment_IdAndProgramme_IdAndDeletedFalse(Long departmentId, Long programmeId);
}
