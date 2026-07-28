package com.campussphere.dto.masterdata;

import com.campussphere.entity.ProgrammeLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ProgrammeDtos {
    private ProgrammeDtos() {
    }

    public record ProgrammeRequest(
            @NotNull Long institutionId,
            @NotBlank @Size(max = 40) String programmeCode,
            @NotBlank @Size(max = 160) String programmeName,
            @NotNull ProgrammeLevel programmeLevel,
            @Min(1) int durationYears,
            @Min(1) int durationSemesters
    ) {}

    public record ProgrammeResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            String institutionName,
            String programmeCode,
            String programmeName,
            ProgrammeLevel programmeLevel,
            int durationYears,
            int durationSemesters,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record ProgrammeOptionResponse(Long id, String label, String programmeCode, boolean active) {}
}
