package com.campussphere.dto.masterdata;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class DepartmentProgrammeDtos {
    private DepartmentProgrammeDtos() {
    }

    public record DepartmentProgrammeRequest(
            @NotNull Long institutionId,
            @NotNull Long departmentId,
            @NotNull Long programmeId,
            Long academicYearId,
            @Min(1) Integer intakeCapacity
    ) {}

    public record DepartmentProgrammeResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            Long departmentId,
            String departmentCode,
            String departmentName,
            Long programmeId,
            String programmeCode,
            String programmeName,
            Long academicYearId,
            String academicYearLabel,
            Integer intakeCapacity,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record DepartmentProgrammeOptionResponse(Long id, String label, boolean active) {}
}
