package com.campussphere.dto.attendance;

import com.campussphere.dto.PageResponse;
import com.campussphere.entity.attendance.AttendanceActionType;
import com.campussphere.entity.attendance.AttendanceMethod;
import com.campussphere.entity.attendance.AttendanceSessionStatus;
import com.campussphere.entity.registration.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class AttendanceDtos {
    private AttendanceDtos() {
    }

    public record AttendanceSessionRequest(
            @NotNull Long eventId,
            Long eventSessionId,
            @NotBlank @Size(max = 180) String sessionTitle,
            @Size(max = 500) String remarks
    ) {
    }

    public record AttendanceSessionResponse(
            Long id,
            Long institutionId,
            Long eventId,
            String eventTitle,
            Long eventSessionId,
            String eventSessionTitle,
            String sessionTitle,
            AttendanceSessionStatus status,
            LocalDateTime openedAt,
            LocalDateTime closedAt,
            String openedBy,
            String closedBy,
            String remarks,
            long recordCount,
            long presentCount,
            long absentCount,
            long lateCount,
            long excusedCount,
            boolean readyForCertificate,
            int attendanceThreshold,
            int completionPercentage
    ) {
    }

    public record QRTokenRequest(
            @NotNull Long registrationId,
            Integer expiresInMinutes,
            Boolean oneTimeUse
    ) {
    }

    public record QRTokenResponse(
            Long id,
            Long registrationId,
            Long eventId,
            String eventTitle,
            String token,
            String tokenPrefix,
            LocalDateTime expiresAt,
            LocalDateTime usedAt,
            LocalDateTime invalidatedAt,
            boolean oneTimeUse
    ) {
    }

    public record QRValidationRequest(
            @NotBlank String token,
            Long attendanceSessionId,
            Long eventId
    ) {
    }

    public record QRValidationResponse(
            boolean valid,
            String message,
            Long registrationId,
            Long eventId,
            String eventTitle,
            String participantName,
            Long attendanceSessionId,
            boolean expired,
            boolean used,
            boolean alreadyMarked
    ) {
    }

    public record AttendanceCheckInRequest(
            @NotBlank String token,
            @NotNull Long attendanceSessionId,
            @NotNull AttendanceStatus attendanceStatus,
            @NotNull AttendanceMethod attendanceMethod,
            @Size(max = 500) String remarks,
            @Size(max = 255) String deviceInfo,
            @Size(max = 80) String ipAddress
    ) {
    }

    public record ManualAttendanceRequest(
            @NotNull Long attendanceSessionId,
            @NotNull Long registrationId,
            @NotNull AttendanceStatus attendanceStatus,
            @Size(max = 500) String remarks,
            @Size(max = 255) String deviceInfo,
            @Size(max = 80) String ipAddress
    ) {
    }

    public record AttendanceBulkRequest(
            @NotNull Long attendanceSessionId,
            @NotNull AttendanceStatus attendanceStatus,
            @Size(min = 1) List<@Positive Long> registrationIds,
            @Size(max = 500) String remarks,
            @Size(max = 255) String deviceInfo,
            @Size(max = 80) String ipAddress
    ) {
    }

    public record AttendanceRecordResponse(
            Long id,
            Long institutionId,
            Long eventId,
            String eventTitle,
            Long attendanceSessionId,
            String attendanceSessionTitle,
            Long eventSessionId,
            String eventSessionTitle,
            Long registrationId,
            String registrationNumber,
            Long participantId,
            String participantName,
            AttendanceStatus attendanceStatus,
            AttendanceMethod attendanceMethod,
            LocalDateTime checkInTime,
            String checkedInBy,
            Long qrTokenId,
            String deviceInfo,
            String ipAddress,
            String remarks,
            boolean certificateEligible,
            int certificateThreshold,
            int completionPercentage,
            boolean certificateReady
    ) {
    }

    public record AttendanceAuditResponse(
            Long id,
            Long recordId,
            AttendanceActionType actionType,
            AttendanceStatus previousStatus,
            AttendanceStatus newStatus,
            String actorName,
            LocalDateTime occurredAt,
            String reason,
            String details
    ) {
    }

    public record AttendanceDashboardResponse(
            long totalParticipants,
            long present,
            long absent,
            long late,
            long excused,
            long attendancePercentage,
            long liveCheckIns,
            long upcomingSessions,
            long certificateEligibleCount,
            long certificateReadyCount,
            int certificateThreshold,
            List<AttendanceSessionResponse> upcomingSessionItems,
            List<AttendanceRecordResponse> recentScans,
            List<AttendanceRecordResponse> liveCheckInItems
    ) {
    }

    public record AttendanceHistoryResponse(
            PageResponse<AttendanceRecordResponse> page,
            List<AttendanceAuditResponse> audits
    ) {
    }

    public record AttendanceReportResponse(
            String eventTitle,
            Long eventId,
            Long attendanceSessionId,
            String attendanceSessionTitle,
            long totalRecords,
            long present,
            long absent,
            long late,
            long excused,
            long attendancePercentage,
            List<AttendanceRecordResponse> rows
    ) {
    }

    public record AttendanceOperationResponse(
            String message,
            AttendanceRecordResponse record,
            AttendanceSessionResponse attendanceSession,
            QRTokenResponse qrToken,
            List<AttendanceRecordResponse> bulkRecords
    ) {
    }
}
