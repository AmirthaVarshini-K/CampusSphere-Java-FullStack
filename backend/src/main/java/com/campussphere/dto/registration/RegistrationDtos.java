package com.campussphere.dto.registration;

import com.campussphere.entity.registration.AttendanceStatus;
import com.campussphere.entity.registration.InvitationStatus;
import com.campussphere.entity.registration.NotificationType;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.entity.registration.RegistrationType;
import com.campussphere.entity.registration.TeamMemberRole;
import com.campussphere.entity.registration.TeamStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public final class RegistrationDtos {
    private RegistrationDtos() {
    }

    public record RegistrationRequest(
            @NotNull RegistrationType registrationType,
            @Size(max = 160) String teamName,
            @Size(max = 40) String teamCode,
            @Size(max = 500) String remarks
    ) {}

    public record RegistrationDecisionRequest(
            @NotNull RegistrationStatus status,
            @Size(max = 500) String rejectionReason,
            @Size(max = 500) String remarks
    ) {}

    public record RegistrationSummaryResponse(
            Long id,
            Long eventId,
            String eventCode,
            String eventTitle,
            Long participantId,
            String participantName,
            String registrationNumber,
            RegistrationType registrationType,
            RegistrationStatus status,
            LocalDateTime registrationDate,
            LocalDateTime approvedAt,
            String approvedByName,
            AttendanceStatus attendanceStatus,
            boolean certificateEligible,
            Integer waitlistPosition,
            Long teamId,
            String teamName,
            String remarks
    ) {}

    public record RegistrationContextResponse(
            Long eventId,
            String eventTitle,
            String eventCode,
            String eventStatus,
            String mode,
            String visibility,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            LocalDateTime registrationEndDateTime,
            boolean registrationOpen,
            boolean waitlistEnabled,
            boolean approvalRequired,
            boolean teamEvent,
            Integer minimumTeamSize,
            Integer maximumTeamSize,
            Integer seatsRemaining,
            boolean canRegister,
            String message
    ) {}

    public record RegistrationDashboardResponse(
            long total,
            long approved,
            long pending,
            long rejected,
            long waitlisted,
            long cancelled,
            List<RegistrationSummaryResponse> recentRegistrations,
            List<RegistrationSummaryResponse> upcomingRegistrations,
            List<RegistrationSummaryResponse> pendingApprovals
    ) {}

    public record TeamRequest(
            @NotBlank @Size(max = 160) String teamName,
            @NotBlank @Size(max = 40) String teamCode
    ) {}

    public record TeamResponse(
            Long id,
            Long eventId,
            String eventTitle,
            String teamName,
            String teamCode,
            Long leaderId,
            String leaderName,
            TeamStatus status,
            int memberCount,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record TeamMemberResponse(
            Long id,
            Long teamId,
            Long userId,
            String userName,
            String email,
            TeamMemberRole role,
            LocalDateTime joinedAt,
            String status
    ) {}

    public record TeamInvitationRequest(
            @NotNull Long invitedUserId,
            @Size(max = 300) String message
    ) {}

    public record TeamInvitationResponse(
            Long id,
            Long teamId,
            String teamName,
            Long invitedUserId,
            String invitedUserName,
            InvitationStatus status,
            LocalDateTime invitedAt,
            LocalDateTime respondedAt,
            String message
    ) {}

    public record TeamTransferRequest(@NotNull Long newLeaderUserId) {}

    public record NotificationResponse(
            Long id,
            Long recipientUserId,
            NotificationType notificationType,
            String title,
            String message,
            String relatedEntityType,
            Long relatedEntityId,
            LocalDateTime readAt,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record EventRegistrationFormResponse(
            RegistrationContextResponse context,
            List<TeamResponse> teams,
            List<TeamInvitationResponse> invitations,
            List<RegistrationSummaryResponse> registrations,
            List<NotificationResponse> notifications
    ) {}
}
