package com.campussphere.entity.certificate;

import com.campussphere.entity.AcademicYear;
import com.campussphere.entity.BaseEntity;
import com.campussphere.entity.Institution;
import com.campussphere.entity.User;
import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_certificates_institution_number", columnNames = {"institution_id", "certificate_number"}),
                @UniqueConstraint(name = "uk_certificates_uuid", columnNames = "certificate_uuid"),
                @UniqueConstraint(name = "uk_certificates_verification_token", columnNames = "verification_token")
        }
)
public class Certificate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "certificate_number", nullable = false, length = 60)
    private String certificateNumber;

    @Column(name = "certificate_uuid", nullable = false, length = 64)
    private String certificateUuid = UUID.randomUUID().toString();

    @Column(name = "verification_token", nullable = false, length = 128)
    private String verificationToken;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_by_user_id")
    private User issueBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @Column(name = "recipient_name", nullable = false, length = 180)
    private String recipientName;

    @Column(name = "recipient_role", nullable = false, length = 80)
    private String recipientRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_session_id")
    private EventSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private CertificateTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false, length = 40)
    private CertificateType certificateType = CertificateType.PARTICIPATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_status", nullable = false, length = 24)
    private CertificateStatus certificateStatus = CertificateStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 24)
    private CertificateVerificationStatus verificationStatus = CertificateVerificationStatus.NOT_VERIFIED;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by_user_id")
    private User revokedBy;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    @Column(length = 60)
    private String position;

    @Column(length = 120)
    private String prize;

    @Column(name = "attendance_percentage")
    private Integer attendancePercentage;

    @Column(name = "verification_url", length = 512)
    private String verificationUrl;

    @Column(name = "pdf_file_name", length = 180)
    private String pdfFileName;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getCertificateUuid() {
        return certificateUuid;
    }

    public void setCertificateUuid(String certificateUuid) {
        this.certificateUuid = certificateUuid;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public User getIssueBy() {
        return issueBy;
    }

    public void setIssueBy(User issueBy) {
        this.issueBy = issueBy;
    }

    public User getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(User recipientUser) {
        this.recipientUser = recipientUser;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public EventSession getSession() {
        return session;
    }

    public void setSession(EventSession session) {
        this.session = session;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public CertificateTemplate getTemplate() {
        return template;
    }

    public void setTemplate(CertificateTemplate template) {
        this.template = template;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }

    public CertificateStatus getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(CertificateStatus certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    public CertificateVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(CertificateVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public User getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(User revokedBy) {
        this.revokedBy = revokedBy;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public Integer getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Integer attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }
}
