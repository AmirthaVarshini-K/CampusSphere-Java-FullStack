package com.campussphere.repository;

import com.campussphere.entity.certificate.CertificateTemplate;
import com.campussphere.entity.certificate.CertificateType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long>, JpaSpecificationExecutor<CertificateTemplate> {

    @EntityGraph(attributePaths = {"institution"})
    Optional<CertificateTemplate> findByIdAndDeletedFalse(Long id);

    List<CertificateTemplate> findByInstitution_IdAndDeletedFalseOrderByTemplateNameAsc(Long institutionId);

    List<CertificateTemplate> findByInstitution_IdAndCertificateTypeAndDeletedFalseOrderByTemplateNameAsc(Long institutionId, CertificateType certificateType);

    boolean existsByInstitution_IdAndTemplateCodeIgnoreCaseAndDeletedFalse(Long institutionId, String templateCode);

    boolean existsByInstitution_IdAndTemplateCodeIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String templateCode, Long id);
}
