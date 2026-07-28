package com.campussphere.repository;

import com.campussphere.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long>, JpaSpecificationExecutor<Institution> {

    Optional<Institution> findByInstitutionCodeIgnoreCaseAndDeletedFalse(String institutionCode);

    boolean existsByInstitutionCodeIgnoreCaseAndDeletedFalse(String institutionCode);

    boolean existsByInstitutionNameIgnoreCaseAndDeletedFalse(String institutionName);

    boolean existsByInstitutionCodeIgnoreCaseAndIdNotAndDeletedFalse(String institutionCode, Long id);

    boolean existsByInstitutionNameIgnoreCaseAndIdNotAndDeletedFalse(String institutionName, Long id);
}
