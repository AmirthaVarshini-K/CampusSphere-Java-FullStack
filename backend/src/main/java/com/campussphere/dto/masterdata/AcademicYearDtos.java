package com.campussphere.dto.masterdata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

public final class AcademicYearDtos {
    private AcademicYearDtos() {
    }

    public record AcademicYearRequest(
            @NotNull Long institutionId,
            @NotBlank @Size(max = 24) String yearLabel,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            boolean currentYear,
            boolean registrationOpen
    ) {}

    public record AcademicYearResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            String institutionName,
            String yearLabel,
            LocalDate startDate,
            LocalDate endDate,
            boolean currentYear,
            boolean registrationOpen,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record AcademicYearOptionResponse(Long id, String label, boolean currentYear, boolean active) {}
}
