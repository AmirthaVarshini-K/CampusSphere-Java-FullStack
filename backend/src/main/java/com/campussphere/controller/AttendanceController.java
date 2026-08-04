package com.campussphere.controller;

import com.campussphere.dto.ApiResponse;
import com.campussphere.dto.PageResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceAuditResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceBulkRequest;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceCheckInRequest;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceDashboardResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceHistoryResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceOperationResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceRecordResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceReportResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceSessionRequest;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceSessionResponse;
import com.campussphere.dto.attendance.AttendanceDtos.ManualAttendanceRequest;
import com.campussphere.dto.attendance.AttendanceDtos.QRTokenRequest;
import com.campussphere.dto.attendance.AttendanceDtos.QRTokenResponse;
import com.campussphere.dto.attendance.AttendanceDtos.QRValidationRequest;
import com.campussphere.dto.attendance.AttendanceDtos.QRValidationResponse;
import com.campussphere.entity.registration.AttendanceStatus;
import com.campussphere.service.AttendanceManagementService;
import com.campussphere.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceManagementService service;

    public AttendanceController(AttendanceManagementService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AttendanceDashboardResponse> dashboard(@RequestParam(required = false) Long eventId) {
        return ApiResponseFactory.success("Attendance dashboard retrieved successfully.", service.getDashboard(currentUserEmail(), eventId));
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AttendanceSessionResponse>> listSessions(@RequestParam(required = false) Long eventId) {
        return ApiResponseFactory.success("Attendance sessions retrieved successfully.", service.listAttendanceSessions(currentUserEmail(), eventId));
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<AttendanceSessionResponse> openSession(@Valid @RequestBody AttendanceSessionRequest request) {
        return ApiResponseFactory.success("Attendance session opened successfully.", service.openAttendanceSession(currentUserEmail(), request));
    }

    @PatchMapping("/sessions/{sessionId}/close")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<AttendanceSessionResponse> closeSession(@PathVariable Long sessionId) {
        return ApiResponseFactory.success("Attendance session closed successfully.", service.closeAttendanceSession(currentUserEmail(), sessionId));
    }

    @PostMapping("/qr-tokens")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<QRTokenResponse> generateToken(@Valid @RequestBody QRTokenRequest request) {
        return ApiResponseFactory.success("QR token generated successfully.", service.generateQrToken(currentUserEmail(), request));
    }

    @GetMapping("/registrations/{registrationId}/qr-token")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<QRTokenResponse> getTokenForRegistration(@PathVariable Long registrationId) {
        return ApiResponseFactory.success("QR token retrieved successfully.", service.getTokenForRegistration(currentUserEmail(), registrationId));
    }

    @PostMapping("/qr-tokens/validate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<QRValidationResponse> validateToken(@Valid @RequestBody QRValidationRequest request) {
        return ApiResponseFactory.success("QR token validated successfully.", service.validateQrToken(currentUserEmail(), request));
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<AttendanceOperationResponse> checkIn(@Valid @RequestBody AttendanceCheckInRequest request) {
        return ApiResponseFactory.success("Attendance recorded successfully.", service.checkIn(currentUserEmail(), request));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<AttendanceOperationResponse> markManual(@Valid @RequestBody ManualAttendanceRequest request) {
        return ApiResponseFactory.success("Manual attendance recorded successfully.", service.markManualAttendance(currentUserEmail(), request));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<AttendanceOperationResponse> bulkAttendance(@Valid @RequestBody AttendanceBulkRequest request) {
        return ApiResponseFactory.success("Bulk attendance updated successfully.", service.bulkAttendance(currentUserEmail(), request));
    }

    @PatchMapping("/records/{recordId}/undo")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<AttendanceOperationResponse> undo(@PathVariable Long recordId, @RequestParam(required = false) String remarks) {
        return ApiResponseFactory.success("Attendance update reverted successfully.", service.undoAttendance(currentUserEmail(), recordId, remarks));
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<AttendanceRecordResponse>> history(@RequestParam(required = false) Long eventId,
                                                                       @RequestParam(required = false) Long attendanceSessionId,
                                                                       @RequestParam(required = false) Long registrationId,
                                                                       @RequestParam(required = false) Long participantId,
                                                                       @RequestParam(required = false) AttendanceStatus status,
                                                                       @RequestParam(required = false) String search,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Attendance history retrieved successfully.", service.listHistory(currentUserEmail(), eventId, attendanceSessionId, registrationId, participantId, status, search, page, size));
    }

    @GetMapping("/history/details")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AttendanceHistoryResponse> historyDetails(@RequestParam(required = false) Long eventId,
                                                                 @RequestParam(required = false) Long attendanceSessionId,
                                                                 @RequestParam(required = false) Long registrationId,
                                                                 @RequestParam(required = false) Long participantId,
                                                                 @RequestParam(required = false) AttendanceStatus status,
                                                                 @RequestParam(required = false) String search,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        PageResponse<AttendanceRecordResponse> history = service.listHistory(currentUserEmail(), eventId, attendanceSessionId, registrationId, participantId, status, search, page, size);
        return ApiResponseFactory.success("Attendance history retrieved successfully.", new AttendanceHistoryResponse(history, List.of()));
    }

    @GetMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AttendanceReportResponse> report(@RequestParam(required = false) Long eventId,
                                                        @RequestParam(required = false) Long attendanceSessionId) {
        return ApiResponseFactory.success("Attendance report retrieved successfully.", service.getReport(currentUserEmail(), eventId, attendanceSessionId));
    }

    @GetMapping("/reports/export")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ResponseEntity<byte[]> exportReport(@RequestParam(required = false) Long eventId,
                                               @RequestParam(required = false) Long attendanceSessionId,
                                               @RequestParam(defaultValue = "csv") String format) {
        AttendanceReportResponse report = service.getReport(currentUserEmail(), eventId, attendanceSessionId);
        byte[] content = buildCsv(report).getBytes(StandardCharsets.UTF_8);
        String extension = "xls".equalsIgnoreCase(format) ? "xls" : "csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=campussphere-attendance-report." + extension)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }

    @PatchMapping("/qr-tokens/{tokenId}/invalidate")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<Void> invalidate(@PathVariable Long tokenId) {
        service.invalidateToken(currentUserEmail(), tokenId);
        return ApiResponseFactory.success("QR token invalidated successfully.", null);
    }

    private String buildCsv(AttendanceReportResponse report) {
        StringBuilder builder = new StringBuilder();
        builder.append("eventTitle,eventId,attendanceSessionId,attendanceSessionTitle,registrationNumber,participantName,status,method,checkInTime,certificateEligible\n");
        for (AttendanceRecordResponse row : report.rows()) {
            builder.append(csv(report.eventTitle())).append(',')
                    .append(csv(String.valueOf(report.eventId()))).append(',')
                    .append(csv(String.valueOf(report.attendanceSessionId()))).append(',')
                    .append(csv(report.attendanceSessionTitle())).append(',')
                    .append(csv(row.registrationNumber())).append(',')
                    .append(csv(row.participantName())).append(',')
                    .append(csv(row.attendanceStatus() == null ? null : row.attendanceStatus().name())).append(',')
                    .append(csv(row.attendanceMethod() == null ? null : row.attendanceMethod().name())).append(',')
                    .append(csv(row.checkInTime() == null ? null : row.checkInTime().toString())).append(',')
                    .append(row.certificateEligible())
                    .append('\n');
        }
        return builder.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
