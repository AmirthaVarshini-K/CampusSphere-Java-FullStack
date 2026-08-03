package com.campussphere.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class EventTypeDtos {
    private EventTypeDtos() {
    }

    public record EventTypeRequest(
            @NotNull Long institutionId,
            @NotBlank @Size(max = 40) String typeCode,
            @NotBlank @Size(max = 160) String typeName,
            @Size(max = 255) String description
    ) {}

    public record EventTypeResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            String typeCode,
            String typeName,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record EventTypeSummary(Long id, String typeCode, String typeName, boolean active) {}
}
