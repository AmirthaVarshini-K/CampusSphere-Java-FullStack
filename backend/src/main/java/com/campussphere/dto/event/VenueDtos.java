package com.campussphere.dto.event;

import com.campussphere.entity.event.VenueType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class VenueDtos {
    private VenueDtos() {
    }

    public record VenueRequest(
            @NotNull Long institutionId,
            @NotBlank @Size(max = 40) String venueCode,
            @NotBlank @Size(max = 160) String venueName,
            @Size(max = 120) String building,
            @Size(max = 40) String floor,
            @Size(max = 40) String roomNumber,
            @Size(max = 255) String address,
            @Min(0) Integer capacity,
            @NotNull VenueType venueType
    ) {}

    public record VenueResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            String venueCode,
            String venueName,
            String building,
            String floor,
            String roomNumber,
            String address,
            Integer capacity,
            VenueType venueType,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record VenueSummary(Long id, String venueCode, String venueName, VenueType venueType, Integer capacity, boolean active) {}
}
