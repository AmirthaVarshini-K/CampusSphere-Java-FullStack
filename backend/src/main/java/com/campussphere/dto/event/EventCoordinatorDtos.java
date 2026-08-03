package com.campussphere.dto.event;

import com.campussphere.entity.event.CoordinatorRole;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class EventCoordinatorDtos {
    private EventCoordinatorDtos() {
    }

    public record EventCoordinatorRequest(
            @NotNull Long eventId,
            @NotNull Long userId,
            @NotNull CoordinatorRole coordinatorRole,
            boolean primaryCoordinator
    ) {}

    public record EventCoordinatorResponse(
            Long id,
            Long eventId,
            Long userId,
            String userName,
            String userEmail,
            CoordinatorRole coordinatorRole,
            boolean primaryCoordinator,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
