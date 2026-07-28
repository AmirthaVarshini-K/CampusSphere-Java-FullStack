package com.campussphere.repository;

import com.campussphere.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long>, JpaSpecificationExecutor<Semester> {

    Optional<Semester> findByIdAndDeletedFalse(Long id);

    List<Semester> findByProgramme_IdAndDeletedFalseOrderBySemesterNumberAsc(Long programmeId);

    boolean existsByProgramme_IdAndSemesterNumberAndDeletedFalse(Long programmeId, int semesterNumber);

    boolean existsByProgramme_IdAndSemesterNumberAndIdNotAndDeletedFalse(Long programmeId, int semesterNumber, Long id);
}
