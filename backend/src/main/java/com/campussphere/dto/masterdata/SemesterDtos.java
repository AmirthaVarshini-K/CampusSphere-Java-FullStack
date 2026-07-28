package com.campussphere.dto.masterdata;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class SemesterDtos {
    private SemesterDtos() {
    }

    public record SemesterRequest(
            @NotNull Long institutionId,
            @NotNull Long programmeId,
            @Min(1) int semesterNumber,
            @NotBlank @Size(max = 80) String displayName
    ) {}

    public record SemesterResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            Long programmeId,
            String programmeCode,
            String programmeName,
            int semesterNumber,
            String displayName,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record SemesterOptionResponse(Long id, String label, int semesterNumber, boolean active) {}
}
