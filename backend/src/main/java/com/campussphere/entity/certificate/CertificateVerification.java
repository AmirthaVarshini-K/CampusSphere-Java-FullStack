package com.campussphere.entity.certificate;

import com.campussphere.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "certificate_verifications")
public class CertificateVerification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    @Column(name = "verification_token", nullable = false, length = 128)
    private String verificationToken;

    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt;

    @Column(name = "verified_ip", length = 80)
    private String verifiedIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 24)
    private CertificateVerificationStatus verificationStatus = CertificateVerificationStatus.NOT_VERIFIED;

    @Column(length = 500)
    private String message;

    @Column(name = "certificate_number", length = 60)
    private String certificateNumber;

    @Column(name = "recipient_name", length = 180)
    private String recipientName;

    @Column(name = "institution_name", length = 180)
    private String institutionName;

    @Column(name = "event_title", length = 180)
    private String eventTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", length = 40)
    private CertificateType certificateType;

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerifiedIp() {
        return verifiedIp;
    }

    public void setVerifiedIp(String verifiedIp) {
        this.verifiedIp = verifiedIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public CertificateVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(CertificateVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }
}
