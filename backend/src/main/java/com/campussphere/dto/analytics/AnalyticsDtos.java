package com.campussphere.dto.analytics;

import com.campussphere.dto.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public final class AnalyticsDtos {
    private AnalyticsDtos() {
    }

    public record AnalyticsMetricResponse(
            String key,
            String label,
            long value,
            String detail,
            String tone,
            String deltaLabel
    ) {
    }

    public record AnalyticsTrendPointResponse(
            String label,
            long value
    ) {
    }

    public record AnalyticsDistributionPointResponse(
            String label,
            long value,
            String tone
    ) {
    }

    public record AnalyticsInsightResponse(
            String title,
            String description,
            String tone,
            String targetRoute
    ) {
    }

    public record AnalyticsActivityResponse(
            Long id,
            String title,
            String description,
            String category,
            Instant occurredAt,
            String tone,
            String targetRoute
    ) {
    }

    public record AnalyticsEventRowResponse(
            Long eventId,
            String eventCode,
            String eventTitle,
            String categoryName,
            String typeName,
            String departmentName,
            String status,
            String mode,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Integer capacity,
            long registrations,
            long confirmedRegistrations,
            long waitlistedRegistrations,
            long cancelledRegistrations,
            long attendanceCount,
            long presentCount,
            long certificateCount,
            int capacityUtilization,
            String health,
            String targetRoute
    ) {
    }

    public record AnalyticsRegistrationRowResponse(
            Long registrationId,
            Long eventId,
            String eventTitle,
            Long participantId,
            String participantName,
            String registrationNumber,
            String registrationType,
            String status,
            LocalDateTime registrationDate,
            String departmentName,
            String academicYear,
            String section,
            Integer waitlistPosition,
            String targetRoute
    ) {
    }

    public record AnalyticsAttendanceRowResponse(
            Long recordId,
            Long eventId,
            String eventTitle,
            Long attendanceSessionId,
            String attendanceSessionTitle,
            Long registrationId,
            String registrationNumber,
            String participantName,
            String attendanceStatus,
            String attendanceMethod,
            LocalDateTime checkInTime,
            String checkedInBy,
            String targetRoute
    ) {
    }

    public record AnalyticsCertificateRowResponse(
            Long certificateId,
            Long eventId,
            String eventTitle,
            Long recipientUserId,
            String recipientName,
            String certificateNumber,
            String certificateType,
            String certificateStatus,
            String verificationStatus,
            boolean revoked,
            Instant generatedAt,
            Integer attendancePercentage,
            String targetRoute
    ) {
    }

    public record AnalyticsDepartmentRowResponse(
            Long departmentId,
            String departmentCode,
            String departmentName,
            long eventsOrganized,
            long registrations,
            long uniqueParticipants,
            long attendanceCount,
            int attendanceRate,
            long certificatesIssued,
            String targetRoute
    ) {
    }

    public record AnalyticsOverviewResponse(
            String roleCode,
            String roleLabel,
            String scopeLabel,
            Instant generatedAt,
            List<AnalyticsMetricResponse> metrics,
            List<AnalyticsTrendPointResponse> trend,
            List<AnalyticsDistributionPointResponse> distribution,
            List<AnalyticsEventRowResponse> topItems,
            List<AnalyticsInsightResponse> insights,
            List<AnalyticsActivityResponse> activity,
            String emptyStateMessage
    ) {
    }

    public record AnalyticsReportResponse<T>(
            PageResponse<T> page,
            List<AnalyticsMetricResponse> metrics,
            List<AnalyticsTrendPointResponse> trend,
            List<AnalyticsDistributionPointResponse> distribution,
            List<AnalyticsInsightResponse> insights,
            List<AnalyticsActivityResponse> activity,
            String emptyStateMessage
    ) {
    }

    public record AnalyticsQuery(
            Long institutionId,
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
            @Min(0) int page,
            @Min(1) @Max(100) int size
    ) {
    }

    public record AnalyticsExportResponse(
            String fileName,
            String content
    ) {
    }
}
