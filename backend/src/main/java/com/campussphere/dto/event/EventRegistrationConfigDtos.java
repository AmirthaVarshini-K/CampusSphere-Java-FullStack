package com.campussphere.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;

public final class EventRegistrationConfigDtos {
    private EventRegistrationConfigDtos() {
    }

    public record EventRegistrationConfigRequest(
            @NotNull Long eventId,
            boolean registrationRequired,
            boolean approvalRequired,
            boolean waitlistEnabled,
            boolean teamEvent,
            Integer minimumTeamSize,
            Integer maximumTeamSize,
            boolean allowExternalParticipants,
            boolean allowMultipleRegistrations,
            boolean certificateEnabled,
            boolean attendanceRequiredForCertificate,
            boolean cancellationAllowed,
            LocalDateTime cancellationDeadline
    ) {}

    public record EventRegistrationConfigResponse(
            Long id,
            Long eventId,
            boolean registrationRequired,
            boolean approvalRequired,
            boolean waitlistEnabled,
            boolean teamEvent,
            Integer minimumTeamSize,
            Integer maximumTeamSize,
            boolean allowExternalParticipants,
            boolean allowMultipleRegistrations,
            boolean certificateEnabled,
            boolean attendanceRequiredForCertificate,
            boolean cancellationAllowed,
            LocalDateTime cancellationDeadline,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
