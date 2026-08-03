package com.campussphere.dto.event;

import com.campussphere.entity.event.EligibilityRuleType;
import com.campussphere.entity.event.ParticipantType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class EventEligibilityRuleDtos {
    private EventEligibilityRuleDtos() {
    }

    public record EventEligibilityRuleRequest(
            @NotNull Long eventId,
            Long departmentId,
            Long programmeId,
            Long sectionId,
            @NotNull ParticipantType participantType,
            @NotNull EligibilityRuleType ruleType,
            @Min(1) Integer minimumYear,
            @Min(1) Integer maximumYear
    ) {}

    public record EventEligibilityRuleResponse(
            Long id,
            Long eventId,
            Long departmentId,
            String departmentName,
            Long programmeId,
            String programmeName,
            Long sectionId,
            String sectionName,
            ParticipantType participantType,
            EligibilityRuleType ruleType,
            Integer minimumYear,
            Integer maximumYear,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
