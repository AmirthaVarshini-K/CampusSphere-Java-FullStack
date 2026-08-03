package com.campussphere.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class EventCategoryDtos {
    private EventCategoryDtos() {
    }

    public record EventCategoryRequest(
            @NotNull Long institutionId,
            @NotBlank @Size(max = 40) String categoryCode,
            @NotBlank @Size(max = 160) String categoryName,
            @Size(max = 255) String description
    ) {}

    public record EventCategoryResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            String categoryCode,
            String categoryName,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record EventCategorySummary(Long id, String categoryCode, String categoryName, boolean active) {}
}
