package com.campussphere.controller;

import com.campussphere.dto.ApiResponse;
import com.campussphere.dto.PageResponse;
import com.campussphere.dto.registration.RegistrationDtos.EventRegistrationFormResponse;
import com.campussphere.dto.registration.RegistrationDtos.NotificationResponse;
import com.campussphere.dto.registration.RegistrationDtos.RegistrationContextResponse;
import com.campussphere.dto.registration.RegistrationDtos.RegistrationDashboardResponse;
import com.campussphere.dto.registration.RegistrationDtos.RegistrationDecisionRequest;
import com.campussphere.dto.registration.RegistrationDtos.RegistrationRequest;
import com.campussphere.dto.registration.RegistrationDtos.RegistrationPreviewResponse;
import com.campussphere.dto.registration.RegistrationDtos.RegistrationSummaryResponse;
import com.campussphere.dto.registration.RegistrationDtos.TeamInvitationRequest;
import com.campussphere.dto.registration.RegistrationDtos.TeamInvitationResponse;
import com.campussphere.dto.registration.RegistrationDtos.TeamMemberResponse;
import com.campussphere.dto.registration.RegistrationDtos.TeamRequest;
import com.campussphere.dto.registration.RegistrationDtos.TeamResponse;
import com.campussphere.dto.registration.RegistrationDtos.TeamTransferRequest;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.service.RegistrationManagementService;
import com.campussphere.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationManagementService service;

    public RegistrationController(RegistrationManagementService service) {
        this.service = service;
    }

    @GetMapping("/registrations/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegistrationDashboardResponse> getDashboard() {
        return ApiResponseFactory.success("Registration dashboard retrieved successfully.", service.getDashboard(currentUserEmail()));
    }

    @GetMapping("/registrations")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<RegistrationSummaryResponse>> listRegistrations(@RequestParam(required = false) Long institutionId,
                                                                                    @RequestParam(required = false) Long eventId,
                                                                                    @RequestParam(required = false) String search,
                                                                                    @RequestParam(required = false) RegistrationStatus status,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Registrations retrieved successfully.", service.listRegistrations(currentUserEmail(), institutionId, eventId, search, status, page, size));
    }

    @GetMapping("/registrations/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<RegistrationSummaryResponse>> listMyRegistrations(@RequestParam(required = false) String search,
                                                                                      @RequestParam(required = false) RegistrationStatus status,
                                                                                      @RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Your registrations were retrieved successfully.", service.listMyRegistrations(currentUserEmail(), search, status, page, size));
    }

    @GetMapping("/registrations/waitlist")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<List<RegistrationSummaryResponse>> listWaitlist(@RequestParam(required = false) Long eventId) {
        return ApiResponseFactory.success("Waitlisted registrations retrieved successfully.", service.listWaitlist(currentUserEmail(), eventId));
    }

    @GetMapping("/registrations/me/waitlist")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RegistrationSummaryResponse>> listMyWaitlist() {
        return ApiResponseFactory.success("Your waitlisted registrations were retrieved successfully.", service.listMyWaitlist(currentUserEmail()));
    }

    @GetMapping("/events/{eventId}/registration-context")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegistrationContextResponse> getRegistrationContext(@PathVariable Long eventId) {
        return ApiResponseFactory.success("Registration context retrieved successfully.", service.getEventContext(currentUserEmail(), eventId));
    }

    @GetMapping("/events/{eventId}/registration-form")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EventRegistrationFormResponse> getRegistrationForm(@PathVariable Long eventId) {
        return ApiResponseFactory.success("Registration form retrieved successfully.", service.getEventRegistrationForm(currentUserEmail(), eventId));
    }

    @PostMapping("/events/{eventId}/registration-preview")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegistrationPreviewResponse> previewRegistration(@PathVariable Long eventId, @Valid @RequestBody RegistrationRequest request) {
        return ApiResponseFactory.success("Registration preview retrieved successfully.", service.previewRegistration(currentUserEmail(), eventId, request));
    }

    @PostMapping("/events/{eventId}/register")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> register(@PathVariable Long eventId, @Valid @RequestBody RegistrationRequest request) {
        return ApiResponseFactory.success("Registration submitted successfully.", service.register(currentUserEmail(), eventId, request));
    }

    @PostMapping("/events/{eventId}/teams")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> createTeam(@PathVariable Long eventId, @Valid @RequestBody TeamRequest request) {
        return ApiResponseFactory.success("Team created successfully.", service.createTeam(currentUserEmail(), eventId, request));
    }

    @GetMapping("/teams/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TeamResponse>> listMyTeams() {
        return ApiResponseFactory.success("Your teams were retrieved successfully.", service.listMyTeams(currentUserEmail()));
    }

    @GetMapping("/team-invitations/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TeamInvitationResponse>> listMyTeamInvitations() {
        return ApiResponseFactory.success("Your team invitations were retrieved successfully.", service.listMyInvitations(currentUserEmail()));
    }

    @GetMapping("/events/{eventId}/teams")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TeamResponse>> listTeams(@PathVariable Long eventId) {
        return ApiResponseFactory.success("Teams retrieved successfully.", service.listTeams(currentUserEmail(), eventId));
    }

    @GetMapping("/teams/{teamId}/invitations")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TeamInvitationResponse>> listTeamInvitations(@PathVariable Long teamId) {
        return ApiResponseFactory.success("Team invitations retrieved successfully.", service.listInvitations(currentUserEmail(), teamId));
    }

    @GetMapping("/teams/{teamId}/members")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TeamMemberResponse>> listTeamMembers(@PathVariable Long teamId) {
        return ApiResponseFactory.success("Team members retrieved successfully.", service.listTeamMembers(currentUserEmail(), teamId));
    }

    @PostMapping("/teams/{teamId}/invitations")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> inviteMember(@PathVariable Long teamId, @Valid @RequestBody TeamInvitationRequest request) {
        return ApiResponseFactory.success("Invitation sent successfully.", service.inviteMember(currentUserEmail(), teamId, request));
    }

    @PostMapping("/team-invitations/{invitationId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> acceptInvitation(@PathVariable Long invitationId) {
        return ApiResponseFactory.success("Invitation accepted successfully.", service.acceptInvitation(currentUserEmail(), invitationId));
    }

    @PostMapping("/team-invitations/{invitationId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> rejectInvitation(@PathVariable Long invitationId) {
        return ApiResponseFactory.success("Invitation rejected successfully.", service.rejectInvitation(currentUserEmail(), invitationId));
    }

    @PostMapping("/team-invitations/{invitationId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> cancelInvitation(@PathVariable Long invitationId) {
        return ApiResponseFactory.success("Invitation cancelled successfully.", service.cancelInvitation(currentUserEmail(), invitationId));
    }

    @PutMapping("/teams/{teamId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> updateTeam(@PathVariable Long teamId, @Valid @RequestBody TeamRequest request) {
        return ApiResponseFactory.success("Team updated successfully.", service.updateTeam(currentUserEmail(), teamId, request));
    }

    @PostMapping("/teams/{teamId}/members/{memberId}/remove")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> removeTeamMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        return ApiResponseFactory.success("Team member removed successfully.", service.removeTeamMember(currentUserEmail(), teamId, memberId));
    }

    @PutMapping("/teams/{teamId}/transfer")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> transferOwnership(@PathVariable Long teamId, @Valid @RequestBody TeamTransferRequest request) {
        return ApiResponseFactory.success("Team ownership transferred successfully.", service.transferTeamOwnership(currentUserEmail(), teamId, request));
    }

    @PostMapping("/teams/{teamId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> leaveTeam(@PathVariable Long teamId) {
        return ApiResponseFactory.success("You left the team successfully.", service.leaveTeam(currentUserEmail(), teamId));
    }

    @PostMapping("/teams/{teamId}/delete")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteTeam(@PathVariable Long teamId) {
        service.deleteTeam(currentUserEmail(), teamId);
        return ApiResponseFactory.success("Team deleted successfully.", null);
    }

    @PostMapping("/registrations/{registrationId}/decision")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<?> decideRegistration(@PathVariable Long registrationId, @Valid @RequestBody RegistrationDecisionRequest request) {
        return ApiResponseFactory.success("Registration updated successfully.", service.approveRegistration(currentUserEmail(), registrationId, request));
    }

    @PostMapping("/registrations/{registrationId}/promote")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<?> promoteWaitlistEntry(@PathVariable Long registrationId) {
        return ApiResponseFactory.success("Waitlist entry promoted successfully.", service.promoteWaitlistEntry(currentUserEmail(), registrationId));
    }

    @PostMapping("/registrations/{registrationId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> cancelRegistration(@PathVariable Long registrationId) {
        return ApiResponseFactory.success("Registration cancelled successfully.", service.cancelRegistration(currentUserEmail(), registrationId));
    }

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NotificationResponse>> listNotifications() {
        return ApiResponseFactory.success("Notifications retrieved successfully.", service.listNotifications(currentUserEmail()));
    }

    @GetMapping("/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> unreadNotificationCount() {
        return ApiResponseFactory.success("Unread notification count retrieved successfully.", service.getUnreadNotificationCount(currentUserEmail()));
    }

    @PostMapping("/notifications/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAllNotificationsRead() {
        service.markAllNotificationsRead(currentUserEmail());
        return ApiResponseFactory.success("All notifications marked as read.", null);
    }

    @PatchMapping("/notifications/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NotificationResponse> markNotificationRead(@PathVariable Long id) {
        return ApiResponseFactory.success("Notification marked as read.", service.markNotificationRead(currentUserEmail(), id));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
