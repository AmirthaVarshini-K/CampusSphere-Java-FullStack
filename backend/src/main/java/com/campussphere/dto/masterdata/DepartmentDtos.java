package com.campussphere.dto.masterdata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class DepartmentDtos {
    private DepartmentDtos() {
    }

    public record DepartmentRequest(
            @NotNull Long institutionId,
            @NotBlank @Size(max = 40) String departmentCode,
            @NotBlank @Size(max = 160) String departmentName,
            @Size(max = 80) String shortName,
            @Size(max = 255) String description,
            @Size(max = 160) String departmentEmail,
            @Size(max = 24) String departmentPhone,
            Long headOfDepartmentUserId
    ) {}

    public record DepartmentResponse(
            Long id,
            Long institutionId,
            String institutionCode,
            String institutionName,
            String departmentCode,
            String departmentName,
            String shortName,
            String description,
            String departmentEmail,
            String departmentPhone,
            Long headOfDepartmentUserId,
            String headOfDepartmentName,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record DepartmentSummaryResponse(Long id, String departmentCode, String departmentName, boolean active) {}

    public record DepartmentOptionResponse(Long id, String label, String departmentCode, boolean active) {}
}
