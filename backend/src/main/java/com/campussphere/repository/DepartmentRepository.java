package com.campussphere.repository;

import com.campussphere.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    Optional<Department> findByIdAndDeletedFalse(Long id);

    List<Department> findByInstitution_IdAndDeletedFalse(Long institutionId);

    boolean existsByInstitution_IdAndDepartmentCodeIgnoreCaseAndDeletedFalse(Long institutionId, String departmentCode);

    boolean existsByInstitution_IdAndDepartmentNameIgnoreCaseAndDeletedFalse(Long institutionId, String departmentName);

    boolean existsByInstitution_IdAndDepartmentCodeIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String departmentCode, Long id);

    boolean existsByInstitution_IdAndDepartmentNameIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String departmentName, Long id);
}
