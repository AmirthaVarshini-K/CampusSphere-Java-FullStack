package com.campussphere.service;

import com.campussphere.dto.PageResponse;
import com.campussphere.dto.registration.RegistrationDtos.EventRegistrationFormResponse;
import com.campussphere.dto.registration.RegistrationDtos.NotificationResponse;
import com.campussphere.dto.registration.RegistrationDtos.RegistrationConflictResponse;
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
import com.campussphere.entity.Department;
import com.campussphere.entity.Institution;
import com.campussphere.entity.Programme;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.Section;
import com.campussphere.entity.User;
import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventEligibilityRule;
import com.campussphere.entity.event.EventMode;
import com.campussphere.entity.event.EventRegistrationConfig;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.registration.AttendanceStatus;
import com.campussphere.entity.registration.EventRegistration;
import com.campussphere.entity.registration.InAppNotification;
import com.campussphere.entity.registration.InvitationStatus;
import com.campussphere.entity.registration.NotificationType;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.entity.registration.RegistrationType;
import com.campussphere.entity.registration.Team;
import com.campussphere.entity.registration.TeamInvitation;
import com.campussphere.entity.registration.TeamMember;
import com.campussphere.entity.registration.TeamMemberRole;
import com.campussphere.entity.registration.TeamStatus;
import com.campussphere.exception.BusinessRuleViolationException;
import com.campussphere.exception.DuplicateResourceException;
import com.campussphere.exception.InvalidInstitutionRelationshipException;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.repository.EventEligibilityRuleRepository;
import com.campussphere.repository.EventRegistrationConfigRepository;
import com.campussphere.repository.EventRegistrationRepository;
import com.campussphere.repository.EventRepository;
import com.campussphere.repository.InAppNotificationRepository;
import com.campussphere.repository.SectionRepository;
import com.campussphere.repository.TeamInvitationRepository;
import com.campussphere.repository.TeamMemberRepository;
import com.campussphere.repository.TeamRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.service.support.CurrentUserContext;
import com.campussphere.service.support.InstitutionScopeResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class RegistrationManagementService {

    private final EventRepository eventRepository;
    private final EventRegistrationConfigRepository configRepository;
    private final EventRegistrationRepository registrationRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInvitationRepository invitationRepository;
    private final InAppNotificationRepository notificationRepository;
    private final EventEligibilityRuleRepository eligibilityRuleRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final CurrentUserContext currentUserContext;
    private final InstitutionScopeResolver scopeResolver;

    public RegistrationManagementService(
            EventRepository eventRepository,
            EventRegistrationConfigRepository configRepository,
            EventRegistrationRepository registrationRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            TeamInvitationRepository invitationRepository,
            InAppNotificationRepository notificationRepository,
            EventEligibilityRuleRepository eligibilityRuleRepository,
            UserRepository userRepository,
            SectionRepository sectionRepository,
            CurrentUserContext currentUserContext,
            InstitutionScopeResolver scopeResolver
    ) {
        this.eventRepository = eventRepository;
        this.configRepository = configRepository;
        this.registrationRepository = registrationRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.invitationRepository = invitationRepository;
        this.notificationRepository = notificationRepository;
        this.eligibilityRuleRepository = eligibilityRuleRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.currentUserContext = currentUserContext;
        this.scopeResolver = scopeResolver;
    }

    public EventRegistrationFormResponse getEventRegistrationForm(String email, Long eventId) {
        User user = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(user, event.getInstitution().getId());
        return new EventRegistrationFormResponse(
                buildContext(event),
                teamRepository.findByEvent_IdAndDeletedFalse(eventId).stream().map(this::toTeamResponse).toList(),
                invitationRepository.findByInvitedUser_IdAndDeletedFalseOrderByInvitedAtDesc(user.getId()).stream()
                        .filter(invitation -> Objects.equals(invitation.getTeam().getEvent().getId(), eventId))
                        .map(this::toInvitationResponse)
                        .toList(),
                registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId()).stream()
                        .filter(registration -> Objects.equals(registration.getEvent().getId(), eventId))
                        .map(this::toRegistrationSummary)
                        .toList(),
                notificationRepository.findByRecipient_IdAndDeletedFalseOrderByCreatedAtDesc(user.getId()).stream()
                        .limit(10)
                        .map(this::toNotificationResponse)
                        .toList()
        );
    }

    public RegistrationContextResponse getEventContext(String email, Long eventId) {
        User user = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(user, event.getInstitution().getId());
        return buildContext(event);
    }

    public RegistrationSummaryResponse register(String email, Long eventId, RegistrationRequest request) {
        User user = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(user, event.getInstitution().getId());
        EventRegistrationConfig config = getConfig(event);

        ensureRegistrationOpen(event, config);
        ensureNoConflict(user, event);
        ensureNoDuplicateRegistration(user, event);
        validateEligibility(user, event);

        Team team = null;
        if (request.registrationType() == RegistrationType.TEAM) {
            if (config == null || !config.isTeamEvent()) {
                throw new BusinessRuleViolationException("This event does not support team registrations.");
            }
            team = createTeamInternal(event, user, request.teamName(), request.teamCode());
            addTeamMember(team, user, TeamMemberRole.LEADER);
        }

        EventRegistration registration = createRegistration(event, user, request.registrationType(), team, request.remarks(), config);
        saveRegistration(registration);
        notifyForRegistration(registration);
        return toRegistrationSummary(registration);
    }

    @Transactional(readOnly = true)
    public RegistrationPreviewResponse previewRegistration(String email, Long eventId, RegistrationRequest request) {
        User user = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(user, event.getInstitution().getId());
        EventRegistrationConfig config = getConfig(event);
        boolean registrationOpen = isRegistrationOpen(event, config);
        boolean duplicate = registrationRepository.existsByEvent_IdAndParticipant_IdAndDeletedFalse(event.getId(), user.getId());
        List<RegistrationConflictResponse> conflicts = findConflicts(user, event);
        boolean waitlistEnabled = config != null && config.isWaitlistEnabled();
        boolean capacityAvailable = !isAtCapacity(event, null);
        RegistrationStatus expectedStatus = !registrationOpen
                ? null
                : duplicate || !conflicts.isEmpty()
                ? null
                : config != null && config.isApprovalRequired()
                ? RegistrationStatus.PENDING
                : capacityAvailable
                ? RegistrationStatus.APPROVED
                : waitlistEnabled
                ? RegistrationStatus.WAITLISTED
                : null;
        Integer waitlistPosition = expectedStatus == RegistrationStatus.WAITLISTED ? nextWaitlistPosition(event) : null;
        List<String> messages = new ArrayList<>();
        if (!registrationOpen) {
            messages.add("Registration is currently closed for this event.");
        }
        if (duplicate) {
            messages.add("You are already registered for this event.");
        }
        if (!conflicts.isEmpty()) {
            messages.add("You are already registered for another event during this time.");
        }
        if (expectedStatus == RegistrationStatus.WAITLISTED) {
            messages.add("The event is full, so the next registration will be placed on the waitlist.");
        } else if (expectedStatus == RegistrationStatus.APPROVED) {
            messages.add("A seat is available for immediate approval.");
        } else if (expectedStatus == RegistrationStatus.PENDING) {
            messages.add("The event requires approval before the registration is confirmed.");
        }
        return new RegistrationPreviewResponse(
                event.getId(),
                event.getTitle(),
                request.registrationType(),
                registrationOpen,
                duplicate,
                capacityAvailable,
                waitlistEnabled,
                expectedStatus,
                waitlistPosition,
                conflicts,
                messages
        );
    }

    public TeamResponse createTeam(String email, Long eventId, TeamRequest request) {
        User user = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(user, event.getInstitution().getId());
        EventRegistrationConfig config = getConfig(event);
        if (config == null || !config.isTeamEvent()) {
            throw new BusinessRuleViolationException("This event does not support teams.");
        }
        ensureRegistrationOpen(event, config);
        Team team = createTeamInternal(event, user, request.teamName(), request.teamCode());
        addTeamMember(team, user, TeamMemberRole.LEADER);
        return toTeamResponse(team);
    }

    public List<TeamResponse> listMyTeams(String email) {
        User user = requireCurrentUser(email);
        return teamMemberRepository.findByUser_IdAndDeletedFalseOrderByJoinedAtDesc(user.getId()).stream()
                .map(TeamMember::getTeam)
                .filter(team -> team != null && !team.isDeleted())
                .map(this::toTeamResponse)
                .toList();
    }

    public List<TeamInvitationResponse> listMyInvitations(String email) {
        User user = requireCurrentUser(email);
        return invitationRepository.findByInvitedUser_IdAndDeletedFalseOrderByInvitedAtDesc(user.getId()).stream()
                .map(this::toInvitationResponse)
                .toList();
    }

    public TeamInvitationResponse inviteMember(String email, Long teamId, TeamInvitationRequest request) {
        User user = requireCurrentUser(email);
        Team team = requireTeam(teamId);
        scopeResolver.resolveForWrite(user, team.getInstitution().getId());
        ensureTeamManager(user, team);
        User invited = requireUser(request.invitedUserId());
        ensureSameInstitution(team.getInstitution(), invited);
        if (teamMemberRepository.findByTeam_IdAndUser_IdAndDeletedFalse(team.getId(), invited.getId()).isPresent()) {
            throw new DuplicateResourceException("This user is already on the team.");
        }
        if (invitationRepository.existsByTeam_IdAndInvitedUser_IdAndDeletedFalse(team.getId(), invited.getId())) {
            throw new DuplicateResourceException("An invitation already exists for this user.");
        }
        TeamInvitation invitation = new TeamInvitation();
        invitation.setTeam(team);
        invitation.setInvitedUser(invited);
        invitation.setInvitedBy(user);
        invitation.setInvitationStatus(InvitationStatus.PENDING);
        invitation.setInvitedAt(LocalDateTime.now());
        invitation.setMessage(trimToNull(request.message()));
        invitationRepository.saveAndFlush(invitation);
        notify(invited, NotificationType.INVITATION_RECEIVED, "Team invitation received", "You were invited to join " + team.getTeamName() + ".", "TeamInvitation", invitation.getId());
        return toInvitationResponse(invitation);
    }

    public TeamInvitationResponse acceptInvitation(String email, Long invitationId) {
        User user = requireCurrentUser(email);
        TeamInvitation invitation = requireInvitation(invitationId);
        if (!Objects.equals(invitation.getInvitedUser().getId(), user.getId())) {
            throw new InvalidInstitutionRelationshipException("This invitation does not belong to your account.");
        }
        if (invitation.getInvitationStatus() != InvitationStatus.PENDING) {
            throw new BusinessRuleViolationException("This invitation is no longer pending.");
        }
        Event event = invitation.getTeam().getEvent();
        EventRegistrationConfig config = getConfig(event);
        ensureRegistrationOpen(event, config);
        ensureNoConflict(user, event);
        ensureNoDuplicateRegistration(user, event);
        validateEligibility(user, event);
        if (teamMemberRepository.countByTeam_IdAndDeletedFalse(invitation.getTeam().getId()) >= maxTeamSize(config)) {
            throw new BusinessRuleViolationException("The team has reached its maximum size.");
        }

        invitation.setInvitationStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.saveAndFlush(invitation);
        addTeamMember(invitation.getTeam(), user, TeamMemberRole.MEMBER);
        EventRegistration registration = createRegistration(event, user, RegistrationType.TEAM, invitation.getTeam(), invitation.getTeam().getTeamName(), config);
        saveRegistration(registration);
        notify(user, NotificationType.INVITATION_ACCEPTED, "Invitation accepted", "You joined " + invitation.getTeam().getTeamName() + ".", "TeamInvitation", invitation.getId());
        return toInvitationResponse(invitation);
    }

    public TeamInvitationResponse rejectInvitation(String email, Long invitationId) {
        User user = requireCurrentUser(email);
        TeamInvitation invitation = requireInvitation(invitationId);
        if (!Objects.equals(invitation.getInvitedUser().getId(), user.getId())) {
            throw new InvalidInstitutionRelationshipException("This invitation does not belong to your account.");
        }
        invitation.setInvitationStatus(InvitationStatus.REJECTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.saveAndFlush(invitation);
        notify(invitation.getInvitedBy(), NotificationType.INVITATION_REJECTED, "Invitation rejected", invitation.getInvitedUser().getFirstName() + " declined the team invitation.", "TeamInvitation", invitation.getId());
        return toInvitationResponse(invitation);
    }

    public RegistrationSummaryResponse approveRegistration(String email, Long registrationId, RegistrationDecisionRequest request) {
        User user = requireCurrentUser(email);
        EventRegistration registration = requireRegistration(registrationId);
        scopeResolver.resolveForWrite(user, registration.getEvent().getInstitution().getId());
        if (request.status() == RegistrationStatus.REJECTED) {
            registration.setRegistrationStatus(RegistrationStatus.REJECTED);
            registration.setRejectionReason(trimToNull(request.rejectionReason()));
            registrationRepository.saveAndFlush(registration);
            notify(registration.getParticipant(), NotificationType.REGISTRATION_REJECTED, "Registration rejected", "Your registration for " + registration.getEvent().getTitle() + " was rejected.", "EventRegistration", registration.getId());
            promoteWaitlist(registration.getEvent());
            return toRegistrationSummary(registration);
        }
        if (request.status() != RegistrationStatus.APPROVED) {
            throw new BusinessRuleViolationException("Only approved or rejected status changes are supported.");
        }
        if (isAtCapacity(registration.getEvent(), registration.getId())) {
            if (getConfig(registration.getEvent()) != null && getConfig(registration.getEvent()).isWaitlistEnabled()) {
                registration.setRegistrationStatus(RegistrationStatus.WAITLISTED);
                registration.setWaitlistPosition(nextWaitlistPosition(registration.getEvent()));
                notify(registration.getParticipant(), NotificationType.WAITLISTED, "Registration waitlisted", "The event is currently full.", "EventRegistration", registration.getId());
            } else {
                throw new BusinessRuleViolationException("The event has reached its capacity.");
            }
        } else {
            registration.setRegistrationStatus(RegistrationStatus.APPROVED);
            registration.setApprovedAt(LocalDateTime.now());
            registration.setApprovedBy(user);
            notify(registration.getParticipant(), NotificationType.REGISTRATION_APPROVED, "Registration approved", "Your registration for " + registration.getEvent().getTitle() + " was approved.", "EventRegistration", registration.getId());
        }
        registrationRepository.saveAndFlush(registration);
        return toRegistrationSummary(registration);
    }

    public RegistrationSummaryResponse promoteWaitlistEntry(String email, Long registrationId) {
        return approveRegistration(email, registrationId, new RegistrationDecisionRequest(RegistrationStatus.APPROVED, null, null));
    }

    public RegistrationSummaryResponse cancelRegistration(String email, Long registrationId) {
        User user = requireCurrentUser(email);
        EventRegistration registration = requireRegistration(registrationId);
        scopeResolver.resolveForWrite(user, registration.getEvent().getInstitution().getId());
        if (!Objects.equals(user.getId(), registration.getParticipant().getId()) && !currentUserContext.isAdministrator(user) && !currentUserContext.isFaculty(user)) {
            throw new BusinessRuleViolationException("You can only cancel your own registration.");
        }
        registration.setRegistrationStatus(RegistrationStatus.CANCELLED);
        registration.setWaitlistPosition(null);
        registrationRepository.saveAndFlush(registration);
        notify(registration.getParticipant(), NotificationType.REGISTRATION_CANCELLED, "Registration cancelled", "Your registration for " + registration.getEvent().getTitle() + " was cancelled.", "EventRegistration", registration.getId());
        promoteWaitlist(registration.getEvent());
        return toRegistrationSummary(registration);
    }

    public TeamMemberResponse leaveTeam(String email, Long teamId) {
        User user = requireCurrentUser(email);
        Team team = requireTeam(teamId);
        scopeResolver.resolveForWrite(user, team.getInstitution().getId());
        TeamMember member = teamMemberRepository.findByTeam_IdAndUser_IdAndDeletedFalse(teamId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this team."));
        if (member.getRole() == TeamMemberRole.LEADER && teamMemberRepository.countByTeam_IdAndDeletedFalse(teamId) > 1) {
            throw new BusinessRuleViolationException("Transfer team ownership before leaving the team.");
        }
        if (member.getRole() == TeamMemberRole.LEADER) {
            invitationRepository.findByTeam_IdAndDeletedFalse(teamId).forEach(invitation -> {
                invitation.setDeleted(true);
                invitation.setStatus(RecordStatus.INACTIVE);
                invitationRepository.save(invitation);
            });
            team.setDeleted(true);
            team.setStatus(RecordStatus.INACTIVE);
            teamRepository.save(team);
        }
        member.setDeleted(true);
        member.setStatus(RecordStatus.INACTIVE);
        teamMemberRepository.save(member);
        return toTeamMemberResponse(member);
    }

    public TeamResponse updateTeam(String email, Long teamId, TeamRequest request) {
        User user = requireCurrentUser(email);
        Team team = requireTeam(teamId);
        scopeResolver.resolveForWrite(user, team.getInstitution().getId());
        ensureTeamManager(user, team);
        String normalizedName = trim(request.teamName());
        String normalizedCode = trim(request.teamCode());
        if (teamRepository.existsByEvent_IdAndTeamNameIgnoreCaseAndIdNotAndDeletedFalse(team.getEvent().getId(), normalizedName, teamId)) {
            throw new DuplicateResourceException("Team name already exists for this event.");
        }
        if (teamRepository.existsByEvent_IdAndTeamCodeIgnoreCaseAndIdNotAndDeletedFalse(team.getEvent().getId(), normalizedCode, teamId)) {
            throw new DuplicateResourceException("Team code already exists for this event.");
        }
        team.setTeamName(normalizedName);
        team.setTeamCode(normalizedCode);
        teamRepository.saveAndFlush(team);
        return toTeamResponse(team);
    }

    public TeamInvitationResponse cancelInvitation(String email, Long invitationId) {
        User user = requireCurrentUser(email);
        TeamInvitation invitation = requireInvitation(invitationId);
        scopeResolver.resolveForWrite(user, invitation.getTeam().getInstitution().getId());
        if (!Objects.equals(invitation.getInvitedBy().getId(), user.getId()) && !ensureManagerSilently(user, invitation.getTeam())) {
            throw new BusinessRuleViolationException("You are not allowed to cancel this invitation.");
        }
        if (invitation.getInvitationStatus() != InvitationStatus.PENDING) {
            throw new BusinessRuleViolationException("Only pending invitations can be cancelled.");
        }
        invitation.setInvitationStatus(InvitationStatus.CANCELLED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.saveAndFlush(invitation);
        notify(invitation.getInvitedUser(), NotificationType.INVITATION_CANCELLED, "Invitation cancelled", "The invitation to join " + invitation.getTeam().getTeamName() + " was cancelled.", "TeamInvitation", invitation.getId());
        return toInvitationResponse(invitation);
    }

    public TeamMemberResponse removeTeamMember(String email, Long teamId, Long memberId) {
        User user = requireCurrentUser(email);
        Team team = requireTeam(teamId);
        scopeResolver.resolveForWrite(user, team.getInstitution().getId());
        ensureTeamManager(user, team);
        TeamMember member = teamMemberRepository.findByIdAndDeletedFalse(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found."));
        if (!Objects.equals(member.getTeam().getId(), teamId)) {
            throw new InvalidInstitutionRelationshipException("The selected member does not belong to this team.");
        }
        if (member.getRole() == TeamMemberRole.LEADER) {
            throw new BusinessRuleViolationException("Transfer leadership before removing the team leader.");
        }
        member.setDeleted(true);
        member.setStatus(RecordStatus.INACTIVE);
        teamMemberRepository.saveAndFlush(member);
        return toTeamMemberResponse(member);
    }

    public TeamMemberResponse transferTeamOwnership(String email, Long teamId, TeamTransferRequest request) {
        User user = requireCurrentUser(email);
        Team team = requireTeam(teamId);
        scopeResolver.resolveForWrite(user, team.getInstitution().getId());
        ensureTeamManager(user, team);
        User newLeader = requireUser(request.newLeaderUserId());
        ensureSameInstitution(team.getInstitution(), newLeader);
        TeamMember newLeaderMember = teamMemberRepository.findByTeam_IdAndUser_IdAndDeletedFalse(teamId, newLeader.getId())
                .orElseThrow(() -> new BusinessRuleViolationException("The selected user must already be a team member."));
        teamMemberRepository.findByTeam_IdAndUser_IdAndDeletedFalse(teamId, team.getLeader().getId()).ifPresent(member -> member.setRole(TeamMemberRole.MEMBER));
        newLeaderMember.setRole(TeamMemberRole.LEADER);
        team.setLeader(newLeader);
        teamRepository.save(team);
        return toTeamMemberResponse(newLeaderMember);
    }

    public void deleteTeam(String email, Long teamId) {
        User user = requireCurrentUser(email);
        Team team = requireTeam(teamId);
        scopeResolver.resolveForWrite(user, team.getInstitution().getId());
        ensureTeamManager(user, team);
        invitationRepository.findByTeam_IdAndDeletedFalse(teamId).forEach(invitation -> {
            invitation.setDeleted(true);
            invitation.setStatus(RecordStatus.INACTIVE);
            invitationRepository.save(invitation);
        });
        team.setDeleted(true);
        team.setStatus(RecordStatus.INACTIVE);
        teamRepository.save(team);
        teamMemberRepository.findByTeam_IdAndDeletedFalse(teamId).forEach(member -> {
            member.setDeleted(true);
            member.setStatus(RecordStatus.INACTIVE);
            teamMemberRepository.save(member);
        });
    }

    public PageResponse<RegistrationSummaryResponse> listRegistrations(String email, Long institutionId, Long eventId, String search, RegistrationStatus status, int page, int size) {
        User user = requireCurrentUser(email);
        Institution scope = scopeResolver.resolveForRead(user, institutionId);
        List<RegistrationSummaryResponse> content = registrationRepository.findAll().stream()
                .filter(registration -> !registration.isDeleted())
                .filter(registration -> scope == null || Objects.equals(registration.getInstitution().getId(), scope.getId()))
                .filter(registration -> eventId == null || Objects.equals(registration.getEvent().getId(), eventId))
                .filter(registration -> status == null || registration.getRegistrationStatus() == status)
                .filter(registration -> matches(registration, search))
                .sorted(Comparator.comparing(EventRegistration::getRegistrationDate).reversed())
                .map(this::toRegistrationSummary)
                .toList();
        return page(content, page, size);
    }

    public PageResponse<RegistrationSummaryResponse> listMyRegistrations(String email, String search, RegistrationStatus status, int page, int size) {
        User user = requireCurrentUser(email);
        List<RegistrationSummaryResponse> content = registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId()).stream()
                .filter(registration -> status == null || registration.getRegistrationStatus() == status)
                .filter(registration -> matches(registration, search))
                .map(this::toRegistrationSummary)
                .toList();
        return page(content, page, size);
    }

    public RegistrationDashboardResponse getDashboard(String email) {
        User user = requireCurrentUser(email);
        List<EventRegistration> source = currentUserContext.isAdministrator(user) || currentUserContext.isFaculty(user)
                ? registrationRepository.findAll().stream().filter(item -> !item.isDeleted() && (user.getInstitution() == null || Objects.equals(item.getInstitution().getId(), user.getInstitution().getId()))).toList()
                : registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId());
        long approved = source.stream().filter(r -> r.getRegistrationStatus() == RegistrationStatus.APPROVED).count();
        long pending = source.stream().filter(r -> r.getRegistrationStatus() == RegistrationStatus.PENDING).count();
        long rejected = source.stream().filter(r -> r.getRegistrationStatus() == RegistrationStatus.REJECTED).count();
        long waitlisted = source.stream().filter(r -> r.getRegistrationStatus() == RegistrationStatus.WAITLISTED).count();
        long cancelled = source.stream().filter(r -> r.getRegistrationStatus() == RegistrationStatus.CANCELLED).count();
        List<RegistrationSummaryResponse> recent = source.stream().limit(8).map(this::toRegistrationSummary).toList();
        List<RegistrationSummaryResponse> upcoming = source.stream().filter(reg -> reg.getEvent().getStartDateTime() != null && reg.getEvent().getStartDateTime().isAfter(LocalDateTime.now())).limit(6).map(this::toRegistrationSummary).toList();
        List<RegistrationSummaryResponse> pendingApprovals = source.stream().filter(reg -> reg.getRegistrationStatus() == RegistrationStatus.PENDING).limit(6).map(this::toRegistrationSummary).toList();
        return new RegistrationDashboardResponse(source.size(), approved, pending, rejected, waitlisted, cancelled, recent, upcoming, pendingApprovals);
    }

    public List<RegistrationSummaryResponse> listWaitlist(String email, Long eventId) {
        User user = requireCurrentUser(email);
        Institution scope = scopeResolver.resolveForRead(user, null);
        return registrationRepository.findAll().stream()
                .filter(registration -> !registration.isDeleted())
                .filter(registration -> registration.getRegistrationStatus() == RegistrationStatus.WAITLISTED)
                .filter(registration -> eventId == null || Objects.equals(registration.getEvent().getId(), eventId))
                .filter(registration -> scope == null || Objects.equals(registration.getInstitution().getId(), scope.getId()))
                .sorted(Comparator.comparing(EventRegistration::getWaitlistPosition, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(EventRegistration::getRegistrationDate, Comparator.reverseOrder()))
                .map(this::toRegistrationSummary)
                .toList();
    }

    public List<RegistrationSummaryResponse> listMyWaitlist(String email) {
        User user = requireCurrentUser(email);
        return registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId()).stream()
                .filter(registration -> registration.getRegistrationStatus() == RegistrationStatus.WAITLISTED)
                .map(this::toRegistrationSummary)
                .toList();
    }

    public List<TeamResponse> listTeams(String email, Long eventId) {
        User user = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(user, event.getInstitution().getId());
        return teamRepository.findByEvent_IdAndDeletedFalse(eventId).stream().map(this::toTeamResponse).toList();
    }

    public List<TeamMemberResponse> listTeamMembers(String email, Long teamId) {
        User user = requireCurrentUser(email);
        Team team = requireTeam(teamId);
        scopeResolver.resolveForRead(user, team.getInstitution().getId());
        return teamMemberRepository.findByTeam_IdAndDeletedFalse(teamId).stream().map(this::toTeamMemberResponse).toList();
    }

    public List<TeamInvitationResponse> listInvitations(String email, Long teamId) {
        User user = requireCurrentUser(email);
        Team team = requireTeam(teamId);
        scopeResolver.resolveForRead(user, team.getInstitution().getId());
        return invitationRepository.findByTeam_IdAndDeletedFalse(teamId).stream().map(this::toInvitationResponse).toList();
    }

    public List<NotificationResponse> listNotifications(String email) {
        User user = requireCurrentUser(email);
        return notificationRepository.findByRecipient_IdAndDeletedFalseOrderByCreatedAtDesc(user.getId()).stream().map(this::toNotificationResponse).toList();
    }

    public long getUnreadNotificationCount(String email) {
        User user = requireCurrentUser(email);
        return notificationRepository.countByRecipient_IdAndReadAtIsNullAndDeletedFalse(user.getId());
    }

    public void markAllNotificationsRead(String email) {
        User user = requireCurrentUser(email);
        notificationRepository.findByRecipient_IdAndDeletedFalseOrderByCreatedAtDesc(user.getId()).forEach(notification -> {
            if (notification.getReadAt() == null) {
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        });
    }

    public NotificationResponse markNotificationRead(String email, Long id) {
        User user = requireCurrentUser(email);
        InAppNotification notification = notificationRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        if (!Objects.equals(notification.getRecipient().getId(), user.getId())) {
            throw new InvalidInstitutionRelationshipException("You cannot modify another user's notification.");
        }
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        return toNotificationResponse(notification);
    }

    private RegistrationContextResponse buildContext(Event event) {
        EventRegistrationConfig config = getConfig(event);
        boolean open = event.getEventStatus() == EventStatus.REGISTRATION_OPEN
                && (event.getRegistrationEndDateTime() == null || !event.getRegistrationEndDateTime().isBefore(LocalDateTime.now()))
                && event.getEventStatus() != EventStatus.CANCELLED
                && event.getEventStatus() != EventStatus.ARCHIVED;
        int seatsRemaining = event.getMaximumParticipants() == null ? Integer.MAX_VALUE : Math.max(event.getMaximumParticipants() - currentSeatCount(event), 0);
        return new RegistrationContextResponse(
                event.getId(),
                event.getTitle(),
                event.getEventCode(),
                event.getEventStatus().name(),
                event.getMode().name(),
                event.getVisibility().name(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getRegistrationEndDateTime(),
                open,
                config != null && config.isWaitlistEnabled(),
                config != null && config.isApprovalRequired(),
                config != null && config.isTeamEvent(),
                config != null && config.getMinimumTeamSize() != null ? config.getMinimumTeamSize() : 1,
                config != null && config.getMaximumTeamSize() != null ? config.getMaximumTeamSize() : 5,
                event.getMaximumParticipants() == null ? null : seatsRemaining,
                open && (event.getMaximumParticipants() == null || seatsRemaining > 0 || (config != null && config.isWaitlistEnabled())),
                open ? "Registration is available." : "Registration is currently closed."
        );
    }

    private EventRegistration createRegistration(Event event, User user, RegistrationType type, Team team, String remarks, EventRegistrationConfig config) {
        EventRegistration registration = new EventRegistration();
        registration.setInstitution(event.getInstitution());
        registration.setEvent(event);
        registration.setParticipant(user);
        registration.setRegistrationNumber(generateRegistrationNumber(event, user));
        registration.setRegistrationType(type);
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setAttendanceStatus(AttendanceStatus.NOT_MARKED);
        registration.setCertificateEligible(false);
        registration.setRemarks(trimToNull(remarks));
        registration.setTeam(team);
        if (config != null && config.isApprovalRequired()) {
            registration.setRegistrationStatus(RegistrationStatus.PENDING);
        } else if (isAtCapacity(event, null)) {
            if (config != null && config.isWaitlistEnabled()) {
                registration.setRegistrationStatus(RegistrationStatus.WAITLISTED);
                registration.setWaitlistPosition(nextWaitlistPosition(event));
            } else {
                throw new BusinessRuleViolationException("The event has reached its capacity.");
            }
        } else {
            registration.setRegistrationStatus(RegistrationStatus.APPROVED);
            registration.setApprovedAt(LocalDateTime.now());
        }
        return registration;
    }

    private void saveRegistration(EventRegistration registration) {
        try {
            registrationRepository.saveAndFlush(registration);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("You are already registered for this event.");
        }
    }

    private void ensureRegistrationOpen(Event event, EventRegistrationConfig config) {
        if (event.getEventStatus() != EventStatus.REGISTRATION_OPEN) {
            throw new BusinessRuleViolationException("Registration is open only when the event status is Registration Open.");
        }
        if (event.getRegistrationEndDateTime() != null && event.getRegistrationEndDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleViolationException("Registration has closed for this event.");
        }
        if (config != null && !config.isRegistrationRequired()) {
            return;
        }
    }

    private void ensureNoDuplicateRegistration(User user, Event event) {
        if (registrationRepository.existsByEvent_IdAndParticipant_IdAndDeletedFalse(event.getId(), user.getId())) {
            throw new DuplicateResourceException("You are already registered for this event.");
        }
    }

    private void ensureNoConflict(User user, Event event) {
        if (event.getStartDateTime() == null || event.getEndDateTime() == null) {
            return;
        }
        registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId()).stream()
                .filter(existing -> !Objects.equals(existing.getEvent().getId(), event.getId()))
                .filter(existing -> existing.getRegistrationStatus() != RegistrationStatus.CANCELLED && existing.getRegistrationStatus() != RegistrationStatus.REJECTED)
                .forEach(existing -> {
                    Event other = existing.getEvent();
                    if (other.getStartDateTime() != null && other.getEndDateTime() != null) {
                        boolean overlaps = event.getStartDateTime().isBefore(other.getEndDateTime()) && event.getEndDateTime().isAfter(other.getStartDateTime());
                        if (overlaps) {
                            throw new BusinessRuleViolationException("You are already registered for another event during this time: " + other.getTitle() + " on " + other.getStartDateTime().toLocalDate() + ".");
                        }
                    }
                });
    }

    private void validateEligibility(User user, Event event) {
        List<EventEligibilityRule> rules = eligibilityRuleRepository.findByEvent_IdAndDeletedFalse(event.getId());
        if (rules.isEmpty()) {
            return;
        }
        boolean includeRulesPresent = rules.stream().anyMatch(rule -> rule.getRuleType().name().equals("INCLUDE"));
        boolean included = !includeRulesPresent || rules.stream().anyMatch(rule -> rule.getRuleType().name().equals("INCLUDE") && matchesRule(user, rule));
        boolean excluded = rules.stream().anyMatch(rule -> rule.getRuleType().name().equals("EXCLUDE") && matchesRule(user, rule));
        if (excluded || !included) {
            throw new BusinessRuleViolationException("You do not meet the eligibility criteria for this event.");
        }
    }

    private boolean matchesRule(User user, EventEligibilityRule rule) {
        if (rule.getDepartment() != null && !matchesDepartment(user, rule.getDepartment())) {
            return false;
        }
        if (rule.getProgramme() != null && !matchesProgramme(user, rule.getProgramme())) {
            return false;
        }
        if (rule.getSection() != null && !matchesSection(user, rule.getSection())) {
            return false;
        }
        if (rule.getMinimumYear() != null) {
            Integer year = parseYear(user.getAcademicYear());
            if (year == null || year < rule.getMinimumYear()) {
                return false;
            }
        }
        if (rule.getMaximumYear() != null) {
            Integer year = parseYear(user.getAcademicYear());
            if (year == null || year > rule.getMaximumYear()) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesDepartment(User user, Department department) {
        String profileDept = normalize(user.getDepartment());
        return profileDept != null && (profileDept.equals(normalize(department.getDepartmentName())) || profileDept.equals(normalize(department.getShortName())) || profileDept.equals(normalize(department.getDepartmentCode())));
    }

    private boolean matchesProgramme(User user, Programme programme) {
        String profile = normalize(user.getAcademicYear());
        return profile != null && (profile.contains(normalize(programme.getProgrammeName())) || profile.contains(normalize(programme.getProgrammeCode())));
    }

    private boolean matchesSection(User user, Section section) {
        if (user.getSection() == null) {
            return false;
        }
        String profile = normalize(user.getSection());
        return profile.equals(normalize(section.getSectionName())) || (section.getSemester() != null && profile.equals(String.valueOf(section.getSemester().getSemesterNumber())));
    }

    private void promoteWaitlist(Event event) {
        if (getConfig(event) == null || !getConfig(event).isWaitlistEnabled()) {
            return;
        }
        boolean promotedAny = false;
        while (!isAtCapacity(event, null)) {
            EventRegistration next = registrationRepository.findByEvent_IdAndRegistrationStatusAndDeletedFalseOrderByWaitlistPositionAsc(event.getId(), RegistrationStatus.WAITLISTED).stream()
                    .filter(registration -> registration.getWaitlistPosition() != null)
                    .sorted(Comparator.comparingInt(EventRegistration::getWaitlistPosition))
                    .findFirst()
                    .orElse(null);
            if (next == null) {
                break;
            }
            next.setRegistrationStatus(RegistrationStatus.APPROVED);
            next.setWaitlistPosition(null);
            next.setApprovedAt(LocalDateTime.now());
            registrationRepository.saveAndFlush(next);
            notify(next.getParticipant(), NotificationType.WAITLIST_PROMOTED, "Waitlist promotion", "A seat opened up for " + event.getTitle() + ".", "EventRegistration", next.getId());
            promotedAny = true;
        }
        if (promotedAny) {
            resequenceWaitlist(event);
        }
    }

    private void resequenceWaitlist(Event event) {
        List<EventRegistration> queue = registrationRepository.findByEvent_IdAndRegistrationStatusAndDeletedFalseOrderByWaitlistPositionAsc(event.getId(), RegistrationStatus.WAITLISTED).stream()
                .filter(registration -> registration.getWaitlistPosition() != null)
                .sorted(Comparator.comparingInt(EventRegistration::getWaitlistPosition))
                .toList();
        int position = 1;
        for (EventRegistration registration : queue) {
            if (!Objects.equals(registration.getWaitlistPosition(), position)) {
                registration.setWaitlistPosition(position);
                registrationRepository.save(registration);
            }
            position++;
        }
    }

    private boolean isAtCapacity(Event event, Long ignoreRegistrationId) {
        Integer max = event.getMaximumParticipants();
        if (max == null || max <= 0) {
            return false;
        }
        long taken = registrationRepository.findByEvent_IdAndDeletedFalseOrderByRegistrationDateDesc(event.getId()).stream()
                .filter(registration -> registration.getRegistrationStatus() == RegistrationStatus.APPROVED || registration.getRegistrationStatus() == RegistrationStatus.PENDING)
                .filter(registration -> ignoreRegistrationId == null || !Objects.equals(registration.getId(), ignoreRegistrationId))
                .count();
        return taken >= max;
    }

    private int currentSeatCount(Event event) {
        return (int) registrationRepository.findByEvent_IdAndDeletedFalseOrderByRegistrationDateDesc(event.getId()).stream()
                .filter(registration -> registration.getRegistrationStatus() == RegistrationStatus.APPROVED || registration.getRegistrationStatus() == RegistrationStatus.PENDING)
                .count();
    }

    private int maxTeamSize(EventRegistrationConfig config) {
        if (config == null || config.getMaximumTeamSize() == null || config.getMaximumTeamSize() <= 0) {
            return Integer.MAX_VALUE;
        }
        return config.getMaximumTeamSize();
    }

    private int nextWaitlistPosition(Event event) {
        return (int) registrationRepository.findByEvent_IdAndRegistrationStatusAndDeletedFalseOrderByWaitlistPositionAsc(event.getId(), RegistrationStatus.WAITLISTED).stream()
                .filter(registration -> registration.getWaitlistPosition() != null)
                .count() + 1;
    }

    private Team createTeamInternal(Event event, User leader, String teamName, String teamCode) {
        String normalizedName = trim(teamName);
        String normalizedCode = trimToNull(teamCode);
        if (normalizedCode == null) {
            normalizedCode = generateCode(normalizedName);
        }
        if (teamRepository.existsByEvent_IdAndTeamNameIgnoreCaseAndDeletedFalse(event.getId(), normalizedName)) {
            throw new DuplicateResourceException("Team name already exists for this event.");
        }
        if (teamRepository.existsByEvent_IdAndTeamCodeIgnoreCaseAndDeletedFalse(event.getId(), normalizedCode)) {
            throw new DuplicateResourceException("Team code already exists for this event.");
        }
        Team team = new Team();
        team.setInstitution(event.getInstitution());
        team.setEvent(event);
        team.setTeamName(normalizedName);
        team.setTeamCode(normalizedCode);
        team.setLeader(leader);
        team.setPrimaryContact(leader.getEmail());
        team.setStatus(RecordStatus.ACTIVE);
        return teamRepository.save(team);
    }

    private TeamMember addTeamMember(Team team, User user, TeamMemberRole role) {
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());
        member.setStatus(RecordStatus.ACTIVE);
        return teamMemberRepository.save(member);
    }

    private void ensureTeamManager(User currentUser, Team team) {
        if (!Objects.equals(team.getLeader().getId(), currentUser.getId()) && !currentUserContext.isAdministrator(currentUser) && !currentUserContext.isFaculty(currentUser)) {
            throw new BusinessRuleViolationException("You are not allowed to manage this team.");
        }
    }

    private boolean ensureManagerSilently(User currentUser, Team team) {
        return Objects.equals(team.getLeader().getId(), currentUser.getId())
                || currentUserContext.isAdministrator(currentUser)
                || currentUserContext.isFaculty(currentUser);
    }

    private boolean isRegistrationOpen(Event event, EventRegistrationConfig config) {
        return event.getEventStatus() == EventStatus.REGISTRATION_OPEN
                && (event.getRegistrationEndDateTime() == null || !event.getRegistrationEndDateTime().isBefore(LocalDateTime.now()))
                && event.getEventStatus() != EventStatus.CANCELLED
                && event.getEventStatus() != EventStatus.ARCHIVED;
    }

    private List<RegistrationConflictResponse> findConflicts(User user, Event event) {
        if (event.getStartDateTime() == null || event.getEndDateTime() == null) {
            return List.of();
        }
        return registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId()).stream()
                .filter(existing -> !Objects.equals(existing.getEvent().getId(), event.getId()))
                .filter(existing -> existing.getRegistrationStatus() != RegistrationStatus.CANCELLED && existing.getRegistrationStatus() != RegistrationStatus.REJECTED)
                .map(existing -> toConflictResponse(existing, event))
                .filter(Objects::nonNull)
                .toList();
    }

    private RegistrationConflictResponse toConflictResponse(EventRegistration existing, Event requestedEvent) {
        Event other = existing.getEvent();
        if (other.getStartDateTime() == null || other.getEndDateTime() == null || requestedEvent.getStartDateTime() == null || requestedEvent.getEndDateTime() == null) {
            return null;
        }
        boolean overlaps = requestedEvent.getStartDateTime().isBefore(other.getEndDateTime())
                && requestedEvent.getEndDateTime().isAfter(other.getStartDateTime());
        if (!overlaps) {
            return null;
        }
        return new RegistrationConflictResponse(
                other.getId(),
                other.getTitle(),
                other.getStartDateTime(),
                other.getEndDateTime(),
                existing.getParticipant().getFirstName() + " " + existing.getParticipant().getLastName(),
                "This registration overlaps with another active registration."
        );
    }

    private Event requireEvent(Long id) {
        return eventRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Event not found."));
    }

    private Team requireTeam(Long id) {
        return teamRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Team not found."));
    }

    private TeamInvitation requireInvitation(Long id) {
        return invitationRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Invitation not found."));
    }

    private EventRegistration requireRegistration(Long id) {
        return registrationRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Registration not found."));
    }

    private User requireUser(Long id) {
        return userRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private User requireCurrentUser(String email) {
        return currentUserContext.requireCurrentUser(email);
    }

    private EventRegistrationConfig getConfig(Event event) {
        return configRepository.findByEvent_IdAndDeletedFalse(event.getId()).orElse(null);
    }

    private void ensureSameInstitution(Institution institution, User user) {
        if (user.getInstitution() == null || !Objects.equals(institution.getId(), user.getInstitution().getId())) {
            throw new InvalidInstitutionRelationshipException("The selected user belongs to another institution.");
        }
    }

    private boolean matches(EventRegistration registration, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String term = normalize(search);
        return contains(registration.getRegistrationNumber(), term)
                || contains(registration.getEvent().getTitle(), term)
                || contains(registration.getEvent().getEventCode(), term)
                || contains(registration.getParticipant().getFirstName(), term)
                || contains(registration.getParticipant().getLastName(), term)
                || contains(registration.getParticipant().getEmail(), term);
    }

    private boolean contains(String value, String search) {
        return value != null && search != null && value.toUpperCase(Locale.ROOT).contains(search);
    }

    private Integer parseYear(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(digits.substring(0, 1));
        } catch (Exception ex) {
            return null;
        }
    }

    private String generateRegistrationNumber(Event event, User user) {
        return (event.getEventCode() + "-" + user.getId() + "-" + Math.abs(System.nanoTime() % 100000)).toUpperCase(Locale.ROOT);
    }

    private String generateCode(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "TEAM-" + Math.abs(System.nanoTime() % 100000);
        }
        return normalized.replaceAll("[^A-Z0-9]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "") + "-" + Math.abs(System.nanoTime() % 1000);
    }

    private String normalize(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private void notify(User recipient, NotificationType type, String title, String message, String relatedType, Long relatedId) {
        InAppNotification notification = new InAppNotification();
        notification.setRecipient(recipient);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType(relatedType);
        notification.setRelatedEntityId(relatedId);
        notification.setStatus(RecordStatus.ACTIVE);
        notificationRepository.save(notification);
    }

    private void notifyForRegistration(EventRegistration registration) {
        if (registration.getRegistrationStatus() == RegistrationStatus.APPROVED) {
            notify(registration.getParticipant(), NotificationType.REGISTRATION_APPROVED, "Registration approved", "Your registration for " + registration.getEvent().getTitle() + " was approved.", "EventRegistration", registration.getId());
        } else if (registration.getRegistrationStatus() == RegistrationStatus.WAITLISTED) {
            notify(registration.getParticipant(), NotificationType.WAITLISTED, "Registration waitlisted", "The event is currently full. You have been placed on the waitlist.", "EventRegistration", registration.getId());
        }
    }

    private TeamResponse toTeamResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getEvent().getId(),
                team.getEvent().getTitle(),
                team.getTeamName(),
                team.getTeamCode(),
                team.getLeader().getId(),
                team.getLeader().getFirstName() + " " + team.getLeader().getLastName(),
                team.getStatus() == null ? TeamStatus.ACTIVE : TeamStatus.valueOf(team.getStatus().name()),
                (int) teamMemberRepository.countByTeam_IdAndDeletedFalse(team.getId()),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }

    private TeamMemberResponse toTeamMemberResponse(TeamMember member) {
        return new TeamMemberResponse(
                member.getId(),
                member.getTeam().getId(),
                member.getUser().getId(),
                member.getUser().getFirstName() + " " + member.getUser().getLastName(),
                member.getUser().getEmail(),
                member.getRole(),
                member.getJoinedAt(),
                member.getStatus().name()
        );
    }

    private TeamInvitationResponse toInvitationResponse(TeamInvitation invitation) {
        return new TeamInvitationResponse(
                invitation.getId(),
                invitation.getTeam().getId(),
                invitation.getTeam().getTeamName(),
                invitation.getInvitedUser().getId(),
                invitation.getInvitedUser().getFirstName() + " " + invitation.getInvitedUser().getLastName(),
                invitation.getInvitationStatus(),
                invitation.getInvitedAt(),
                invitation.getRespondedAt(),
                invitation.getMessage()
        );
    }

    private NotificationResponse toNotificationResponse(InAppNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipient().getId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notificationTargetRoute(notification),
                notificationSeverity(notification.getNotificationType()),
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    private String notificationTargetRoute(InAppNotification notification) {
        if ("TeamInvitation".equalsIgnoreCase(notification.getRelatedEntityType())) {
            return "/dashboard/notifications";
        }
        if ("EventRegistration".equalsIgnoreCase(notification.getRelatedEntityType())) {
            return "/dashboard/registrations";
        }
        return "/dashboard/notifications";
    }

    private String notificationSeverity(NotificationType type) {
        return switch (type) {
            case REGISTRATION_REJECTED, REGISTRATION_CANCELLED, INVITATION_REJECTED, INVITATION_CANCELLED -> "warning";
            case REGISTRATION_APPROVED, WAITLIST_PROMOTED, INVITATION_ACCEPTED -> "success";
            case WAITLISTED, INVITATION_RECEIVED -> "info";
            default -> "neutral";
        };
    }

    private RegistrationSummaryResponse toRegistrationSummary(EventRegistration registration) {
        return new RegistrationSummaryResponse(
                registration.getId(),
                registration.getEvent().getId(),
                registration.getEvent().getEventCode(),
                registration.getEvent().getTitle(),
                registration.getParticipant().getId(),
                registration.getParticipant().getFirstName() + " " + registration.getParticipant().getLastName(),
                registration.getRegistrationNumber(),
                registration.getRegistrationType(),
                registration.getRegistrationStatus(),
                registration.getRegistrationDate(),
                registration.getApprovedAt(),
                registration.getApprovedBy() == null ? null : registration.getApprovedBy().getFirstName() + " " + registration.getApprovedBy().getLastName(),
                registration.getAttendanceStatus(),
                registration.isCertificateEligible(),
                registration.getWaitlistPosition(),
                registration.getTeam() == null ? null : registration.getTeam().getId(),
                registration.getTeam() == null ? null : registration.getTeam().getTeamName(),
                registration.getRemarks()
        );
    }

    private PageResponse<RegistrationSummaryResponse> page(List<RegistrationSummaryResponse> content, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        long totalElements = content.size();
        int from = Math.min(safePage * safeSize, content.size());
        int to = Math.min(from + safeSize, content.size());
        List<RegistrationSummaryResponse> slice = content.subList(from, to);
        int totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / safeSize);
        return PageResponse.of(slice, safePage, safeSize, totalElements, totalPages, safePage == 0, safePage >= totalPages - 1);
    }
}
