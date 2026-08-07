package com.campussphere.repository;

import com.campussphere.entity.certificate.CertificateIssueLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateIssueLogRepository extends JpaRepository<CertificateIssueLog, Long> {

    @EntityGraph(attributePaths = {"certificate", "certificate.event", "actor"})
    List<CertificateIssueLog> findByCertificate_IdAndDeletedFalseOrderByOccurredAtDesc(Long certificateId);
}
