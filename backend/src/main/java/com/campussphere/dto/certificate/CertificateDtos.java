package com.campussphere.dto.certificate;

import com.campussphere.entity.certificate.CertificateStatus;
import com.campussphere.entity.certificate.CertificateTemplateOrientation;
import com.campussphere.entity.certificate.CertificateType;
import com.campussphere.entity.certificate.CertificateVerificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public final class CertificateDtos {
    private CertificateDtos() {
    }

    public record CertificateDashboardResponse(
            long certificatesGenerated,
            long pending,
            long eligibleRecipients,
            long revoked,
            long verificationCount,
            long downloads,
            List<CertificateSummaryResponse> recentCertificates,
            List<CertificateTemplateSummaryResponse> activeTemplates
    ) {
    }

    public record CertificateTemplateRequest(
            @NotBlank @Size(max = 40) String templateCode,
            @NotBlank @Size(max = 160) String templateName,
            @NotNull CertificateType certificateType,
            @NotNull CertificateTemplateOrientation orientation,
            @Size(max = 500) String description,
            @Size(max = 512) String institutionLogoUrl,
            @Size(max = 512) String organizerLogoUrl,
            @Size(max = 512) String backgroundImageUrl,
            @Size(max = 512) String signatureLeftUrl,
            @Size(max = 512) String signatureRightUrl,
            @Size(max = 512) String sealUrl,
            @Size(max = 16) String primaryColor,
            @Size(max = 16) String accentColor,
            @Size(max = 120) String watermarkText,
            Integer marginTopMm,
            Integer marginRightMm,
            Integer marginBottomMm,
            Integer marginLeftMm,
            Boolean qrCodeEnabled,
            @Size(max = 512) String verificationUrlBase,
            @Size(max = 8000) String templateHtml,
            @Size(max = 8000) String variablesJson,
            Boolean active
    ) {
    }

    public record CertificateTemplateSummaryResponse(
            Long id,
            Long institutionId,
            String institutionName,
            String templateCode,
            String templateName,
            CertificateType certificateType,
            CertificateTemplateOrientation orientation,
            boolean qrCodeEnabled,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CertificateTemplateResponse(
            Long id,
            Long institutionId,
            String institutionName,
            String templateCode,
            String templateName,
            CertificateType certificateType,
            CertificateTemplateOrientation orientation,
            String description,
            String institutionLogoUrl,
            String organizerLogoUrl,
            String backgroundImageUrl,
            String signatureLeftUrl,
            String signatureRightUrl,
            String sealUrl,
            String primaryColor,
            String accentColor,
            String watermarkText,
            Integer marginTopMm,
            Integer marginRightMm,
            Integer marginBottomMm,
            Integer marginLeftMm,
            boolean qrCodeEnabled,
            String verificationUrlBase,
            String templateHtml,
            String variablesJson,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CertificateTemplatePreviewResponse(
            CertificateTemplateResponse template,
            List<String> supportedVariables,
            String sampleRender
    ) {
    }

    public record CertificateIssueRequest(
            @NotNull Long eventId,
            Long sessionId,
            Long academicYearId,
            @NotNull Long recipientUserId,
            @NotNull CertificateType certificateType,
            Long templateId,
            @Size(max = 80) String recipientRole,
            @Size(max = 60) String position,
            @Size(max = 120) String prize,
            Integer attendancePercentage,
            LocalDateTime issueDate,
            Boolean adminOverride,
            @Size(max = 500) String remarks
    ) {
    }

    public record CertificateBulkIssueRequest(
            @NotNull Long eventId,
            Long sessionId,
            Long academicYearId,
            @NotNull CertificateType certificateType,
            Long templateId,
            @NotEmpty List<@NotNull Long> recipientUserIds,
            @Size(max = 80) String recipientRole,
            @Size(max = 60) String position,
            @Size(max = 120) String prize,
            Integer attendancePercentage,
            LocalDateTime issueDate,
            Boolean adminOverride,
            @Size(max = 500) String remarks
    ) {
    }

    public record CertificateRevokeRequest(
            @NotBlank @Size(max = 500) String reason
    ) {
    }

    public record CertificateSummaryResponse(
            Long id,
            Long institutionId,
            String institutionName,
            Long eventId,
            String eventTitle,
            Long sessionId,
            String sessionTitle,
            Long recipientUserId,
            String recipientName,
            String recipientRole,
            String certificateNumber,
            String certificateUuid,
            String verificationToken,
            CertificateType certificateType,
            CertificateStatus certificateStatus,
            CertificateVerificationStatus verificationStatus,
            boolean revoked,
            LocalDateTime issueDate,
            Instant generatedAt,
            Integer attendancePercentage,
            String position,
            String prize,
            String downloadUrl,
            String verificationUrl
    ) {
    }

    public record CertificateResponse(
            Long id,
            Long institutionId,
            String institutionName,
            Long eventId,
            String eventTitle,
            Long sessionId,
            String sessionTitle,
            Long academicYearId,
            String academicYearLabel,
            Long templateId,
            String templateName,
            Long recipientUserId,
            String recipientName,
            String recipientRole,
            String certificateNumber,
            String certificateUuid,
            String verificationToken,
            CertificateType certificateType,
            CertificateStatus certificateStatus,
            CertificateVerificationStatus verificationStatus,
            boolean revoked,
            LocalDateTime issueDate,
            Instant generatedAt,
            Integer attendancePercentage,
            String position,
            String prize,
            String verificationUrl,
            String pdfFileName,
            String downloadUrl,
            List<CertificateIssueLogResponse> issueLogs,
            List<CertificateAuditResponse> audits,
            List<CertificateVerificationResponse> verifications
    ) {
    }

    public record CertificateIssueLogResponse(
            Long id,
            Long certificateId,
            String actionType,
            Long actorUserId,
            String actorName,
            Instant occurredAt,
            String reason,
            String details
    ) {
    }

    public record CertificateAuditResponse(
            Long id,
            Long certificateId,
            String actionType,
            String previousStatus,
            String newStatus,
            Long actorUserId,
            String actorName,
            Instant occurredAt,
            String reason,
            String details
    ) {
    }

    public record CertificateVerificationResponse(
            Long id,
            String verificationToken,
            boolean valid,
            String message,
            Long certificateId,
            String certificateNumber,
            String certificateUuid,
            String recipientName,
            String institutionName,
            String eventTitle,
            CertificateType certificateType,
            CertificateVerificationStatus verificationStatus,
            boolean revoked,
            Instant verifiedAt,
            String verificationUrl
    ) {
    }

    public record CertificateSettingsResponse(
            String frontendBaseUrl,
            String verificationPath,
            boolean qrCodeEnabled,
            List<CertificateType> supportedCertificateTypes,
            List<String> supportedVariables
    ) {
    }

    public record CertificateListResponse(
            List<CertificateSummaryResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }
}
