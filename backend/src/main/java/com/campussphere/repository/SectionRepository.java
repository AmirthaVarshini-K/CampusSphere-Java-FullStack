package com.campussphere.repository;

import com.campussphere.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long>, JpaSpecificationExecutor<Section> {

    Optional<Section> findByIdAndDeletedFalse(Long id);

    List<Section> findByDepartment_IdAndDeletedFalseOrderByStudyYearAscSectionNameAsc(Long departmentId);

    boolean existsByInstitution_IdAndDepartment_IdAndProgramme_IdAndAcademicYear_IdAndSemester_IdAndSectionNameIgnoreCaseAndDeletedFalse(
            Long institutionId,
            Long departmentId,
            Long programmeId,
            Long academicYearId,
            Long semesterId,
            String sectionName
    );

    boolean existsByInstitution_IdAndDepartment_IdAndProgramme_IdAndAcademicYear_IdAndSemester_IdAndSectionNameIgnoreCaseAndIdNotAndDeletedFalse(
            Long institutionId,
            Long departmentId,
            Long programmeId,
            Long academicYearId,
            Long semesterId,
            String sectionName,
            Long id
    );
}
