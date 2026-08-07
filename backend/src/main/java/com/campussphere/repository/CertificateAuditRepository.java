package com.campussphere.repository;

import com.campussphere.entity.certificate.CertificateAudit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateAuditRepository extends JpaRepository<CertificateAudit, Long> {

    @EntityGraph(attributePaths = {"certificate", "actor"})
    List<CertificateAudit> findByCertificate_IdAndDeletedFalseOrderByOccurredAtDesc(Long certificateId);
}
