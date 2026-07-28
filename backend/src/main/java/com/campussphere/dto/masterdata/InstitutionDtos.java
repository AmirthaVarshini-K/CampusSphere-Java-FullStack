package com.campussphere.dto.masterdata;

import com.campussphere.entity.InstitutionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class InstitutionDtos {
    private InstitutionDtos() {
    }

    public record InstitutionRequest(
            @NotBlank @Size(max = 40) String institutionCode,
            @NotBlank @Size(max = 160) String institutionName,
            @Size(max = 80) String shortName,
            @NotNull InstitutionType institutionType,
            @Size(max = 160) String affiliation,
            @Size(max = 160) String accreditation,
            @Email @Size(max = 160) String email,
            @Size(max = 24) String phone,
            @Size(max = 255) String website,
            @Size(max = 255) String addressLine1,
            @Size(max = 255) String addressLine2,
            @Size(max = 100) String city,
            @Size(max = 100) String state,
            @Size(max = 100) String country,
            @Size(max = 20) String postalCode,
            @Size(max = 512) String logoUrl,
            @Size(max = 64) String timezone
    ) {}

    public record InstitutionResponse(
            Long id,
            String institutionCode,
            String institutionName,
            String shortName,
            InstitutionType institutionType,
            String affiliation,
            String accreditation,
            String email,
            String phone,
            String website,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String country,
            String postalCode,
            String logoUrl,
            String timezone,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record InstitutionSummaryResponse(Long id, String institutionCode, String institutionName, boolean active) {}

    public record InstitutionOptionResponse(Long id, String label, String institutionCode, boolean active) {}
}
