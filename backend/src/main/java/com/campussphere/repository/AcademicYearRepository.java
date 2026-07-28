package com.campussphere.repository;

import com.campussphere.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long>, JpaSpecificationExecutor<AcademicYear> {

    Optional<AcademicYear> findByIdAndDeletedFalse(Long id);

    Optional<AcademicYear> findByInstitution_IdAndCurrentYearTrueAndDeletedFalse(Long institutionId);

    List<AcademicYear> findByInstitution_IdAndDeletedFalseOrderByStartDateDesc(Long institutionId);
}
