package com.campussphere.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;

public final class EventSessionDtos {
    private EventSessionDtos() {
    }

    public record EventSessionRequest(
            @NotNull Long eventId,
            @NotBlank @Size(max = 180) String title,
            @Size(max = 1000) String description,
            LocalDateTime sessionStart,
            LocalDateTime sessionEnd,
            Long venueId,
            @Size(max = 160) String speakerName,
            @Min(1) int sequenceNumber
    ) {}

    public record EventSessionResponse(
            Long id,
            Long eventId,
            String eventCode,
            String title,
            String description,
            LocalDateTime sessionStart,
            LocalDateTime sessionEnd,
            Long venueId,
            String venueName,
            String speakerName,
            int sequenceNumber,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
