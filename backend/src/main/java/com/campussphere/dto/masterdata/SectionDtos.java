package com.campussphere.dto.masterdata;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class SectionDtos {
    private SectionDtos() {
    }

    public record SectionRequest(
            @NotNull Long institutionId,
            @NotNull Long departmentId,
            @NotNull Long programmeId,
            @NotNull Long academicYearId,
            @NotNull Long semesterId,
            @NotBlank @Size(max = 80) String sectionName,
            @Min(1) Integer capacity,
            Integer studyYear,
            Long advisorUserId
    ) {}

    public record SectionResponse(
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
            Long semesterId,
            int semesterNumber,
            String semesterDisplayName,
            String sectionName,
            Integer capacity,
            Integer studyYear,
            Long advisorUserId,
            String advisorName,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record SectionOptionResponse(Long id, String label, boolean active) {}
}
