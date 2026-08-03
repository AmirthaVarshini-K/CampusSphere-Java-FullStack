package com.campussphere.dto.event;

import com.campussphere.entity.event.EventMode;
import com.campussphere.entity.event.EventVisibility;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public final class EventDtos {
    private EventDtos() {
    }

    public record EventRequest(
            @NotNull Long institutionId,
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 40) String eventCode,
            @Size(max = 200) String slug,
            @Size(max = 500) String shortDescription,
            @Size(max = 4000) String fullDescription,
            @NotNull Long eventCategoryId,
            @NotNull Long eventTypeId,
            Long organizingDepartmentId,
            Long academicYearId,
            Long venueId,
            @NotNull EventMode mode,
            @NotNull EventVisibility visibility,
            @FutureOrPresent LocalDateTime startDateTime,
            @FutureOrPresent LocalDateTime endDateTime,
            LocalDateTime registrationStartDateTime,
            LocalDateTime registrationEndDateTime,
            LocalDateTime cancellationDeadline,
            @Size(max = 512) String onlineMeetingUrl,
            @Min(1) Integer maximumParticipants,
            @Min(1) Integer minimumParticipants,
            BigDecimal registrationFee,
            @Size(max = 12) String currency,
            @Size(max = 512) String bannerImageUrl,
            @Email @Size(max = 160) String contactEmail,
            @Size(max = 24) String contactPhone
    ) {}

    public record EventResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            String title,
            String eventCode,
            String slug,
            String shortDescription,
            String fullDescription,
            Long eventCategoryId,
            String eventCategoryCode,
            String eventCategoryName,
            Long eventTypeId,
            String eventTypeCode,
            String eventTypeName,
            Long organizingDepartmentId,
            String organizingDepartmentName,
            Long academicYearId,
            String academicYearLabel,
            Long venueId,
            String venueName,
            EventMode mode,
            EventVisibility visibility,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            LocalDateTime registrationStartDateTime,
            LocalDateTime registrationEndDateTime,
            LocalDateTime cancellationDeadline,
            String onlineMeetingUrl,
            Integer maximumParticipants,
            Integer minimumParticipants,
            BigDecimal registrationFee,
            String currency,
            String bannerImageUrl,
            String contactEmail,
            String contactPhone,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record EventSummaryResponse(
            Long id,
            String eventCode,
            String title,
            String status,
            EventMode mode,
            String eventCategoryName,
            String eventTypeName,
            String departmentName,
            String venueName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Integer maximumParticipants
    ) {}

    public record EventStatusRequest(@NotBlank String status) {}

    public record EventOverviewResponse(
            Long eventId,
            String title,
            String eventCode,
            String status,
            String mode,
            boolean publicationReady,
            int coordinatorCount,
            int sessionCount,
            int eligibilityRuleCount,
            boolean registrationConfigReady,
            String nextAction
    ) {}
}
