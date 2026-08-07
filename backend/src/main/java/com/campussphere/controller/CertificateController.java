package com.campussphere.controller;

import com.campussphere.dto.ApiResponse;
import com.campussphere.dto.PageResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateBulkIssueRequest;
import com.campussphere.dto.certificate.CertificateDtos.CertificateDashboardResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateIssueRequest;
import com.campussphere.dto.certificate.CertificateDtos.CertificateRevokeRequest;
import com.campussphere.dto.certificate.CertificateDtos.CertificateResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateSettingsResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateSummaryResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateTemplatePreviewResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateTemplateRequest;
import com.campussphere.dto.certificate.CertificateDtos.CertificateTemplateResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateTemplateSummaryResponse;
import com.campussphere.dto.certificate.CertificateDtos.CertificateVerificationResponse;
import com.campussphere.entity.certificate.CertificateStatus;
import com.campussphere.entity.certificate.CertificateType;
import com.campussphere.service.CertificateManagementService;
import com.campussphere.util.ApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CertificateController {

    private final CertificateManagementService service;

    public CertificateController(CertificateManagementService service) {
        this.service = service;
    }

    @GetMapping("/certificates/dashboard")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateDashboardResponse> dashboard(@RequestParam(required = false) Long institutionId) {
        return ApiResponseFactory.success("Certificate dashboard retrieved successfully.", service.getDashboard(currentUserEmail(), institutionId));
    }

    @GetMapping("/certificates")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<PageResponse<CertificateSummaryResponse>> listCertificates(@RequestParam(required = false) Long institutionId,
                                                                                   @RequestParam(required = false) Long eventId,
                                                                                   @RequestParam(required = false) CertificateType certificateType,
                                                                                   @RequestParam(required = false) CertificateStatus certificateStatus,
                                                                                   @RequestParam(required = false) String search,
                                                                                   @RequestParam(defaultValue = "0") int page,
                                                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Certificates retrieved successfully.", service.listCertificates(currentUserEmail(), institutionId, eventId, certificateType, certificateStatus, search, page, size));
    }

    @GetMapping("/certificates/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CertificateSummaryResponse>> listMyCertificates() {
        return ApiResponseFactory.success("Your certificates were retrieved successfully.", service.listMyCertificates(currentUserEmail()));
    }

    @GetMapping("/certificate-templates")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<PageResponse<CertificateTemplateSummaryResponse>> listTemplates(@RequestParam(required = false) Long institutionId,
                                                                                       @RequestParam(required = false) CertificateType certificateType,
                                                                                       @RequestParam(required = false) Boolean active,
                                                                                       @RequestParam(required = false) String search,
                                                                                       @RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Certificate templates retrieved successfully.", service.listTemplates(currentUserEmail(), institutionId, certificateType, active, search, page, size));
    }

    @GetMapping("/certificate-templates/{templateId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateTemplateResponse> getTemplate(@PathVariable Long templateId) {
        return ApiResponseFactory.success("Certificate template retrieved successfully.", service.getTemplate(currentUserEmail(), templateId));
    }

    @GetMapping("/certificate-templates/{templateId}/preview")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateTemplatePreviewResponse> previewTemplate(@PathVariable Long templateId) {
        return ApiResponseFactory.success("Certificate template preview retrieved successfully.", service.previewTemplate(currentUserEmail(), templateId));
    }

    @PostMapping("/certificate-templates")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateTemplateResponse> createTemplate(@Valid @RequestBody CertificateTemplateRequest request) {
        return ApiResponseFactory.success("Certificate template created successfully.", service.createTemplate(currentUserEmail(), request));
    }

    @PutMapping("/certificate-templates/{templateId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateTemplateResponse> updateTemplate(@PathVariable Long templateId, @Valid @RequestBody CertificateTemplateRequest request) {
        return ApiResponseFactory.success("Certificate template updated successfully.", service.updateTemplate(currentUserEmail(), templateId, request));
    }

    @PostMapping("/certificate-templates/{templateId}/duplicate")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateTemplateResponse> duplicateTemplate(@PathVariable Long templateId, @Valid @RequestBody CertificateTemplateRequest request) {
        return ApiResponseFactory.success("Certificate template duplicated successfully.", service.duplicateTemplate(currentUserEmail(), templateId, request));
    }

    @PatchMapping("/certificate-templates/{templateId}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateTemplateResponse> toggleTemplateStatus(@PathVariable Long templateId, @RequestParam boolean active) {
        return ApiResponseFactory.success("Certificate template status updated successfully.", service.toggleTemplateStatus(currentUserEmail(), templateId, active));
    }

    @DeleteMapping("/certificate-templates/{templateId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long templateId) {
        service.deleteTemplate(currentUserEmail(), templateId);
        return ApiResponseFactory.success("Certificate template deleted successfully.", null);
    }

    @PostMapping("/certificates/preview")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateResponse> previewCertificate(@Valid @RequestBody CertificateIssueRequest request) {
        return ApiResponseFactory.success("Certificate preview prepared successfully.", service.previewCertificate(currentUserEmail(), request));
    }

    @PostMapping("/certificates")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateResponse> issueCertificate(@Valid @RequestBody CertificateIssueRequest request) {
        return ApiResponseFactory.success("Certificate issued successfully.", service.issueCertificate(currentUserEmail(), request));
    }

    @PostMapping("/certificates/bulk")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<List<CertificateResponse>> issueBulkCertificates(@Valid @RequestBody CertificateBulkIssueRequest request) {
        return ApiResponseFactory.success("Certificates issued successfully.", service.issueCertificatesBulk(currentUserEmail(), request));
    }

    @GetMapping("/certificates/{certificateId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CertificateResponse> getCertificate(@PathVariable Long certificateId) {
        return ApiResponseFactory.success("Certificate retrieved successfully.", service.getCertificate(currentUserEmail(), certificateId));
    }

    @PostMapping("/certificates/{certificateId}/regenerate")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateResponse> regenerateCertificate(@PathVariable Long certificateId) {
        return ApiResponseFactory.success("Certificate regenerated successfully.", service.regenerateCertificate(currentUserEmail(), certificateId));
    }

    @PostMapping("/certificates/{certificateId}/revoke")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateResponse> revokeCertificate(@PathVariable Long certificateId, @Valid @RequestBody CertificateRevokeRequest request) {
        return ApiResponseFactory.success("Certificate revoked successfully.", service.revokeCertificate(currentUserEmail(), certificateId, request));
    }

    @GetMapping("/certificates/{certificateId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long certificateId) {
        byte[] pdf = service.downloadCertificate(currentUserEmail(), certificateId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate-" + certificateId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/certificates/settings")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<CertificateSettingsResponse> settings(@RequestParam(required = false) Long institutionId) {
        return ApiResponseFactory.success("Certificate settings retrieved successfully.", service.getSettings(currentUserEmail(), institutionId));
    }

    @GetMapping("/certificates/verify/{token}")
    public ApiResponse<CertificateVerificationResponse> verify(@PathVariable String token, HttpServletRequest request) {
        return ApiResponseFactory.success("Certificate verification completed.", service.verifyToken(token, request));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
