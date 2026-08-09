package com.campussphere.controller;

import com.campussphere.dto.ApiResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsCertificateRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsDepartmentRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsEventRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsExportResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsOverviewResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsQuery;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsRegistrationRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsAttendanceRowResponse;
import com.campussphere.entity.RoleCode;
import com.campussphere.service.AnalyticsService;
import com.campussphere.util.ApiResponseFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@Validated
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AnalyticsOverviewResponse> overview(@RequestParam(required = false) Long institutionId) {
        return ApiResponseFactory.success("Analytics overview retrieved successfully.", analyticsService.getOverview(currentUserEmail(), institutionId));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AnalyticsOverviewResponse> myInsights() {
        return ApiResponseFactory.success("Your analytics insights were retrieved successfully.", analyticsService.getStudentInsights(currentUserEmail()));
    }

    @GetMapping("/coordinator")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AnalyticsOverviewResponse> coordinatorInsights() {
        return ApiResponseFactory.success("Coordinator analytics insights retrieved successfully.", analyticsService.getCoordinatorInsights(currentUserEmail()));
    }

    @GetMapping("/events")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsEventRowResponse>> events(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponseFactory.success("Event analytics retrieved successfully.", analyticsService.getEvents(currentUserEmail(), query(institutionId, eventId, categoryId, typeId, departmentId, programmeId, academicYearId, search, mode, status, attendanceStatus, certificateType, startDate, endDate, sort, direction, page, size)));
    }

    @GetMapping("/registrations")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsRegistrationRowResponse>> registrations(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponseFactory.success("Registration analytics retrieved successfully.", analyticsService.getRegistrations(currentUserEmail(), query(institutionId, eventId, categoryId, typeId, departmentId, programmeId, academicYearId, search, mode, status, attendanceStatus, certificateType, startDate, endDate, sort, direction, page, size)));
    }

    @GetMapping("/attendance")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsAttendanceRowResponse>> attendance(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponseFactory.success("Attendance analytics retrieved successfully.", analyticsService.getAttendance(currentUserEmail(), query(institutionId, eventId, categoryId, typeId, departmentId, programmeId, academicYearId, search, mode, status, attendanceStatus, certificateType, startDate, endDate, sort, direction, page, size)));
    }

    @GetMapping("/certificates")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsCertificateRowResponse>> certificates(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponseFactory.success("Certificate analytics retrieved successfully.", analyticsService.getCertificates(currentUserEmail(), query(institutionId, eventId, categoryId, typeId, departmentId, programmeId, academicYearId, search, mode, status, attendanceStatus, certificateType, startDate, endDate, sort, direction, page, size)));
    }

    @GetMapping("/departments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsDepartmentRowResponse>> departments(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponseFactory.success("Department analytics retrieved successfully.", analyticsService.getDepartments(currentUserEmail(), query(institutionId, eventId, categoryId, typeId, departmentId, programmeId, academicYearId, search, mode, status, attendanceStatus, certificateType, startDate, endDate, sort, direction, page, size)));
    }

    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> export(
            @RequestParam String reportType,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        AnalyticsExportResponse response = analyticsService.exportReport(currentUserEmail(), reportType, query(institutionId, eventId, categoryId, typeId, departmentId, programmeId, academicYearId, search, mode, status, attendanceStatus, certificateType, startDate, endDate, sort, direction, page, size));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(response.content().getBytes(StandardCharsets.UTF_8));
    }

    private AnalyticsQuery query(Long institutionId,
                                 Long eventId,
                                 Long categoryId,
                                 Long typeId,
                                 Long departmentId,
                                 Long programmeId,
                                 Long academicYearId,
                                 String search,
                                 String mode,
                                 String status,
                                 String attendanceStatus,
                                 String certificateType,
                                 String startDate,
                                 String endDate,
                                 String sort,
                                 String direction,
                                 int page,
                                 int size) {
        return new AnalyticsQuery(
                institutionId,
                eventId,
                categoryId,
                typeId,
                departmentId,
                programmeId,
                academicYearId,
                search,
                mode,
                status,
                attendanceStatus,
                certificateType,
                startDate,
                endDate,
                sort,
                direction,
                page,
                size
        );
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
