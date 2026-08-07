package com.campussphere.repository;

import com.campussphere.entity.certificate.CertificateVerification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateVerificationRepository extends JpaRepository<CertificateVerification, Long> {

    @EntityGraph(attributePaths = {"certificate", "certificate.institution", "certificate.event"})
    Optional<CertificateVerification> findByVerificationTokenAndDeletedFalseOrderByVerifiedAtDesc(String verificationToken);

    List<CertificateVerification> findByCertificate_IdAndDeletedFalseOrderByVerifiedAtDesc(Long certificateId);

    long countByDeletedFalse();
}
