package com.campussphere.service;

import com.campussphere.config.ApplicationProperties;
import com.campussphere.dto.PageResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateAuditResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateBulkIssueRequest;
import com.campussphere.dto.certificate.CertificateDtos.CertificateDashboardResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateIssueLogResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateIssueRequest;
import com.campussphere.dto.certificate.CertificateDtos.CertificateListResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateRevokeRequest;
import com.campussphere.dto.certificate.CertificateDtos.CertificateResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateSettingsResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateSummaryResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateTemplatePreviewResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateTemplateRequest;
import com.campussphere.dto.certificate.CertificateDtos.CertificateTemplateResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateTemplateSummaryResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateVerificationResponse;
import com.campussphere.entity.AcademicYear;
import com.campussphere.entity.Institution;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.User;
import com.campussphere.entity.certificate.Certificate;
import com.campussphere.entity.certificate.CertificateAudit;
import com.campussphere.entity.certificate.CertificateIssueAction;
import com.campussphere.entity.certificate.CertificateIssueLog;
import com.campussphere.entity.certificate.CertificateStatus;
import com.campussphere.entity.certificate.CertificateTemplate;
import com.campussphere.entity.certificate.CertificateTemplateOrientation;
import com.campussphere.entity.certificate.CertificateType;
import com.campussphere.entity.certificate.CertificateVerification;
import com.campussphere.entity.certificate.CertificateVerificationStatus;
import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventSession;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.event.EventCoordinator;
import com.campussphere.entity.registration.EventRegistration;
import com.campussphere.entity.registration.NotificationType;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.exception.BusinessRuleViolationException;
import com.campussphere.exception.ConflictException;
import com.campussphere.exception.DuplicateResourceException;
import com.campussphere.exception.InvalidInstitutionRelationshipException;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.repository.AcademicYearRepository;
import com.campussphere.repository.CertificateAuditRepository;
import com.campussphere.repository.CertificateIssueLogRepository;
import com.campussphere.repository.CertificateRepository;
import com.campussphere.repository.CertificateTemplateRepository;
import com.campussphere.repository.CertificateVerificationRepository;
import com.campussphere.repository.EventCoordinatorRepository;
import com.campussphere.repository.EventRegistrationRepository;
import com.campussphere.repository.EventRepository;
import com.campussphere.repository.EventSessionRepository;
import com.campussphere.repository.InAppNotificationRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.service.support.CurrentUserContext;
import com.campussphere.service.support.InstitutionScopeResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CertificateManagementService {

    private static final List<String> SUPPORTED_VARIABLES = List.of(
            "{{StudentName}}",
            "{{RegisterNumber}}",
            "{{Institution}}",
            "{{Department}}",
            "{{Programme}}",
            "{{EventName}}",
            "{{EventDate}}",
            "{{Coordinator}}",
            "{{CertificateNumber}}",
            "{{IssueDate}}",
            "{{Position}}",
            "{{Prize}}",
            "{{AttendancePercentage}}",
            "{{VerificationURL}}",
            "{{QRCode}}"
    );

    private final CertificateRepository certificateRepository;
    private final CertificateTemplateRepository templateRepository;
    private final CertificateIssueLogRepository issueLogRepository;
    private final CertificateVerificationRepository verificationRepository;
    private final CertificateAuditRepository auditRepository;
    private final EventRepository eventRepository;
    private final EventSessionRepository eventSessionRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventCoordinatorRepository coordinatorRepository;
    private final AcademicYearRepository academicYearRepository;
    private final UserRepository userRepository;
    private final InAppNotificationRepository notificationRepository;
    private final CurrentUserContext currentUserContext;
    private final InstitutionScopeResolver scopeResolver;
    private final ApplicationProperties applicationProperties;

    public CertificateManagementService(
            CertificateRepository certificateRepository,
            CertificateTemplateRepository templateRepository,
            CertificateIssueLogRepository issueLogRepository,
            CertificateVerificationRepository verificationRepository,
            CertificateAuditRepository auditRepository,
            EventRepository eventRepository,
            EventSessionRepository eventSessionRepository,
            EventRegistrationRepository registrationRepository,
            EventCoordinatorRepository coordinatorRepository,
            AcademicYearRepository academicYearRepository,
            UserRepository userRepository,
            InAppNotificationRepository notificationRepository,
            CurrentUserContext currentUserContext,
            InstitutionScopeResolver scopeResolver,
            ApplicationProperties applicationProperties
    ) {
        this.certificateRepository = certificateRepository;
        this.templateRepository = templateRepository;
        this.issueLogRepository = issueLogRepository;
        this.verificationRepository = verificationRepository;
        this.auditRepository = auditRepository;
        this.eventRepository = eventRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.registrationRepository = registrationRepository;
        this.coordinatorRepository = coordinatorRepository;
        this.academicYearRepository = academicYearRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.currentUserContext = currentUserContext;
        this.scopeResolver = scopeResolver;
        this.applicationProperties = applicationProperties;
    }

    @Transactional(readOnly = true)
    public CertificateDashboardResponse getDashboard(String email, Long institutionId) {
        User currentUser = requireCurrentUser(email);
        Institution institution = resolveInstitution(currentUser, institutionId, false);
        long totalCertificates = certificateRepository.countByInstitution_IdAndDeletedFalse(institution.getId());
        long revoked = certificateRepository.countByInstitution_IdAndCertificateStatusAndDeletedFalse(institution.getId(), CertificateStatus.REVOKED);
        long verified = certificateRepository.countByInstitution_IdAndVerificationStatusAndDeletedFalse(institution.getId(), CertificateVerificationStatus.VERIFIED);
        long pending = certificateRepository.countByInstitution_IdAndCertificateStatusAndDeletedFalse(institution.getId(), CertificateStatus.DRAFT);
        long downloads = issueLogRepository.count();
        long eligibleRecipients = registrationRepository.countByEvent_Institution_IdAndDeletedFalse(institution.getId());
        List<CertificateSummaryResponse> recentCertificates = listCertificates(email, institution.getId(), null, null, null, null, 0, 5).getContent();
        List<CertificateTemplateSummaryResponse> templates = listTemplates(email, institution.getId(), null, null, null, 0, 5).getContent();
        return new CertificateDashboardResponse(totalCertificates, pending, eligibleRecipients, revoked, verified, downloads, recentCertificates, templates);
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificateSummaryResponse> listCertificates(String email, Long institutionId, Long eventId, CertificateType certificateType, CertificateStatus certificateStatus, String search, int page, int size) {
        User currentUser = requireCurrentUser(email);
        Institution institution = resolveInstitution(currentUser, institutionId, true);
        Specification<Certificate> spec = (root, query, cb) -> cb.equal(root.get("deleted"), false);
        spec = spec.and((root, query, cb) -> cb.equal(root.get("institution").get("id"), institution.getId()));
        if (eventId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("event").get("id"), eventId));
        }
        if (certificateType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("certificateType"), certificateType));
        }
        if (certificateStatus != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("certificateStatus"), certificateStatus));
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("certificateNumber")), like),
                    cb.like(cb.lower(root.get("recipientName")), like),
                    cb.like(cb.lower(root.get("event").get("title")), like)
            ));
        }
        Page<Certificate> result = certificateRepository.findAll(spec, PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "generatedAt")));
        List<CertificateSummaryResponse> content = result.getContent().stream().map(this::toSummaryResponse).toList();
        return PageResponse.of(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }

    @Transactional(readOnly = true)
    public List<CertificateSummaryResponse> listMyCertificates(String email) {
        User currentUser = requireCurrentUser(email);
        return certificateRepository.findAll((root, query, cb) -> cb.and(
                        cb.equal(root.get("deleted"), false),
                        cb.equal(root.get("recipientUser").get("id"), currentUser.getId())
                ), Sort.by(Sort.Direction.DESC, "generatedAt"))
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificateTemplateSummaryResponse> listTemplates(String email, Long institutionId, CertificateType certificateType, Boolean active, String search, int page, int size) {
        User currentUser = requireCurrentUser(email);
        Institution institution = resolveInstitution(currentUser, institutionId, true);
        Specification<CertificateTemplate> spec = (root, query, cb) -> cb.equal(root.get("deleted"), false);
        spec = spec.and((root, query, cb) -> cb.equal(root.get("institution").get("id"), institution.getId()));
        if (certificateType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("certificateType"), certificateType));
        }
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("templateCode")), like),
                    cb.like(cb.lower(root.get("templateName")), like)
            ));
        }
        Page<CertificateTemplate> result = templateRepository.findAll(spec, PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "updatedAt")));
        List<CertificateTemplateSummaryResponse> content = result.getContent().stream().map(this::toTemplateSummaryResponse).toList();
        return PageResponse.of(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }

    @Transactional(readOnly = true)
    public CertificateTemplateResponse getTemplate(String email, Long templateId) {
        User currentUser = requireCurrentUser(email);
        CertificateTemplate template = requireTemplate(currentUser, templateId);
        return toTemplateResponse(template);
    }

    public CertificateTemplateResponse createTemplate(String email, CertificateTemplateRequest request) {
        User currentUser = requireCurrentUser(email);
        Institution institution = resolveWriteInstitution(currentUser, null);
        validateTemplateUniqueness(institution.getId(), request.templateCode(), null);
        CertificateTemplate template = new CertificateTemplate();
        template.setInstitution(institution);
        applyTemplateRequest(template, request);
        template.setStatus(RecordStatus.ACTIVE);
        templateRepository.save(template);
        auditTemplate(template, currentUser, CertificateIssueAction.ISSUED, "Template created.");
        return toTemplateResponse(template);
    }

    public CertificateTemplateResponse updateTemplate(String email, Long templateId, CertificateTemplateRequest request) {
        User currentUser = requireCurrentUser(email);
        CertificateTemplate template = requireTemplate(currentUser, templateId);
        validateTemplateUniqueness(template.getInstitution().getId(), request.templateCode(), template.getId());
        applyTemplateRequest(template, request);
        templateRepository.save(template);
        auditTemplate(template, currentUser, CertificateIssueAction.REGENERATED, "Template updated.");
        return toTemplateResponse(template);
    }

    public CertificateTemplateResponse duplicateTemplate(String email, Long templateId, CertificateTemplateRequest request) {
        User currentUser = requireCurrentUser(email);
        CertificateTemplate source = requireTemplate(currentUser, templateId);
        Institution institution = resolveWriteInstitution(currentUser, source.getInstitution().getId());
        validateTemplateUniqueness(institution.getId(), request.templateCode(), null);
        CertificateTemplate template = new CertificateTemplate();
        template.setInstitution(institution);
        applyTemplateRequest(template, request);
        templateRepository.save(template);
        auditTemplate(template, currentUser, CertificateIssueAction.ISSUED, "Template duplicated from " + source.getTemplateCode());
        return toTemplateResponse(template);
    }

    public CertificateTemplateResponse toggleTemplateStatus(String email, Long templateId, boolean active) {
        User currentUser = requireCurrentUser(email);
        CertificateTemplate template = requireTemplate(currentUser, templateId);
        template.setActive(active);
        templateRepository.save(template);
        auditTemplate(template, currentUser, active ? CertificateIssueAction.REGENERATED : CertificateIssueAction.REVOKED, active ? "Template activated." : "Template deactivated.");
        return toTemplateResponse(template);
    }

    public void deleteTemplate(String email, Long templateId) {
        User currentUser = requireCurrentUser(email);
        CertificateTemplate template = requireTemplate(currentUser, templateId);
        template.setDeleted(true);
        template.setDeletedAt(nowInstant());
        templateRepository.save(template);
        auditTemplate(template, currentUser, CertificateIssueAction.REVOKED, "Template deleted.");
    }

    @Transactional(readOnly = true)
    public CertificateTemplatePreviewResponse previewTemplate(String email, Long templateId) {
        CertificateTemplate template = requireTemplate(requireCurrentUser(email), templateId);
        return new CertificateTemplatePreviewResponse(toTemplateResponse(template), SUPPORTED_VARIABLES, buildTemplatePreview(template));
    }

    public CertificateResponse issueCertificate(String email, CertificateIssueRequest request) {
        User currentUser = requireCurrentUser(email);
        return issueCertificateInternal(currentUser, request, true);
    }

    @Transactional(readOnly = true)
    public CertificateResponse previewCertificate(String email, CertificateIssueRequest request) {
        User currentUser = requireCurrentUser(email);
        return issueCertificateInternal(currentUser, request, false);
    }

    public List<CertificateResponse> issueCertificatesBulk(String email, CertificateBulkIssueRequest request) {
        User currentUser = requireCurrentUser(email);
        return request.recipientUserIds().stream().map(recipientId -> issueCertificateInternal(currentUser, new CertificateIssueRequest(
                request.eventId(),
                request.sessionId(),
                request.academicYearId(),
                recipientId,
                request.certificateType(),
                request.templateId(),
                request.recipientRole(),
                request.position(),
                request.prize(),
                request.attendancePercentage(),
                request.issueDate(),
                request.adminOverride(),
                request.remarks()
        ), true)).toList();
    }

    public CertificateResponse regenerateCertificate(String email, Long certificateId) {
        User currentUser = requireCurrentUser(email);
        Certificate certificate = requireCertificate(currentUser, certificateId);
        certificate.setVerificationToken(generateVerificationToken());
        certificate.setCertificateNumber(generateCertificateNumber(certificate.getInstitution(), certificate.getEvent(), certificate.getCertificateType()));
        certificate.setGeneratedAt(nowInstant());
        certificate.setCertificateStatus(CertificateStatus.ISSUED);
        certificate.setVerificationStatus(CertificateVerificationStatus.NOT_VERIFIED);
        certificate.setRevoked(false);
        certificate.setRevokedAt(null);
        certificate.setRevokedBy(null);
        certificate.setRevocationReason(null);
        certificate.setPdfFileName(buildPdfFileName(certificate));
        certificate.setVerificationUrl(buildVerificationUrl(certificate.getVerificationToken(), certificate.getTemplate()));
        certificateRepository.save(certificate);
        audit(certificate, currentUser, CertificateIssueAction.REGENERATED, CertificateStatus.REVOKED.name(), CertificateStatus.ISSUED.name(), "Certificate regenerated.");
        issueLog(certificate, currentUser, CertificateIssueAction.REGENERATED, "Certificate regenerated.", null);
        return toCertificateResponse(certificate, null);
    }

    public CertificateResponse revokeCertificate(String email, Long certificateId, CertificateRevokeRequest request) {
        User currentUser = requireCurrentUser(email);
        Certificate certificate = requireCertificate(currentUser, certificateId);
        if (certificate.isRevoked()) {
            return toCertificateResponse(certificate, null);
        }
        certificate.setRevoked(true);
        certificate.setRevokedAt(nowInstant());
        certificate.setRevokedBy(currentUser);
        certificate.setRevocationReason(trimToNull(request.reason()));
        certificate.setCertificateStatus(CertificateStatus.REVOKED);
        certificate.setVerificationStatus(CertificateVerificationStatus.REVOKED);
        certificateRepository.save(certificate);
        audit(certificate, currentUser, CertificateIssueAction.REVOKED, CertificateStatus.ISSUED.name(), CertificateStatus.REVOKED.name(), request.reason());
        issueLog(certificate, currentUser, CertificateIssueAction.REVOKED, request.reason(), null);
        notifyRecipient(certificate.getRecipientUser(), NotificationType.CERTIFICATE_REVOKED, "Certificate revoked", "Your certificate for " + certificate.getEvent().getTitle() + " was revoked.", certificate.getId());
        return toCertificateResponse(certificate, null);
    }

    @Transactional(readOnly = true)
    public CertificateResponse getCertificate(String email, Long certificateId) {
        User currentUser = requireCurrentUser(email);
        Certificate certificate = requireCertificate(currentUser, certificateId);
        return toCertificateResponse(certificate, null);
    }

    @Transactional(readOnly = true)
    public CertificateVerificationResponse verifyToken(String token, HttpServletRequest request) {
        CertificateVerification verification = new CertificateVerification();
        verification.setVerificationToken(token);
        verification.setVerifiedAt(nowInstant());
        verification.setVerifiedIp(request == null ? null : trimToNull(request.getRemoteAddr()));
        verification.setUserAgent(request == null ? null : trimToLimit(request.getHeader("User-Agent"), 255));

        Optional<Certificate> certificateOptional = certificateRepository.findByVerificationTokenAndDeletedFalse(token);
        CertificateVerificationResponse response;
        if (certificateOptional.isEmpty()) {
            verification.setVerificationStatus(CertificateVerificationStatus.INVALID);
            verification.setMessage("The certificate token is not valid.");
            verificationRepository.save(verification);
            response = new CertificateVerificationResponse(null, token, false, verification.getMessage(), null, null, null, null, null, null, null, CertificateVerificationStatus.INVALID, false, verification.getVerifiedAt(), buildVerificationUrl(token, null));
            return response;
        }

        Certificate certificate = certificateOptional.get();
        CertificateVerificationStatus status = certificate.isRevoked() ? CertificateVerificationStatus.REVOKED : CertificateVerificationStatus.VERIFIED;
        String message = certificate.isRevoked() ? "This certificate has been revoked." : "The certificate is valid.";
        verification.setCertificate(certificate);
        verification.setVerificationStatus(status);
        verification.setMessage(message);
        verification.setCertificateNumber(certificate.getCertificateNumber());
        verification.setRecipientName(certificate.getRecipientName());
        verification.setInstitutionName(certificate.getInstitution().getInstitutionName());
        verification.setEventTitle(certificate.getEvent().getTitle());
        verification.setCertificateType(certificate.getCertificateType());
        verificationRepository.save(verification);
        certificate.setVerificationStatus(status);
        certificateRepository.save(certificate);
        response = new CertificateVerificationResponse(certificate.getId(), token, status == CertificateVerificationStatus.VERIFIED, message, certificate.getId(), certificate.getCertificateNumber(), certificate.getCertificateUuid(), certificate.getRecipientName(), certificate.getInstitution().getInstitutionName(), certificate.getEvent().getTitle(), certificate.getCertificateType(), status, certificate.isRevoked(), verification.getVerifiedAt(), buildVerificationUrl(token, certificate.getTemplate()));
        return response;
    }

    @Transactional(readOnly = true)
    public byte[] downloadCertificate(String email, Long certificateId) {
        Certificate certificate = requireCertificate(requireCurrentUser(email), certificateId);
        return renderCertificatePdf(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateSettingsResponse getSettings(String email, Long institutionId) {
        User currentUser = requireCurrentUser(email);
        resolveInstitution(currentUser, institutionId, true);
        return new CertificateSettingsResponse(
                applicationProperties.getFrontendBaseUrl(),
                "/verify/{token}",
                true,
                List.of(CertificateType.values()),
                SUPPORTED_VARIABLES
        );
    }

    private CertificateResponse issueCertificateInternal(User actor, CertificateIssueRequest request, boolean persist) {
        Event event = requireEvent(request.eventId());
        Institution institution = scopeResolver.resolveForWrite(actor, event.getInstitution().getId());
        User recipient = requireUser(request.recipientUserId());
        ensureSameInstitution(institution, recipient);
        EventSession session = request.sessionId() == null ? null : requireEventSession(request.sessionId(), event.getId());
        AcademicYear academicYear = request.academicYearId() == null ? null : requireAcademicYear(request.academicYearId(), institution.getId());
        CertificateTemplate template = request.templateId() == null ? findDefaultTemplate(institution.getId(), request.certificateType()) : requireTemplate(actor, request.templateId());
        if (!Objects.equals(template.getInstitution().getId(), institution.getId())) {
            throw new InvalidInstitutionRelationshipException("The selected template does not belong to the event institution.");
        }
        if (template.getCertificateType() != request.certificateType()) {
            throw new InvalidInstitutionRelationshipException("The selected template does not support this certificate type.");
        }
        if (certificateRepository.existsByInstitution_IdAndEvent_IdAndRecipientUser_IdAndCertificateTypeAndDeletedFalse(institution.getId(), event.getId(), recipient.getId(), request.certificateType())) {
            throw new DuplicateResourceException("A certificate already exists for this recipient and event.");
        }
        validateEligibility(actor, event, recipient, request);
        Certificate certificate = new Certificate();
        certificate.setInstitution(institution);
        certificate.setEvent(event);
        certificate.setSession(session);
        certificate.setAcademicYear(academicYear);
        certificate.setTemplate(template);
        certificate.setRecipientUser(recipient);
        certificate.setRecipientName(displayName(recipient));
        certificate.setRecipientRole(trimToDefault(request.recipientRole(), certificateRoleFor(request.certificateType(), recipient)));
        certificate.setCertificateType(request.certificateType());
        certificate.setIssueBy(actor);
        certificate.setIssueDate(request.issueDate() == null ? LocalDateTime.now(ZoneOffset.UTC) : request.issueDate());
        certificate.setGeneratedAt(nowInstant());
        certificate.setCertificateStatus(CertificateStatus.ISSUED);
        certificate.setVerificationStatus(CertificateVerificationStatus.NOT_VERIFIED);
        certificate.setVerificationToken(generateVerificationToken());
        certificate.setCertificateNumber(generateCertificateNumber(institution, event, request.certificateType()));
        certificate.setPosition(trimToNull(request.position()));
        certificate.setPrize(trimToNull(request.prize()));
        certificate.setAttendancePercentage(request.attendancePercentage());
        certificate.setVerificationUrl(buildVerificationUrl(certificate.getVerificationToken(), template));
        certificate.setPdfFileName(buildPdfFileName(certificate));
        if (persist) {
            certificateRepository.save(certificate);
            issueLog(certificate, actor, CertificateIssueAction.ISSUED, "Certificate issued.", request.remarks());
            audit(certificate, actor, CertificateIssueAction.ISSUED, null, certificate.getCertificateStatus().name(), request.remarks());
            notifyRecipient(recipient, NotificationType.CERTIFICATE_ISSUED, "Certificate issued", "A certificate was issued for " + event.getTitle() + ".", certificate.getId());
            renderCertificatePdf(certificate);
        }
        return toCertificateResponse(certificate, null);
    }

    private void validateEligibility(User actor, Event event, User recipient, CertificateIssueRequest request) {
        if (!Objects.equals(event.getInstitution().getId(), recipient.getInstitution() == null ? null : recipient.getInstitution().getId())) {
            throw new InvalidInstitutionRelationshipException("The selected recipient does not belong to this institution.");
        }
        boolean adminOverride = Boolean.TRUE.equals(request.adminOverride());
        switch (request.certificateType()) {
            case PARTICIPATION -> {
                EventRegistration registration = registrationRepository.findByEvent_IdAndParticipant_IdAndDeletedFalse(event.getId(), recipient.getId()).stream().findFirst()
                        .orElseThrow(() -> new BusinessRuleViolationException("The recipient does not have a registration for this event."));
                if (!registration.isCertificateEligible() && !adminOverride) {
                    throw new BusinessRuleViolationException("The recipient is not yet eligible for a participation certificate.");
                }
            }
            case WINNER -> {
                if (trimToNull(request.position()) == null && !adminOverride) {
                    throw new BusinessRuleViolationException("Winner certificates require a position.");
                }
            }
            case ORGANIZER, FACULTY_COORDINATOR -> {
                boolean coordinator = coordinatorRepository.existsByEvent_IdAndUser_IdAndDeletedFalse(event.getId(), recipient.getId());
                if (!coordinator && !currentUserContext.isAdministrator(actor) && !adminOverride) {
                    throw new BusinessRuleViolationException("The recipient is not assigned as an organiser for this event.");
                }
            }
            case VOLUNTEER, JUDGE -> {
                if (!adminOverride && trimToNull(request.recipientRole()) == null) {
                    throw new BusinessRuleViolationException("Please provide a valid recipient role for this certificate type.");
                }
            }
        }
    }

    private CertificateTemplate findDefaultTemplate(Long institutionId, CertificateType certificateType) {
        return templateRepository.findByInstitution_IdAndCertificateTypeAndDeletedFalseOrderByTemplateNameAsc(institutionId, certificateType)
                .stream()
                .filter(CertificateTemplate::isActive)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException("Create an active certificate template before generating certificates."));
    }

    private CertificateTemplate requireTemplate(User currentUser, Long templateId) {
        CertificateTemplate template = templateRepository.findByIdAndDeletedFalse(templateId).orElseThrow(() -> new ResourceNotFoundException("Certificate template not found."));
        scopeResolver.resolveForRead(currentUser, template.getInstitution().getId());
        return template;
    }

    private Certificate requireCertificate(User currentUser, Long certificateId) {
        Certificate certificate = certificateRepository.findByIdAndDeletedFalse(certificateId).orElseThrow(() -> new ResourceNotFoundException("Certificate not found."));
        scopeResolver.resolveForRead(currentUser, certificate.getInstitution().getId());
        return certificate;
    }

    private Event requireEvent(Long eventId) {
        return eventRepository.findByIdAndDeletedFalse(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found."));
    }

    private EventSession requireEventSession(Long sessionId, Long eventId) {
        EventSession session = eventSessionRepository.findByIdAndDeletedFalse(sessionId).orElseThrow(() -> new ResourceNotFoundException("Event session not found."));
        if (!Objects.equals(session.getEvent().getId(), eventId)) {
            throw new InvalidInstitutionRelationshipException("The selected session does not belong to this event.");
        }
        return session;
    }

    private AcademicYear requireAcademicYear(Long academicYearId, Long institutionId) {
        AcademicYear academicYear = academicYearRepository.findByIdAndDeletedFalse(academicYearId).orElseThrow(() -> new ResourceNotFoundException("Academic year not found."));
        if (!Objects.equals(academicYear.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("The selected academic year does not belong to this institution.");
        }
        return academicYear;
    }

    private User requireUser(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private User requireCurrentUser(String email) {
        return currentUserContext.requireCurrentUser(email);
    }

    private Institution resolveInstitution(User currentUser, Long institutionId, boolean allowReadAcross) {
        if (institutionId != null) {
            return allowReadAcross ? scopeResolver.resolveForRead(currentUser, institutionId) : scopeResolver.resolveForWrite(currentUser, institutionId);
        }
        if (currentUser.getInstitution() != null) {
            return currentUser.getInstitution();
        }
        throw new BusinessRuleViolationException("Institution scope is required.");
    }

    private Institution resolveWriteInstitution(User currentUser, Long preferredInstitutionId) {
        if (preferredInstitutionId != null) {
            return scopeResolver.resolveForWrite(currentUser, preferredInstitutionId);
        }
        if (currentUser.getInstitution() != null) {
            return scopeResolver.resolveForWrite(currentUser, currentUser.getInstitution().getId());
        }
        throw new BusinessRuleViolationException("Institution scope is required.");
    }

    private void validateTemplateUniqueness(Long institutionId, String templateCode, Long existingId) {
        if (existingId == null) {
            if (templateRepository.existsByInstitution_IdAndTemplateCodeIgnoreCaseAndDeletedFalse(institutionId, templateCode)) {
                throw new DuplicateResourceException("A certificate template with this code already exists.");
            }
        } else if (templateRepository.existsByInstitution_IdAndTemplateCodeIgnoreCaseAndIdNotAndDeletedFalse(institutionId, templateCode, existingId)) {
            throw new DuplicateResourceException("A certificate template with this code already exists.");
        }
    }

    private void applyTemplateRequest(CertificateTemplate template, CertificateTemplateRequest request) {
        template.setTemplateCode(trimToNull(request.templateCode()).toUpperCase(Locale.ROOT));
        template.setTemplateName(trimToNull(request.templateName()));
        template.setCertificateType(request.certificateType());
        template.setOrientation(request.orientation());
        template.setDescription(trimToNull(request.description()));
        template.setInstitutionLogoUrl(trimToNull(request.institutionLogoUrl()));
        template.setOrganizerLogoUrl(trimToNull(request.organizerLogoUrl()));
        template.setBackgroundImageUrl(trimToNull(request.backgroundImageUrl()));
        template.setSignatureLeftUrl(trimToNull(request.signatureLeftUrl()));
        template.setSignatureRightUrl(trimToNull(request.signatureRightUrl()));
        template.setSealUrl(trimToNull(request.sealUrl()));
        template.setPrimaryColor(trimToNull(request.primaryColor()));
        template.setAccentColor(trimToNull(request.accentColor()));
        template.setWatermarkText(trimToNull(request.watermarkText()));
        template.setMarginTopMm(request.marginTopMm());
        template.setMarginRightMm(request.marginRightMm());
        template.setMarginBottomMm(request.marginBottomMm());
        template.setMarginLeftMm(request.marginLeftMm());
        template.setQrCodeEnabled(request.qrCodeEnabled() == null || request.qrCodeEnabled());
        template.setVerificationUrlBase(trimToNull(request.verificationUrlBase()));
        template.setTemplateHtml(trimToNull(request.templateHtml()));
        template.setVariablesJson(trimToNull(request.variablesJson()));
        template.setActive(request.active() == null || request.active());
    }

    private String certificateRoleFor(CertificateType type, User recipient) {
        return switch (type) {
            case PARTICIPATION -> "Participant";
            case WINNER -> "Winner";
            case ORGANIZER -> "Organizer";
            case VOLUNTEER -> "Volunteer";
            case JUDGE -> "Judge";
            case FACULTY_COORDINATOR -> "Faculty Coordinator";
        };
    }

    private String generateCertificateNumber(Institution institution, Event event, CertificateType type) {
        String seed = (institution.getInstitutionCode() == null ? "CS" : institution.getInstitutionCode()).replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String eventCode = event.getEventCode() == null ? "EVENT" : event.getEventCode().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String candidate = "CERT-" + seed + "-" + eventCode + "-" + type.name().substring(0, Math.min(3, type.name().length())) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        while (certificateRepository.existsByInstitution_IdAndCertificateNumberIgnoreCaseAndDeletedFalse(institution.getId(), candidate)) {
            candidate = candidate + "X";
        }
        return candidate;
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String buildVerificationUrl(String token, CertificateTemplate template) {
        String base = template != null && trimToNull(template.getVerificationUrlBase()) != null
                ? trimToNull(template.getVerificationUrlBase())
                : trimToNull(applicationProperties.getFrontendBaseUrl());
        if (base == null) {
            base = "http://localhost:5173";
        }
        return base.replaceAll("/+$", "") + "/verify/" + token;
    }

    private String buildPdfFileName(Certificate certificate) {
        String safeNumber = certificate.getCertificateNumber().replaceAll("[^A-Za-z0-9_-]", "_");
        return safeNumber + ".pdf";
    }

    private CertificateResponse toCertificateResponse(Certificate certificate, byte[] pdfBytes) {
        List<CertificateIssueLogResponse> issueLogs = certificate.getId() == null ? List.of() : issueLogRepository.findByCertificate_IdAndDeletedFalseOrderByOccurredAtDesc(certificate.getId()).stream().map(this::toIssueLogResponse).toList();
        List<CertificateAuditResponse> audits = certificate.getId() == null ? List.of() : auditRepository.findByCertificate_IdAndDeletedFalseOrderByOccurredAtDesc(certificate.getId()).stream().map(this::toAuditResponse).toList();
        List<CertificateVerificationResponse> verifications = certificate.getId() == null ? List.of() : verificationRepository.findByCertificate_IdAndDeletedFalseOrderByVerifiedAtDesc(certificate.getId()).stream().map(this::toVerificationResponse).toList();
        return new CertificateResponse(
                certificate.getId(),
                certificate.getInstitution().getId(),
                certificate.getInstitution().getInstitutionName(),
                certificate.getEvent().getId(),
                certificate.getEvent().getTitle(),
                certificate.getSession() == null ? null : certificate.getSession().getId(),
                certificate.getSession() == null ? null : certificate.getSession().getTitle(),
                certificate.getAcademicYear() == null ? null : certificate.getAcademicYear().getId(),
                certificate.getAcademicYear() == null ? null : certificate.getAcademicYear().getYearLabel(),
                certificate.getTemplate().getId(),
                certificate.getTemplate().getTemplateName(),
                certificate.getRecipientUser().getId(),
                certificate.getRecipientName(),
                certificate.getRecipientRole(),
                certificate.getCertificateNumber(),
                certificate.getCertificateUuid(),
                certificate.getVerificationToken(),
                certificate.getCertificateType(),
                certificate.getCertificateStatus(),
                certificate.getVerificationStatus(),
                certificate.isRevoked(),
                certificate.getIssueDate(),
                certificate.getGeneratedAt(),
                certificate.getAttendancePercentage(),
                certificate.getPosition(),
                certificate.getPrize(),
                certificate.getVerificationUrl(),
                certificate.getPdfFileName(),
                "/api/certificates/" + certificate.getId() + "/download",
                issueLogs,
                audits,
                verifications
        );
    }

    private CertificateSummaryResponse toSummaryResponse(Certificate certificate) {
        return new CertificateSummaryResponse(
                certificate.getId(),
                certificate.getInstitution().getId(),
                certificate.getInstitution().getInstitutionName(),
                certificate.getEvent().getId(),
                certificate.getEvent().getTitle(),
                certificate.getSession() == null ? null : certificate.getSession().getId(),
                certificate.getSession() == null ? null : certificate.getSession().getTitle(),
                certificate.getRecipientUser().getId(),
                certificate.getRecipientName(),
                certificate.getRecipientRole(),
                certificate.getCertificateNumber(),
                certificate.getCertificateUuid(),
                certificate.getVerificationToken(),
                certificate.getCertificateType(),
                certificate.getCertificateStatus(),
                certificate.getVerificationStatus(),
                certificate.isRevoked(),
                certificate.getIssueDate(),
                certificate.getGeneratedAt(),
                certificate.getAttendancePercentage(),
                certificate.getPosition(),
                certificate.getPrize(),
                "/api/certificates/" + certificate.getId() + "/download",
                certificate.getVerificationUrl()
        );
    }

    private CertificateTemplateSummaryResponse toTemplateSummaryResponse(CertificateTemplate template) {
        return new CertificateTemplateSummaryResponse(
                template.getId(),
                template.getInstitution().getId(),
                template.getInstitution().getInstitutionName(),
                template.getTemplateCode(),
                template.getTemplateName(),
                template.getCertificateType(),
                template.getOrientation(),
                template.isQrCodeEnabled(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    private CertificateTemplateResponse toTemplateResponse(CertificateTemplate template) {
        return new CertificateTemplateResponse(
                template.getId(),
                template.getInstitution().getId(),
                template.getInstitution().getInstitutionName(),
                template.getTemplateCode(),
                template.getTemplateName(),
                template.getCertificateType(),
                template.getOrientation(),
                template.getDescription(),
                template.getInstitutionLogoUrl(),
                template.getOrganizerLogoUrl(),
                template.getBackgroundImageUrl(),
                template.getSignatureLeftUrl(),
                template.getSignatureRightUrl(),
                template.getSealUrl(),
                template.getPrimaryColor(),
                template.getAccentColor(),
                template.getWatermarkText(),
                template.getMarginTopMm(),
                template.getMarginRightMm(),
                template.getMarginBottomMm(),
                template.getMarginLeftMm(),
                template.isQrCodeEnabled(),
                template.getVerificationUrlBase(),
                template.getTemplateHtml(),
                template.getVariablesJson(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    private CertificateIssueLogResponse toIssueLogResponse(CertificateIssueLog log) {
        return new CertificateIssueLogResponse(
                log.getId(),
                log.getCertificate() == null ? null : log.getCertificate().getId(),
                log.getActionType() == null ? null : log.getActionType().name(),
                log.getActor() == null ? null : log.getActor().getId(),
                displayName(log.getActor()),
                log.getOccurredAt(),
                log.getReason(),
                log.getDetails()
        );
    }

    private CertificateAuditResponse toAuditResponse(CertificateAudit audit) {
        return new CertificateAuditResponse(
                audit.getId(),
                audit.getCertificate() == null ? null : audit.getCertificate().getId(),
                audit.getActionType() == null ? null : audit.getActionType().name(),
                audit.getPreviousStatus(),
                audit.getNewStatus(),
                audit.getActor() == null ? null : audit.getActor().getId(),
                displayName(audit.getActor()),
                audit.getOccurredAt(),
                audit.getReason(),
                audit.getDetails()
        );
    }

    private CertificateVerificationResponse toVerificationResponse(CertificateVerification verification) {
        return new CertificateVerificationResponse(
                verification.getId(),
                verification.getVerificationToken(),
                verification.getVerificationStatus() == CertificateVerificationStatus.VERIFIED,
                verification.getMessage(),
                verification.getCertificate() == null ? null : verification.getCertificate().getId(),
                verification.getCertificateNumber(),
                verification.getCertificate() == null ? null : verification.getCertificate().getCertificateUuid(),
                verification.getRecipientName(),
                verification.getInstitutionName(),
                verification.getEventTitle(),
                verification.getCertificateType(),
                verification.getVerificationStatus(),
                verification.getVerificationStatus() == CertificateVerificationStatus.REVOKED,
                verification.getVerifiedAt(),
                verification.getCertificate() == null ? buildVerificationUrl(verification.getVerificationToken(), null) : verification.getCertificate().getVerificationUrl()
        );
    }

    private void issueLog(Certificate certificate, User actor, CertificateIssueAction actionType, String reason, String details) {
        CertificateIssueLog log = new CertificateIssueLog();
        log.setCertificate(certificate);
        log.setActor(actor);
        log.setActionType(actionType);
        log.setOccurredAt(nowInstant());
        log.setReason(reason);
        log.setDetails(details);
        issueLogRepository.save(log);
    }

    private void audit(Certificate certificate, User actor, CertificateIssueAction actionType, String previousStatus, String newStatus, String reason) {
        CertificateAudit audit = new CertificateAudit();
        audit.setCertificate(certificate);
        audit.setActionType(actionType);
        audit.setPreviousStatus(previousStatus);
        audit.setNewStatus(newStatus);
        audit.setActor(actor);
        audit.setOccurredAt(nowInstant());
        audit.setReason(reason);
        audit.setDetails(reason);
        auditRepository.save(audit);
    }

    private void auditTemplate(CertificateTemplate template, User actor, CertificateIssueAction actionType, String details) {
        CertificateAudit audit = new CertificateAudit();
        audit.setCertificate(null);
        audit.setActionType(actionType);
        audit.setPreviousStatus(null);
        audit.setNewStatus(template.isActive() ? "ACTIVE" : "INACTIVE");
        audit.setActor(actor);
        audit.setOccurredAt(nowInstant());
        audit.setReason(details);
        audit.setDetails(details);
        auditRepository.save(audit);
    }

    private void notifyRecipient(User recipient, NotificationType type, String title, String message, Long relatedEntityId) {
        if (recipient == null) {
            return;
        }
        com.campussphere.entity.registration.InAppNotification notification = new com.campussphere.entity.registration.InAppNotification();
        notification.setRecipient(recipient);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType("Certificate");
        notification.setRelatedEntityId(relatedEntityId);
        notificationRepository.save(notification);
    }

    private void ensureSameInstitution(Institution institution, User user) {
        if (user.getInstitution() == null || !Objects.equals(institution.getId(), user.getInstitution().getId())) {
            throw new InvalidInstitutionRelationshipException("The selected user does not belong to this institution.");
        }
    }

    private String buildTemplatePreview(CertificateTemplate template) {
        Map<String, String> sample = new LinkedHashMap<>();
        sample.put("StudentName", "Asha Menon");
        sample.put("RegisterNumber", "24CSE0001");
        sample.put("Institution", template.getInstitution().getInstitutionName());
        sample.put("Department", "Computer Science and Engineering");
        sample.put("Programme", "B.Tech Computer Science and Engineering");
        sample.put("EventName", "CampusSphere Symposium");
        sample.put("EventDate", "07 Aug 2026");
        sample.put("Coordinator", "Faculty Coordinator");
        sample.put("CertificateNumber", "CERT-SAMPLE-001");
        sample.put("IssueDate", "07 Aug 2026");
        sample.put("Position", "Winner");
        sample.put("Prize", "First Prize");
        sample.put("AttendancePercentage", "92%");
        sample.put("VerificationURL", buildVerificationUrl("sample-token", template));
        sample.put("QRCode", "[QR]");
        return sample.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).reduce((left, right) -> left + "\n" + right).orElse("");
    }

    private byte[] renderCertificatePdf(Certificate certificate) {
        try (PDDocument document = new PDDocument()) {
            PDRectangle size = certificate.getTemplate().getOrientation() == CertificateTemplateOrientation.LANDSCAPE
                    ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                    : PDRectangle.A4;
            PDPage page = new PDPage(size);
            document.addPage(page);
            float width = size.getWidth();
            float height = size.getHeight();
            try (PDPageContentStream content = new PDPageContentStream(document, page, AppendMode.OVERWRITE, true, true)) {
                content.setNonStrokingColor(248, 250, 252);
                content.addRect(0, 0, width, height);
                content.fill();

                content.setStrokingColor(15, 23, 42);
                content.setLineWidth(2f);
                content.addRect(24, 24, width - 48, height - 48);
                content.stroke();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 26);
                content.newLineAtOffset(48, height - 70);
                content.showText("CampusSphere Certificate");
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(48, height - 100);
                content.showText(certificate.getInstitution().getInstitutionName());
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                content.newLineAtOffset(48, height - 150);
                content.showText("This certificate is awarded to");
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 28);
                content.newLineAtOffset(48, height - 190);
                content.showText(safePdfText(certificate.getRecipientName()));
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 14);
                content.newLineAtOffset(48, height - 230);
                content.showText(safePdfText(certificate.getCertificateType().name().replace('_', ' ') + " certificate for " + certificate.getEvent().getTitle()));
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(48, height - 270);
                content.showText("Certificate Number: " + safePdfText(certificate.getCertificateNumber()));
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(48, height - 290);
                content.showText("Issue Date: " + certificate.getIssueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 10);
                content.newLineAtOffset(48, 80);
                content.showText("Verification: " + certificate.getVerificationUrl());
                content.endText();

                if (certificate.getTemplate() != null && certificate.getTemplate().isQrCodeEnabled()) {
                    PDImageXObject qrImage = buildQrImage(document, certificate.getVerificationUrl());
                    float qrSize = 96f;
                    content.drawImage(qrImage, width - qrSize - 48, 48, qrSize, qrSize);
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to render certificate PDF.", exception);
        }
    }

    private PDImageXObject buildQrImage(PDDocument document, String text) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 240, 240, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
            return LosslessFactory.createFromImage(document, bufferedImage);
        } catch (Exception exception) {
            BufferedImage fallback = new BufferedImage(240, 240, BufferedImage.TYPE_INT_RGB);
            return LosslessFactory.createFromImage(document, fallback);
        }
    }

    private String safePdfText(String value) {
        return value == null ? "" : value.replace("\n", " ").replace("\r", " ");
    }

    private String displayName(User user) {
        if (user == null) {
            return null;
        }
        String first = trimToNull(user.getFirstName());
        String last = trimToNull(user.getLastName());
        if (first == null && last == null) {
            return trimToNull(user.getEmail());
        }
        if (first == null) {
            return last;
        }
        if (last == null) {
            return first;
        }
        return first + " " + last;
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private String trimToLimit(String value, int limit) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.length() > limit ? trimmed.substring(0, limit) : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Instant nowInstant() {
        return Instant.now();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
