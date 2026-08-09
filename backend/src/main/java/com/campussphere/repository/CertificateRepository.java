package com.campussphere.repository;

import com.campussphere.entity.certificate.Certificate;
import com.campussphere.entity.certificate.CertificateStatus;
import com.campussphere.entity.certificate.CertificateType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long>, JpaSpecificationExecutor<Certificate> {

    @EntityGraph(attributePaths = {"institution", "issueBy", "recipientUser", "event", "session", "academicYear", "template", "revokedBy"})
    Optional<Certificate> findByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"institution", "issueBy", "recipientUser", "event", "session", "academicYear", "template", "revokedBy"})
    Optional<Certificate> findByVerificationTokenAndDeletedFalse(String verificationToken);

    @EntityGraph(attributePaths = {"institution", "issueBy", "recipientUser", "event", "session", "academicYear", "template", "revokedBy"})
    List<Certificate> findByInstitution_IdAndDeletedFalse(Long institutionId);

    @EntityGraph(attributePaths = {"institution", "issueBy", "recipientUser", "event", "session", "academicYear", "template", "revokedBy"})
    List<Certificate> findByRecipientUser_IdAndDeletedFalseOrderByGeneratedAtDesc(Long recipientUserId);

    boolean existsByInstitution_IdAndCertificateNumberIgnoreCaseAndDeletedFalse(Long institutionId, String certificateNumber);

    boolean existsByInstitution_IdAndCertificateNumberIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String certificateNumber, Long id);

    boolean existsByInstitution_IdAndEvent_IdAndRecipientUser_IdAndCertificateTypeAndDeletedFalse(Long institutionId, Long eventId, Long recipientUserId, CertificateType certificateType);

    long countByInstitution_IdAndDeletedFalse(Long institutionId);

    long countByInstitution_IdAndCertificateStatusAndDeletedFalse(Long institutionId, CertificateStatus certificateStatus);

    long countByInstitution_IdAndVerificationStatusAndDeletedFalse(Long institutionId, com.campussphere.entity.certificate.CertificateVerificationStatus verificationStatus);
}
