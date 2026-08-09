package com.campussphere.service;

import com.campussphere.dto.PageResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsActivityResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsAttendanceRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsCertificateRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsDepartmentRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsDistributionPointResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsEventRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsExportResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsInsightResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsMetricResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsOverviewResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsQuery;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsRegistrationRowResponse;
import com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsTrendPointResponse;
import com.campussphere.entity.AcademicYear;
import com.campussphere.entity.Department;
import com.campussphere.entity.Institution;
import com.campussphere.entity.Programme;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.RoleCode;
import com.campussphere.entity.User;
import com.campussphere.entity.attendance.AttendanceRecord;
import com.campussphere.entity.certificate.Certificate;
import com.campussphere.entity.certificate.CertificateStatus;
import com.campussphere.entity.certificate.CertificateVerification;
import com.campussphere.entity.certificate.CertificateVerificationStatus;
import com.campussphere.entity.event.CoordinatorRole;
import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventCoordinator;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.registration.AttendanceStatus;
import com.campussphere.entity.registration.EventRegistration;
import com.campussphere.entity.registration.InvitationStatus;
import com.campussphere.entity.registration.NotificationType;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.entity.registration.Team;
import com.campussphere.entity.registration.TeamInvitation;
import com.campussphere.exception.BusinessRuleViolationException;
import com.campussphere.repository.AcademicYearRepository;
import com.campussphere.repository.AttendanceRecordRepository;
import com.campussphere.repository.CertificateRepository;
import com.campussphere.repository.CertificateVerificationRepository;
import com.campussphere.repository.DepartmentRepository;
import com.campussphere.repository.EventCoordinatorRepository;
import com.campussphere.repository.EventRegistrationRepository;
import com.campussphere.repository.EventRepository;
import com.campussphere.repository.InAppNotificationRepository;
import com.campussphere.repository.InstitutionRepository;
import com.campussphere.repository.ProgrammeRepository;
import com.campussphere.repository.TeamInvitationRepository;
import com.campussphere.repository.TeamRepository;
import com.campussphere.service.support.CurrentUserContext;
import com.campussphere.service.support.InstitutionScopeResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final CurrentUserContext currentUserContext;
    private final InstitutionScopeResolver scopeResolver;
    private final InstitutionRepository institutionRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateVerificationRepository verificationRepository;
    private final TeamRepository teamRepository;
    private final EventCoordinatorRepository coordinatorRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final InAppNotificationRepository notificationRepository;
    private final TeamInvitationRepository invitationRepository;
    private final ProgrammeRepository programmeRepository;

    public AnalyticsService(CurrentUserContext currentUserContext,
                            InstitutionScopeResolver scopeResolver,
                            InstitutionRepository institutionRepository,
                            EventRepository eventRepository,
                            EventRegistrationRepository registrationRepository,
                            AttendanceRecordRepository attendanceRecordRepository,
                            CertificateRepository certificateRepository,
                            CertificateVerificationRepository verificationRepository,
                            TeamRepository teamRepository,
                            EventCoordinatorRepository coordinatorRepository,
                            DepartmentRepository departmentRepository,
                            AcademicYearRepository academicYearRepository,
                            InAppNotificationRepository notificationRepository,
                            TeamInvitationRepository invitationRepository,
                            ProgrammeRepository programmeRepository) {
        this.currentUserContext = currentUserContext;
        this.scopeResolver = scopeResolver;
        this.institutionRepository = institutionRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.certificateRepository = certificateRepository;
        this.verificationRepository = verificationRepository;
        this.teamRepository = teamRepository;
        this.coordinatorRepository = coordinatorRepository;
        this.departmentRepository = departmentRepository;
        this.academicYearRepository = academicYearRepository;
        this.notificationRepository = notificationRepository;
        this.invitationRepository = invitationRepository;
        this.programmeRepository = programmeRepository;
    }

    public AnalyticsOverviewResponse getOverview(String email, Long institutionId) {
        User user = requireCurrentUser(email);
        if (currentUserContext.isStudent(user)) {
            return buildStudentOverview(user);
        }
        if (currentUserContext.isFaculty(user)) {
            return buildCoordinatorOverview(user);
        }
        return buildAdminOverview(user, institutionId);
    }

    public AnalyticsOverviewResponse getStudentInsights(String email) {
        User user = requireCurrentUser(email);
        if (!currentUserContext.isStudent(user)) {
            throw new AccessDeniedException("This insight is available to students only.");
        }
        return buildStudentOverview(user);
    }

    public AnalyticsOverviewResponse getCoordinatorInsights(String email) {
        User user = requireCurrentUser(email);
        if (!currentUserContext.isFaculty(user)) {
            throw new AccessDeniedException("This insight is available to coordinators only.");
        }
        return buildCoordinatorOverview(user);
    }

    public com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsEventRowResponse> getEvents(String email, AnalyticsQuery query) {
        User user = requireCurrentUser(email);
        ScopeContext scope = resolveScope(user, query.institutionId());
        if (currentUserContext.isStudent(user)) {
            throw new AccessDeniedException("Students cannot access event analytics.");
        }

        List<Event> events = accessibleEvents(user, scope).stream()
                .filter(event -> matchesEvent(event, query))
                .toList();
        Map<Long, List<EventRegistration>> registrationsByEvent = registrationsByEvent(events, scope, query);
        Map<Long, List<AttendanceRecord>> attendanceByEvent = attendanceByEvent(events, scope, query);
        Map<Long, List<Certificate>> certificatesByEvent = certificatesByEvent(events, scope, query);

        List<AnalyticsEventRowResponse> rows = events.stream()
                .map(event -> toEventRow(event,
                        registrationsByEvent.getOrDefault(event.getId(), List.of()),
                        attendanceByEvent.getOrDefault(event.getId(), List.of()),
                        certificatesByEvent.getOrDefault(event.getId(), List.of())))
                .sorted(eventRowComparator(query.sort(), query.direction()))
                .toList();

        List<AnalyticsMetricResponse> metrics = eventMetrics(rows, scope.institution(), query);
        List<AnalyticsTrendPointResponse> trend = buildTrendPoints(
                registrationsByEvent.values().stream().flatMap(List::stream).map(EventRegistration::getRegistrationDate).toList(),
                query.startDate(),
                query.endDate()
        );
        List<AnalyticsDistributionPointResponse> distribution = buildRegistrationStatusDistribution(
                registrationsByEvent.values().stream().flatMap(List::stream).toList()
        );

        return new com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<>(
                page(rows, query.page(), query.size()),
                metrics,
                trend,
                distribution,
                eventInsights(rows),
                activityFromEvents(rows),
                rows.isEmpty() ? "Not enough activity yet to calculate this insight." : null
        );
    }

    public com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsRegistrationRowResponse> getRegistrations(String email, AnalyticsQuery query) {
        User user = requireCurrentUser(email);
        ScopeContext scope = resolveScope(user, query.institutionId());
        if (currentUserContext.isStudent(user)) {
            throw new AccessDeniedException("Students can only view their own registration insights through the personal overview.");
        }

        List<EventRegistration> registrations = registrationsForScope(user, scope).stream()
                .filter(registration -> matchesRegistration(registration, query))
                .toList();
        List<AnalyticsRegistrationRowResponse> rows = registrations.stream()
                .map(this::toRegistrationRow)
                .sorted(registrationRowComparator(query.sort(), query.direction()))
                .toList();

        return new com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<>(
                page(rows, query.page(), query.size()),
                registrationMetrics(registrations),
                buildTrendPoints(registrations.stream().map(EventRegistration::getRegistrationDate).toList(), query.startDate(), query.endDate()),
                buildRegistrationStatusDistribution(registrations),
                registrationInsights(registrations, scope),
                activityFromRegistrations(registrations),
                rows.isEmpty() ? "Not enough activity yet to calculate this insight." : null
        );
    }

    public com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsAttendanceRowResponse> getAttendance(String email, AnalyticsQuery query) {
        User user = requireCurrentUser(email);
        ScopeContext scope = resolveScope(user, query.institutionId());
        if (currentUserContext.isStudent(user)) {
            throw new AccessDeniedException("Students can only view their own attendance overview.");
        }

        List<AttendanceRecord> records = attendanceForScope(user, scope).stream()
                .filter(record -> matchesAttendance(record, query))
                .toList();
        List<AnalyticsAttendanceRowResponse> rows = records.stream()
                .map(this::toAttendanceRow)
                .sorted(attendanceRowComparator(query.sort(), query.direction()))
                .toList();

        return new com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<>(
                page(rows, query.page(), query.size()),
                attendanceMetrics(records),
                buildTrendPoints(records.stream().map(AttendanceRecord::getCheckInTime).toList(), query.startDate(), query.endDate()),
                buildAttendanceDistribution(records),
                attendanceInsights(records, scope),
                activityFromAttendance(records),
                rows.isEmpty() ? "Not enough activity yet to calculate this insight." : null
        );
    }

    public com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsCertificateRowResponse> getCertificates(String email, AnalyticsQuery query) {
        User user = requireCurrentUser(email);
        ScopeContext scope = resolveScope(user, query.institutionId());
        if (currentUserContext.isStudent(user)) {
            throw new AccessDeniedException("Students can only view their own certificate overview.");
        }

        List<Certificate> certificates = certificatesForScope(user, scope).stream()
                .filter(certificate -> matchesCertificate(certificate, query))
                .toList();
        List<AnalyticsCertificateRowResponse> rows = certificates.stream()
                .map(this::toCertificateRow)
                .sorted(certificateRowComparator(query.sort(), query.direction()))
                .toList();

        return new com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<>(
                page(rows, query.page(), query.size()),
                certificateMetrics(certificates),
                buildTrendPoints(certificates.stream()
                        .map(certificate -> certificate.getGeneratedAt() == null
                                ? null
                                : certificate.getGeneratedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                        .toList(), query.startDate(), query.endDate()),
                buildCertificateDistribution(certificates),
                certificateInsights(certificates, scope),
                activityFromCertificates(certificates),
                rows.isEmpty() ? "Not enough activity yet to calculate this insight." : null
        );
    }

    public com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<AnalyticsDepartmentRowResponse> getDepartments(String email, AnalyticsQuery query) {
        User user = requireCurrentUser(email);
        ScopeContext scope = resolveScope(user, query.institutionId());
        if (currentUserContext.isStudent(user)) {
            throw new AccessDeniedException("Students cannot access department analytics.");
        }

        List<Department> departments = departmentRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId()).stream()
                .filter(department -> matchesDepartmentFilter(department, query))
                .toList();
        List<Event> events = accessibleEvents(user, scope);
        List<EventRegistration> registrations = registrationsForScope(user, scope);
        List<AttendanceRecord> attendanceRecords = attendanceForScope(user, scope);
        List<Certificate> certificates = certificatesForScope(user, scope);

        List<AnalyticsDepartmentRowResponse> rows = departments.stream()
                .map(department -> toDepartmentRow(department, events, registrations, attendanceRecords, certificates))
                .sorted(departmentRowComparator(query.sort(), query.direction()))
                .toList();

        List<AnalyticsMetricResponse> metrics = List.of(
                metric("active-departments", "Active departments", rows.stream().filter(row -> row.eventsOrganized() > 0 || row.registrations() > 0).count(), "Departments with visible activity.", "neutral", null),
                metric("department-registrations", "Department registrations", rows.stream().mapToLong(AnalyticsDepartmentRowResponse::registrations).sum(), "Participant registrations grouped by department.", "neutral", null),
                metric("department-certificates", "Department certificates", rows.stream().mapToLong(AnalyticsDepartmentRowResponse::certificatesIssued).sum(), "Certificates linked to participants in each department.", "neutral", null)
        );
        return new com.campussphere.dto.analytics.AnalyticsDtos.AnalyticsReportResponse<>(
                page(rows, query.page(), query.size()),
                metrics,
                buildTrendPoints(registrations.stream().map(EventRegistration::getRegistrationDate).toList(), query.startDate(), query.endDate()),
                buildDepartmentDistribution(rows),
                departmentInsights(scope, rows),
                activityFromDepartments(rows),
                rows.isEmpty() ? "Not enough activity yet to calculate this insight." : null
        );
    }

    public AnalyticsExportResponse exportReport(String email, String reportType, AnalyticsQuery query) {
        User user = requireCurrentUser(email);
        ScopeContext scope = resolveScope(user, query.institutionId());
        String normalized = reportType == null ? "" : reportType.trim().toUpperCase(Locale.ROOT);
        String csv;
        String fileName;
        switch (normalized) {
            case "EVENTS", "EVENT_PERFORMANCE" -> {
                var report = getEvents(email, query);
                csv = buildEventCsv(report.page().getContent());
                fileName = "campussphere-event-performance.csv";
            }
            case "REGISTRATIONS" -> {
                var report = getRegistrations(email, query);
                csv = buildRegistrationCsv(report.page().getContent());
                fileName = "campussphere-registrations.csv";
            }
            case "ATTENDANCE" -> {
                var report = getAttendance(email, query);
                csv = buildAttendanceCsv(report.page().getContent());
                fileName = "campussphere-attendance.csv";
            }
            case "CERTIFICATES" -> {
                var report = getCertificates(email, query);
                csv = buildCertificateCsv(report.page().getContent());
                fileName = "campussphere-certificates.csv";
            }
            case "DEPARTMENTS" -> {
                var report = getDepartments(email, query);
                csv = buildDepartmentCsv(report.page().getContent());
                fileName = "campussphere-departments.csv";
            }
            default -> throw new BusinessRuleViolationException("Unsupported analytics export type.");
        }
        return new AnalyticsExportResponse(fileName, csv);
    }

    private AnalyticsOverviewResponse buildAdminOverview(User user, Long institutionId) {
        ScopeContext scope = resolveScope(user, institutionId);
        List<Event> events = accessibleEvents(user, scope);
        List<EventRegistration> registrations = registrationsForScope(user, scope);
        List<AttendanceRecord> attendanceRecords = attendanceForScope(user, scope);
        List<Certificate> certificates = certificatesForScope(user, scope);
        List<Team> teams = teamRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId());
        List<Department> departments = departmentRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId());
        List<AcademicYear> years = academicYearRepository.findByInstitution_IdAndDeletedFalseOrderByStartDateDesc(scope.institution().getId());

        Map<Long, Long> registrationCounts = registrations.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), LinkedHashMap::new, Collectors.counting()));
        Map<Long, Long> attendanceCounts = attendanceRecords.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), LinkedHashMap::new, Collectors.counting()));
        Map<Long, Long> presentCounts = attendanceRecords.stream().filter(r -> r.getAttendanceStatus() == AttendanceStatus.PRESENT).collect(Collectors.groupingBy(r -> r.getEvent().getId(), LinkedHashMap::new, Collectors.counting()));
        Map<Long, Long> certificateCounts = certificates.stream().collect(Collectors.groupingBy(c -> c.getEvent().getId(), LinkedHashMap::new, Collectors.counting()));
        Map<Long, List<EventRegistration>> registrationsByEvent = registrations.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<AttendanceRecord>> attendanceByEvent = attendanceRecords.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<Certificate>> certificatesByEvent = certificates.stream().collect(Collectors.groupingBy(c -> c.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));

        List<AnalyticsEventRowResponse> topItems = events.stream()
                .map(event -> toEventRow(event,
                        registrationsByEvent.getOrDefault(event.getId(), List.of()),
                        attendanceByEvent.getOrDefault(event.getId(), List.of()),
                        certificatesByEvent.getOrDefault(event.getId(), List.of())))
                .sorted(Comparator.comparingLong(AnalyticsEventRowResponse::registrations).reversed())
                .limit(5)
                .toList();

        List<AnalyticsMetricResponse> metrics = adminMetrics(scope, events, registrations, attendanceRecords, certificates, teams, departments, years);
        return new AnalyticsOverviewResponse(
                roleCode(user),
                roleLabel(user),
                scope.institution().getInstitutionName(),
                Instant.now(),
                metrics,
                buildTrendPoints(registrations.stream().map(EventRegistration::getRegistrationDate).toList(), null, null),
                buildRegistrationStatusDistribution(registrations),
                topItems,
                adminInsights(scope, events, registrations, attendanceRecords, certificates),
                activityFromOverview(events, registrations, attendanceRecords, certificates),
                events.isEmpty() ? "Not enough activity yet to calculate this insight." : null
        );
    }

    private AnalyticsOverviewResponse buildCoordinatorOverview(User user) {
        ScopeContext scope = resolveScope(user, user.getInstitution() == null ? null : user.getInstitution().getId());
        Set<Long> managedEventIds = coordinatorRepository.findByUser_IdAndDeletedFalse(user.getId()).stream()
                .filter(coordinator -> coordinator.getEvent() != null && !coordinator.getEvent().isDeleted())
                .filter(coordinator -> scope.institution() == null || Objects.equals(coordinator.getEvent().getInstitution().getId(), scope.institution().getId()))
                .map(coordinator -> coordinator.getEvent().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Event> events = accessibleEvents(user, scope).stream()
                .filter(event -> managedEventIds.contains(event.getId()))
                .toList();
        List<EventRegistration> registrations = registrationsForEvents(events);
        List<AttendanceRecord> attendanceRecords = attendanceForEvents(events);
        List<Certificate> certificates = certificatesForEvents(events);
        List<Team> teams = teamRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId()).stream()
                .filter(team -> managedEventIds.contains(team.getEvent().getId()))
                .toList();

        Map<Long, List<EventRegistration>> registrationsByEvent = registrations.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<AttendanceRecord>> attendanceByEvent = attendanceRecords.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<Certificate>> certificatesByEvent = certificates.stream().collect(Collectors.groupingBy(c -> c.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));

        List<AnalyticsEventRowResponse> topItems = events.stream()
                .map(event -> toEventRow(event,
                        registrationsByEvent.getOrDefault(event.getId(), List.of()),
                        attendanceByEvent.getOrDefault(event.getId(), List.of()),
                        certificatesByEvent.getOrDefault(event.getId(), List.of())))
                .sorted(Comparator.comparingLong(AnalyticsEventRowResponse::registrations).reversed())
                .limit(5)
                .toList();

        List<AnalyticsMetricResponse> metrics = List.of(
                metric("managed-events", "Managed events", events.size(), "Events assigned to your account.", "neutral", null),
                metric("upcoming-managed-events", "Upcoming managed events", events.stream().filter(this::isUpcomingEvent).count(), "Managed events that have not started yet.", "neutral", null),
                metric("managed-registrations", "Registrations", registrations.size(), "Participants across your events.", "neutral", null),
                metric("managed-attendance", "Attendance records", attendanceRecords.size(), "Recorded scans and manual check-ins.", "neutral", null),
                metric("managed-certificates", "Certificates", certificates.size(), "Certificates connected to your events.", "neutral", null)
        );

        return new AnalyticsOverviewResponse(
                roleCode(user),
                roleLabel(user),
                scope.institution().getInstitutionName(),
                Instant.now(),
                metrics,
                buildTrendPoints(registrations.stream().map(EventRegistration::getRegistrationDate).toList(), null, null),
                buildRegistrationStatusDistribution(registrations),
                topItems,
                coordinatorInsights(events, registrations, attendanceRecords),
                activityFromOverview(events, registrations, attendanceRecords, certificates),
                events.isEmpty() ? "No managed events are assigned yet." : null
        );
    }

    private AnalyticsOverviewResponse buildStudentOverview(User user) {
        List<EventRegistration> registrations = registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId());
        List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByParticipant_IdAndDeletedFalseOrderByCheckInTimeDesc(user.getId());
        List<Certificate> certificates = certificateRepository.findByRecipientUser_IdAndDeletedFalseOrderByGeneratedAtDesc(user.getId());
        List<TeamInvitation> invitations = invitationRepository.findByInvitedUser_IdAndDeletedFalseOrderByInvitedAtDesc(user.getId());
        List<Team> teams = teamRepository.findByInstitution_IdAndDeletedFalse(user.getInstitution() == null ? -1L : user.getInstitution().getId()).stream()
                .filter(team -> team.getLeader() != null && Objects.equals(team.getLeader().getId(), user.getId())
                        || team.getId() != null && team.getId() > 0 && teamRepository.findByEvent_IdAndDeletedFalse(team.getEvent().getId()).stream().anyMatch(t -> Objects.equals(t.getLeader().getId(), user.getId())))
                .toList();

        List<AnalyticsEventRowResponse> topItems = registrations.stream()
                .map(EventRegistration::getEvent)
                .distinct()
                .limit(5)
                .map(event -> toEventRow(event,
                        registrations.stream().filter(reg -> Objects.equals(reg.getEvent().getId(), event.getId())).toList(),
                        attendanceRecords.stream().filter(record -> Objects.equals(record.getEvent().getId(), event.getId())).toList(),
                        certificates.stream().filter(certificate -> Objects.equals(certificate.getEvent().getId(), event.getId())).toList()))
                .toList();

        List<AnalyticsMetricResponse> metrics = List.of(
                metric("my-registrations", "Events registered", registrations.size(), "Events you have joined.", "neutral", null),
                metric("my-upcoming", "Upcoming events", registrations.stream().map(EventRegistration::getEvent).filter(this::isUpcomingEvent).count(), "Registered events that have not started yet.", "neutral", null),
                metric("my-completed", "Completed events", registrations.stream().map(EventRegistration::getEvent).filter(event -> event.getEventStatus() == EventStatus.COMPLETED).count(), "Events already finished.", "neutral", null),
                metric("my-attendance", "Attendance records", attendanceRecords.size(), "Attendance entries linked to your account.", "neutral", null),
                metric("my-certificates", "Certificates", certificates.size(), "Certificates issued to your profile.", "neutral", null),
                metric("my-invitations", "Pending invitations", invitations.stream().filter(invitation -> invitation.getInvitationStatus() == InvitationStatus.PENDING).count(), "Active team invitations.", "neutral", null)
        );

        return new AnalyticsOverviewResponse(
                roleCode(user),
                roleLabel(user),
                user.getInstitution() == null ? "Your activity" : user.getInstitution().getInstitutionName(),
                Instant.now(),
                metrics,
                buildTrendPoints(registrations.stream().map(EventRegistration::getRegistrationDate).toList(), null, null),
                buildRegistrationStatusDistribution(registrations),
                topItems,
                studentInsights(registrations, attendanceRecords, certificates, invitations),
                activityFromStudent(registrations, attendanceRecords, certificates, invitations),
                registrations.isEmpty() ? "You have not registered for any events yet." : null
        );
    }

    private ScopeContext resolveScope(User user, Long requestedInstitutionId) {
        Institution institution = scopeResolver.resolveForRead(user, requestedInstitutionId);
        if (institution == null) {
            institution = user.getInstitution();
        }
        if (institution == null) {
            throw new AccessDeniedException("Your account is not assigned to an institution.");
        }
        return new ScopeContext(user, institution);
    }

    private List<Event> accessibleEvents(User user, ScopeContext scope) {
        if (currentUserContext.isStudent(user)) {
            return registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId()).stream()
                    .map(EventRegistration::getEvent)
                    .filter(event -> event != null && !event.isDeleted())
                    .distinct()
                    .toList();
        }
        if (currentUserContext.isFaculty(user)) {
            Set<Long> managedEventIds = coordinatorRepository.findByUser_IdAndDeletedFalse(user.getId()).stream()
                    .map(EventCoordinator::getEvent)
                    .filter(Objects::nonNull)
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            return eventRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId()).stream()
                    .filter(event -> managedEventIds.contains(event.getId()))
                    .toList();
        }
        return eventRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId());
    }

    private List<EventRegistration> registrationsForScope(User user, ScopeContext scope) {
        if (currentUserContext.isStudent(user)) {
            return registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(user.getId());
        }
        if (currentUserContext.isFaculty(user)) {
            Set<Long> managedEventIds = coordinatorRepository.findByUser_IdAndDeletedFalse(user.getId()).stream()
                    .map(EventCoordinator::getEvent)
                    .filter(Objects::nonNull)
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            return registrationRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId()).stream()
                    .filter(registration -> managedEventIds.contains(registration.getEvent().getId()))
                    .toList();
        }
        return registrationRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId());
    }

    private List<EventRegistration> registrationsForEvents(List<Event> events) {
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        if (eventIds.isEmpty()) {
            return List.of();
        }
        return registrationRepository.findAll().stream()
                .filter(registration -> !registration.isDeleted() && registration.getEvent() != null && eventIds.contains(registration.getEvent().getId()))
                .toList();
    }

    private List<AttendanceRecord> attendanceForScope(User user, ScopeContext scope) {
        if (currentUserContext.isStudent(user)) {
            return attendanceRecordRepository.findByParticipant_IdAndDeletedFalseOrderByCheckInTimeDesc(user.getId());
        }
        if (currentUserContext.isFaculty(user)) {
            Set<Long> managedEventIds = coordinatorRepository.findByUser_IdAndDeletedFalse(user.getId()).stream()
                    .map(EventCoordinator::getEvent)
                    .filter(Objects::nonNull)
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            return attendanceRecordRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId()).stream()
                    .filter(record -> managedEventIds.contains(record.getEvent().getId()))
                    .toList();
        }
        return attendanceRecordRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId());
    }

    private List<AttendanceRecord> attendanceForEvents(List<Event> events) {
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        if (eventIds.isEmpty()) {
            return List.of();
        }
        return attendanceRecordRepository.findAll().stream()
                .filter(record -> !record.isDeleted() && record.getEvent() != null && eventIds.contains(record.getEvent().getId()))
                .toList();
    }

    private List<Certificate> certificatesForScope(User user, ScopeContext scope) {
        if (currentUserContext.isStudent(user)) {
            return certificateRepository.findByRecipientUser_IdAndDeletedFalseOrderByGeneratedAtDesc(user.getId());
        }
        if (currentUserContext.isFaculty(user)) {
            Set<Long> managedEventIds = coordinatorRepository.findByUser_IdAndDeletedFalse(user.getId()).stream()
                    .map(EventCoordinator::getEvent)
                    .filter(Objects::nonNull)
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            return certificateRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId()).stream()
                    .filter(certificate -> managedEventIds.contains(certificate.getEvent().getId()))
                    .toList();
        }
        return certificateRepository.findByInstitution_IdAndDeletedFalse(scope.institution().getId());
    }

    private List<Certificate> certificatesForEvents(List<Event> events) {
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        if (eventIds.isEmpty()) {
            return List.of();
        }
        return certificateRepository.findAll().stream()
                .filter(certificate -> !certificate.isDeleted() && certificate.getEvent() != null && eventIds.contains(certificate.getEvent().getId()))
                .toList();
    }

    private AnalyticsEventRowResponse toEventRow(Event event, List<EventRegistration> registrations, List<AttendanceRecord> attendanceRecords, List<Certificate> certificates) {
        long registrationCount = registrations.size();
        long confirmed = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.APPROVED).count();
        long waitlisted = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.WAITLISTED).count();
        long cancelled = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.CANCELLED).count();
        long attendanceCount = attendanceRecords.size();
        long present = attendanceRecords.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.PRESENT).count();
        long certificateCount = certificates.size();
        int capacity = event.getMaximumParticipants() == null ? 0 : event.getMaximumParticipants();
        int utilization = capacity <= 0 ? 0 : (int) Math.min(100, Math.round(registrationCount * 100.0 / capacity));
        String health = determineEventHealth(event, registrationCount, utilization);
        return new AnalyticsEventRowResponse(
                event.getId(),
                event.getEventCode(),
                event.getTitle(),
                event.getEventCategory() == null ? "-" : event.getEventCategory().getCategoryName(),
                event.getEventType() == null ? "-" : event.getEventType().getTypeName(),
                event.getOrganizingDepartment() == null ? "-" : event.getOrganizingDepartment().getDepartmentName(),
                event.getEventStatus() == null ? "-" : event.getEventStatus().name(),
                event.getMode() == null ? "-" : event.getMode().name(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getMaximumParticipants(),
                registrationCount,
                confirmed,
                waitlisted,
                cancelled,
                attendanceCount,
                present,
                certificateCount,
                utilization,
                health,
                "/dashboard/events"
        );
    }

    private AnalyticsRegistrationRowResponse toRegistrationRow(EventRegistration registration) {
        return new AnalyticsRegistrationRowResponse(
                registration.getId(),
                registration.getEvent() == null ? null : registration.getEvent().getId(),
                registration.getEvent() == null ? "-" : registration.getEvent().getTitle(),
                registration.getParticipant() == null ? null : registration.getParticipant().getId(),
                displayName(registration.getParticipant()),
                registration.getRegistrationNumber(),
                registration.getRegistrationType() == null ? "-" : registration.getRegistrationType().name(),
                registration.getRegistrationStatus() == null ? "-" : registration.getRegistrationStatus().name(),
                registration.getRegistrationDate(),
                registration.getParticipant() == null ? "-" : safeText(registration.getParticipant().getDepartment()),
                registration.getParticipant() == null ? "-" : safeText(registration.getParticipant().getAcademicYear()),
                registration.getParticipant() == null ? "-" : safeText(registration.getParticipant().getSection()),
                registration.getWaitlistPosition(),
                "/dashboard/registrations"
        );
    }

    private AnalyticsAttendanceRowResponse toAttendanceRow(AttendanceRecord record) {
        return new AnalyticsAttendanceRowResponse(
                record.getId(),
                record.getEvent() == null ? null : record.getEvent().getId(),
                record.getEvent() == null ? "-" : record.getEvent().getTitle(),
                record.getAttendanceSession() == null ? null : record.getAttendanceSession().getId(),
                record.getAttendanceSession() == null ? "-" : record.getAttendanceSession().getSessionTitle(),
                record.getRegistration() == null ? null : record.getRegistration().getId(),
                record.getRegistration() == null ? "-" : record.getRegistration().getRegistrationNumber(),
                displayName(record.getParticipant()),
                record.getAttendanceStatus() == null ? "-" : record.getAttendanceStatus().name(),
                record.getAttendanceMethod() == null ? "-" : record.getAttendanceMethod().name(),
                record.getCheckInTime(),
                record.getCheckedInBy() == null ? "-" : displayName(record.getCheckedInBy()),
                "/dashboard/attendance"
        );
    }

    private AnalyticsCertificateRowResponse toCertificateRow(Certificate certificate) {
        return new AnalyticsCertificateRowResponse(
                certificate.getId(),
                certificate.getEvent() == null ? null : certificate.getEvent().getId(),
                certificate.getEvent() == null ? "-" : certificate.getEvent().getTitle(),
                certificate.getRecipientUser() == null ? null : certificate.getRecipientUser().getId(),
                safeText(certificate.getRecipientName()),
                safeText(certificate.getCertificateNumber()),
                certificate.getCertificateType() == null ? "-" : certificate.getCertificateType().name(),
                certificate.getCertificateStatus() == null ? "-" : certificate.getCertificateStatus().name(),
                certificate.getVerificationStatus() == null ? "-" : certificate.getVerificationStatus().name(),
                certificate.isRevoked(),
                certificate.getGeneratedAt(),
                certificate.getAttendancePercentage(),
                "/dashboard/certificates"
        );
    }

    private AnalyticsDepartmentRowResponse toDepartmentRow(Department department,
                                                           List<Event> events,
                                                           List<EventRegistration> registrations,
                                                           List<AttendanceRecord> attendanceRecords,
                                                           List<Certificate> certificates) {
        long organized = events.stream().filter(event -> event.getOrganizingDepartment() != null && Objects.equals(event.getOrganizingDepartment().getId(), department.getId())).count();
        List<EventRegistration> departmentRegistrations = registrations.stream()
                .filter(registration -> registration.getParticipant() != null && matchesDepartment(registration.getParticipant().getDepartment(), department))
                .toList();
        long participants = departmentRegistrations.stream()
                .map(EventRegistration::getParticipant)
                .filter(Objects::nonNull)
                .map(User::getId)
                .distinct()
                .count();
        long attendanceCount = attendanceRecords.stream()
                .filter(record -> record.getParticipant() != null && matchesDepartment(record.getParticipant().getDepartment(), department))
                .count();
        long certificatesIssued = certificates.stream()
                .filter(certificate -> certificate.getRecipientUser() != null && matchesDepartment(certificate.getRecipientUser().getDepartment(), department))
                .count();
        int attendanceRate = departmentRegistrations.isEmpty() ? 0 : (int) Math.round((attendanceCount * 100.0) / Math.max(1L, departmentRegistrations.size()));
        return new AnalyticsDepartmentRowResponse(
                department.getId(),
                department.getDepartmentCode(),
                department.getDepartmentName(),
                organized,
                departmentRegistrations.size(),
                participants,
                attendanceCount,
                attendanceRate,
                certificatesIssued,
                "/dashboard/institution-setup/departments"
        );
    }

    private List<AnalyticsMetricResponse> adminMetrics(ScopeContext scope,
                                                       List<Event> events,
                                                       List<EventRegistration> registrations,
                                                       List<AttendanceRecord> attendanceRecords,
                                                       List<Certificate> certificates,
                                                       List<Team> teams,
                                                       List<Department> departments,
                                                       List<AcademicYear> academicYears) {
        long currentMonthEvents = events.stream().filter(this::isCurrentMonth).count();
        long currentMonthRegistrations = registrations.stream().filter(registration -> isCurrentMonth(registration.getRegistrationDate())).count();
        long uniqueParticipants = registrations.stream().map(EventRegistration::getParticipant).filter(Objects::nonNull).map(User::getId).distinct().count();
        long confirmed = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.APPROVED).count();
        long waitlisted = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.WAITLISTED).count();
        long cancelled = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.CANCELLED).count();
        long present = attendanceRecords.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.PRESENT).count();
        long revoked = certificates.stream().filter(Certificate::isRevoked).count();
        long verified = certificates.stream().filter(certificate -> certificate.getVerificationStatus() == CertificateVerificationStatus.VERIFIED).count();
        long verifications = certificates.stream()
                .mapToLong(certificate -> verificationRepository.findByCertificate_IdAndDeletedFalseOrderByVerifiedAtDesc(certificate.getId()).size())
                .sum();
        long liveEvents = events.stream().filter(event -> Set.of(EventStatus.PUBLISHED, EventStatus.REGISTRATION_OPEN, EventStatus.REGISTRATION_CLOSED, EventStatus.ONGOING).contains(event.getEventStatus())).count();
        long completedEvents = events.stream().filter(event -> event.getEventStatus() == EventStatus.COMPLETED).count();

        return List.of(
                metric("total-events", "Total events", events.size(), "All active and inactive events within the institution.", "neutral", null),
                metric("live-events", "Live events", liveEvents, "Published and active events that are still in flight.", "neutral", null),
                metric("upcoming-events", "Upcoming events", events.stream().filter(this::isUpcomingEvent).count(), "Events scheduled for the future.", "neutral", null),
                metric("completed-events", "Completed events", completedEvents, "Finished events that have closed.", "neutral", null),
                metric("registrations", "Total registrations", registrations.size(), "All event registrations in the institution.", "neutral", null),
                metric("confirmed-registrations", "Confirmed registrations", confirmed, "Approved registrations.", "neutral", null),
                metric("waitlisted-registrations", "Waitlisted registrations", waitlisted, "Registrations waiting for capacity.", "neutral", null),
                metric("cancelled-registrations", "Cancelled registrations", cancelled, "Cancelled registrations.", "neutral", null),
                metric("unique-participants", "Unique participants", uniqueParticipants, "Distinct students and staff who registered.", "neutral", null),
                metric("teams", "Teams", teams.size(), "Active event teams.", "neutral", null),
                metric("attendance", "Attendance records", attendanceRecords.size(), "Recorded scans and manual check-ins.", "neutral", null),
                metric("attendance-rate", "Attendance rate", attendanceRecords.isEmpty() ? 0 : Math.round((present * 100.0) / attendanceRecords.size()), "Present records compared with all attendance records.", "neutral", null),
                metric("certificates", "Certificates issued", certificates.size(), "Certificates stored in the system.", "neutral", null),
                metric("certificates-revoked", "Certificates revoked", revoked, "Certificates marked revoked.", "neutral", null),
                metric("certificate-verifications", "Certificate verifications", verifications, "Public certificate verification lookups.", "neutral", null),
                metric("active-departments", "Active departments", departments.stream().filter(department -> department.getStatus() == RecordStatus.ACTIVE).count(), "Departments ready for administration.", "neutral", null),
                metric("academic-years", "Academic years", academicYears.size(), "Configured academic years for the institution.", "neutral", null),
                metric("events-this-month", "Events this month", currentMonthEvents, "Events starting this month.", "neutral", null),
                metric("registrations-this-month", "Registrations this month", currentMonthRegistrations, "Registrations created this month.", "neutral", null)
        );
    }

    private List<AnalyticsMetricResponse> registrationMetrics(List<EventRegistration> registrations) {
        long approved = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.APPROVED).count();
        long pending = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.PENDING).count();
        long rejected = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.REJECTED).count();
        long waitlisted = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.WAITLISTED).count();
        long cancelled = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.CANCELLED).count();
        long team = registrations.stream().filter(item -> item.getRegistrationType() != null && item.getRegistrationType().name().equals("TEAM")).count();
        long individual = registrations.size() - team;
        return List.of(
                metric("total", "Total registrations", registrations.size(), "All registrations that matched the filters.", "neutral", null),
                metric("approved", "Approved", approved, "Confirmed registrations.", "neutral", null),
                metric("pending", "Pending", pending, "Waiting for review.", "neutral", null),
                metric("rejected", "Rejected", rejected, "Registrations that were declined.", "neutral", null),
                metric("waitlisted", "Waitlisted", waitlisted, "Registrations waiting on seat availability.", "neutral", null),
                metric("cancelled", "Cancelled", cancelled, "Registrations withdrawn or cancelled.", "neutral", null),
                metric("team", "Team registrations", team, "Registrations linked to a team.", "neutral", null),
                metric("individual", "Individual registrations", individual, "Registrations completed by a single participant.", "neutral", null)
        );
    }

    private List<AnalyticsMetricResponse> attendanceMetrics(List<AttendanceRecord> attendanceRecords) {
        long present = attendanceRecords.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.PRESENT).count();
        long absent = attendanceRecords.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.ABSENT).count();
        long late = attendanceRecords.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.LATE).count();
        long excused = attendanceRecords.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.EXCUSED).count();
        long qr = attendanceRecords.stream().filter(item -> item.getAttendanceMethod() != null && item.getAttendanceMethod().name().equals("QR")).count();
        long manual = attendanceRecords.size() - qr;
        long rate = attendanceRecords.isEmpty() ? 0 : Math.round((present * 100.0) / attendanceRecords.size());
        return List.of(
                metric("total", "Attendance records", attendanceRecords.size(), "Recorded check-ins and manual updates.", "neutral", null),
                metric("present", "Present", present, "Successful check-ins.", "neutral", null),
                metric("absent", "Absent", absent, "Marked absent.", "neutral", null),
                metric("late", "Late", late, "Late check-ins.", "neutral", null),
                metric("excused", "Excused", excused, "Excused attendance entries.", "neutral", null),
                metric("attendance-rate", "Attendance rate", rate, "Present records compared with all attendance records.", "neutral", null),
                metric("qr", "QR check-ins", qr, "Scans recorded using QR validation.", "neutral", null),
                metric("manual", "Manual check-ins", manual, "Attendance entered manually.", "neutral", null)
        );
    }

    private List<AnalyticsMetricResponse> certificateMetrics(List<Certificate> certificates) {
        long revoked = certificates.stream().filter(Certificate::isRevoked).count();
        long verified = certificates.stream().filter(certificate -> certificate.getVerificationStatus() == CertificateVerificationStatus.VERIFIED).count();
        long eligible = certificates.stream().filter(certificate -> certificate.getCertificateStatus() == CertificateStatus.ISSUED).count();
        long downloaded = certificates.stream().filter(certificate -> certificate.getPdfFileName() != null).count();
        return List.of(
                metric("total", "Certificates", certificates.size(), "Certificates issued in the current scope.", "neutral", null),
                metric("eligible", "Eligible / issued", eligible, "Issued certificates available for verification.", "neutral", null),
                metric("verified", "Verified", verified, "Certificates validated through the public verifier.", "neutral", null),
                metric("revoked", "Revoked", revoked, "Revoked certificates.", "neutral", null),
                metric("downloads", "Downloads", downloaded, "Certificates with a generated PDF file.", "neutral", null)
        );
    }

    private List<AnalyticsDistributionPointResponse> buildRegistrationStatusDistribution(List<EventRegistration> registrations) {
        Map<RegistrationStatus, Long> counts = registrations.stream().collect(Collectors.groupingBy(EventRegistration::getRegistrationStatus, () -> new EnumMap<>(RegistrationStatus.class), Collectors.counting()));
        return List.of(
                distribution("Approved", counts.getOrDefault(RegistrationStatus.APPROVED, 0L), "success"),
                distribution("Pending", counts.getOrDefault(RegistrationStatus.PENDING, 0L), "neutral"),
                distribution("Waitlisted", counts.getOrDefault(RegistrationStatus.WAITLISTED, 0L), "warning"),
                distribution("Rejected", counts.getOrDefault(RegistrationStatus.REJECTED, 0L), "danger"),
                distribution("Cancelled", counts.getOrDefault(RegistrationStatus.CANCELLED, 0L), "muted")
        );
    }

    private List<AnalyticsDistributionPointResponse> buildAttendanceDistribution(List<AttendanceRecord> attendanceRecords) {
        Map<AttendanceStatus, Long> counts = attendanceRecords.stream().collect(Collectors.groupingBy(AttendanceRecord::getAttendanceStatus, () -> new EnumMap<>(AttendanceStatus.class), Collectors.counting()));
        return List.of(
                distribution("Present", counts.getOrDefault(AttendanceStatus.PRESENT, 0L), "success"),
                distribution("Absent", counts.getOrDefault(AttendanceStatus.ABSENT, 0L), "danger"),
                distribution("Late", counts.getOrDefault(AttendanceStatus.LATE, 0L), "warning"),
                distribution("Excused", counts.getOrDefault(AttendanceStatus.EXCUSED, 0L), "neutral")
        );
    }

    private List<AnalyticsDistributionPointResponse> buildCertificateDistribution(List<Certificate> certificates) {
        Map<CertificateStatus, Long> counts = certificates.stream().collect(Collectors.groupingBy(Certificate::getCertificateStatus, () -> new EnumMap<>(CertificateStatus.class), Collectors.counting()));
        return List.of(
                distribution("Issued", counts.getOrDefault(CertificateStatus.ISSUED, 0L), "success"),
                distribution("Draft", counts.getOrDefault(CertificateStatus.DRAFT, 0L), "neutral"),
                distribution("Revoked", certificates.stream().filter(Certificate::isRevoked).count(), "danger")
        );
    }

    private List<AnalyticsDistributionPointResponse> buildDepartmentDistribution(List<AnalyticsDepartmentRowResponse> rows) {
        return rows.stream()
                .limit(6)
                .map(row -> distribution(row.departmentName(), row.registrations(), "neutral"))
                .toList();
    }

    private List<AnalyticsTrendPointResponse> buildTrendPoints(List<LocalDateTime> values, String startDate, String endDate) {
        if (values.isEmpty()) {
            return List.of();
        }
        List<LocalDateTime> filtered = values.stream()
                .filter(value -> withinRange(value, startDate, endDate))
                .sorted()
                .toList();
        if (filtered.isEmpty()) {
            return List.of();
        }
        LocalDate first = filtered.getFirst().toLocalDate();
        LocalDate last = filtered.getLast().toLocalDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(first, last);
        Map<String, Long> grouped = new LinkedHashMap<>();
        if (days <= 45) {
            filtered.forEach(value -> grouped.merge(value.toLocalDate().format(DAY_LABEL), 1L, Long::sum));
        } else {
            filtered.forEach(value -> grouped.merge(YearMonth.from(value).format(MONTH_LABEL), 1L, Long::sum));
        }
        return grouped.entrySet().stream().map(entry -> new AnalyticsTrendPointResponse(entry.getKey(), entry.getValue())).toList();
    }

    private List<AnalyticsInsightResponse> eventInsights(List<AnalyticsEventRowResponse> rows) {
        if (rows.isEmpty()) {
            return List.of(analyticsInsight("No events found", "Create an event to see registrations, attendance, and certificate readiness here.", "neutral", "/dashboard/events"));
        }
        List<AnalyticsInsightResponse> insights = new ArrayList<>();
        long nearingCapacity = rows.stream().filter(row -> row.capacity() != null && row.capacity() > 0 && row.capacityUtilization() >= 80 && row.capacityUtilization() < 100).count();
        long fullEvents = rows.stream().filter(row -> row.capacity() != null && row.capacity() > 0 && row.capacityUtilization() >= 100).count();
        long missingCoordinators = rows.stream().filter(row -> row.departmentName() == null || "-".equals(row.departmentName())).count();
        insights.add(analyticsInsight("Events nearing capacity", nearingCapacity + " event(s) are at 80% or above capacity.", nearingCapacity > 0 ? "warning" : "neutral", "/dashboard/events"));
        insights.add(analyticsInsight("Full events", fullEvents + " event(s) have reached or exceeded their published capacity.", fullEvents > 0 ? "warning" : "neutral", "/dashboard/events"));
        insights.add(analyticsInsight("Department assignment", missingCoordinators + " event(s) do not currently list a department.", missingCoordinators > 0 ? "neutral" : "success", "/dashboard/events"));
        return insights;
    }

    private List<AnalyticsInsightResponse> adminInsights(ScopeContext scope,
                                                         List<Event> events,
                                                         List<EventRegistration> registrations,
                                                         List<AttendanceRecord> attendanceRecords,
                                                         List<Certificate> certificates) {
        if (events.isEmpty() && registrations.isEmpty() && attendanceRecords.isEmpty() && certificates.isEmpty()) {
            return List.of(analyticsInsight("No activity yet", "Create events and invite participants to unlock institution-wide insights.", "neutral", "/dashboard/events"));
        }
        long liveEvents = events.stream().filter(event -> Set.of(EventStatus.PUBLISHED, EventStatus.REGISTRATION_OPEN, EventStatus.REGISTRATION_CLOSED, EventStatus.ONGOING).contains(event.getEventStatus())).count();
        long upcomingEvents = events.stream().filter(this::isUpcomingEvent).count();
        long waitlisted = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.WAITLISTED).count();
        long attendanceRate = attendanceRecords.isEmpty() ? 0 : Math.round((attendanceRecords.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.PRESENT).count() * 100.0) / attendanceRecords.size());
        long issuedCertificates = certificates.stream().filter(certificate -> certificate.getCertificateStatus() == CertificateStatus.ISSUED).count();
        return List.of(
                analyticsInsight("Live events", liveEvents + " event(s) are currently published or open.", liveEvents > 0 ? "success" : "neutral", "/dashboard/events"),
                analyticsInsight("Upcoming events", upcomingEvents + " event(s) are scheduled ahead.", upcomingEvents > 0 ? "neutral" : "warning", "/dashboard/events"),
                analyticsInsight("Waitlist pressure", waitlisted + " registration(s) are waiting for a seat.", waitlisted > 0 ? "warning" : "success", "/dashboard/registrations"),
                analyticsInsight("Attendance rate", attendanceRate + "% of recorded attendance entries are marked present.", attendanceRate > 0 ? "neutral" : "warning", "/dashboard/attendance"),
                analyticsInsight("Certificates issued", issuedCertificates + " certificate(s) are available in this institution scope.", issuedCertificates > 0 ? "success" : "neutral", "/dashboard/certificates"),
                analyticsInsight("Institution scope", "All analytics remain scoped to " + scope.institution().getInstitutionName() + ".", "neutral", "/dashboard")
        );
    }

    private List<AnalyticsInsightResponse> registrationInsights(List<EventRegistration> registrations, ScopeContext scope) {
        if (registrations.isEmpty()) {
            return List.of(analyticsInsight("No registrations yet", "No registration records matched the current filters.", "neutral", "/dashboard/registrations"));
        }
        long waitlisted = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.WAITLISTED).count();
        long teamRegistrations = registrations.stream().filter(item -> item.getRegistrationType() != null && item.getRegistrationType().name().equals("TEAM")).count();
        long today = registrations.stream().filter(item -> item.getRegistrationDate() != null && item.getRegistrationDate().toLocalDate().equals(LocalDate.now())).count();
        return List.of(
                analyticsInsight("Waitlist", waitlisted + " registration(s) are waiting for a seat.", waitlisted > 0 ? "warning" : "success", "/dashboard/registrations"),
                analyticsInsight("Team registrations", teamRegistrations + " registration(s) are linked to a team.", teamRegistrations > 0 ? "neutral" : "success", "/dashboard/registrations"),
                analyticsInsight("Today's activity", today + " registration(s) were created today in " + scope.institution().getInstitutionName() + ".", today > 0 ? "neutral" : "success", "/dashboard/registrations")
        );
    }

    private List<AnalyticsInsightResponse> attendanceInsights(List<AttendanceRecord> records, ScopeContext scope) {
        if (records.isEmpty()) {
            return List.of(analyticsInsight("No attendance records yet", "Open an attendance session to start collecting check-ins.", "neutral", "/dashboard/attendance"));
        }
        long present = records.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.PRESENT).count();
        long qr = records.stream().filter(item -> item.getAttendanceMethod() != null && item.getAttendanceMethod().name().equals("QR")).count();
        long manual = records.size() - qr;
        return List.of(
                analyticsInsight("Present records", present + " record(s) are marked present.", present > 0 ? "success" : "neutral", "/dashboard/attendance"),
                analyticsInsight("QR vs manual", qr + " QR scan(s) and " + manual + " manual update(s).", "neutral", "/dashboard/attendance"),
                analyticsInsight("Institution scope", "All attendance rows stay scoped to " + scope.institution().getInstitutionName() + ".", "neutral", "/dashboard/attendance")
        );
    }

    private List<AnalyticsInsightResponse> certificateInsights(List<Certificate> certificates, ScopeContext scope) {
        if (certificates.isEmpty()) {
            return List.of(analyticsInsight("No certificates yet", "Issue certificates after attendance and completion rules are met.", "neutral", "/dashboard/certificates"));
        }
        long revoked = certificates.stream().filter(Certificate::isRevoked).count();
        long verified = certificates.stream().filter(certificate -> certificate.getVerificationStatus() == CertificateVerificationStatus.VERIFIED).count();
        return List.of(
                analyticsInsight("Verified certificates", verified + " certificate(s) have been verified publicly.", verified > 0 ? "success" : "neutral", "/dashboard/certificates"),
                analyticsInsight("Revoked certificates", revoked + " certificate(s) are marked revoked.", revoked > 0 ? "warning" : "success", "/dashboard/certificates"),
                analyticsInsight("Scope", "Certificate records stay scoped to " + scope.institution().getInstitutionName() + ".", "neutral", "/dashboard/certificates")
        );
    }

    private List<AnalyticsInsightResponse> departmentInsights(ScopeContext scope, List<AnalyticsDepartmentRowResponse> rows) {
        if (rows.isEmpty()) {
            return List.of(analyticsInsight("No department activity yet", "Department participation will appear once users register and attend events.", "neutral", "/dashboard/institution-setup/departments"));
        }
        long mostActive = rows.stream().mapToLong(AnalyticsDepartmentRowResponse::registrations).max().orElse(0L);
        return List.of(
                analyticsInsight("Most active department", mostActive > 0 ? "At least one department has " + mostActive + " registration(s)." : "Not enough activity yet to calculate this insight.", mostActive > 0 ? "neutral" : "warning", "/dashboard/institution-setup/departments"),
                analyticsInsight("Programme insight", "Programme-level participation is not stored in the current participant profile, so this view remains department-focused.", "neutral", "/dashboard/institution-setup/programmes"),
                analyticsInsight("Institution scope", "All numbers are scoped to " + scope.institution().getInstitutionName() + ".", "neutral", "/dashboard/institution-setup")
        );
    }

    private List<AnalyticsInsightResponse> studentInsights(List<EventRegistration> registrations,
                                                          List<AttendanceRecord> attendanceRecords,
                                                          List<Certificate> certificates,
                                                          List<TeamInvitation> invitations) {
        List<AnalyticsInsightResponse> insights = new ArrayList<>();
        if (registrations.isEmpty()) {
            insights.add(analyticsInsight("No registrations yet", "Register for an event to see your activity timeline here.", "neutral", "/dashboard/events"));
            return insights;
        }
        long upcoming = registrations.stream().map(EventRegistration::getEvent).filter(this::isUpcomingEvent).count();
        long waitlisted = registrations.stream().filter(item -> item.getRegistrationStatus() == RegistrationStatus.WAITLISTED).count();
        long pendingInvitations = invitations.stream().filter(item -> item.getInvitationStatus() == InvitationStatus.PENDING).count();
        insights.add(analyticsInsight("Upcoming events", upcoming + " registered event(s) are still ahead.", upcoming > 0 ? "neutral" : "success", "/dashboard/registrations"));
        insights.add(analyticsInsight("Waitlist", waitlisted + " registration(s) are on a waitlist.", waitlisted > 0 ? "warning" : "success", "/dashboard/registrations"));
        insights.add(analyticsInsight("Pending invitations", pendingInvitations + " team invitation(s) still need a response.", pendingInvitations > 0 ? "neutral" : "success", "/dashboard/registrations"));
        insights.add(analyticsInsight("Certificates", certificates.size() + " certificate(s) issued to your profile.", certificates.isEmpty() ? "neutral" : "success", "/dashboard/certificates"));
        insights.add(analyticsInsight("Attendance", attendanceRecords.size() + " attendance record(s) are linked to your account.", attendanceRecords.isEmpty() ? "neutral" : "success", "/dashboard/attendance/history"));
        return insights;
    }

    private List<AnalyticsInsightResponse> coordinatorInsights(List<Event> events,
                                                               List<EventRegistration> registrations,
                                                               List<AttendanceRecord> attendanceRecords) {
        if (events.isEmpty()) {
            return List.of(analyticsInsight("No managed events yet", "Your coordinator dashboard becomes active once events are assigned to your account.", "neutral", "/dashboard/events"));
        }
        long nearCapacity = events.stream().filter(event -> event.getMaximumParticipants() != null && event.getMaximumParticipants() > 0)
                .filter(event -> {
                    long count = registrations.stream().filter(item -> Objects.equals(item.getEvent().getId(), event.getId())).count();
                    long utilization = Math.round(count * 100.0 / event.getMaximumParticipants());
                    return utilization >= 80 && utilization < 100;
                }).count();
        long deadlineSoon = events.stream().filter(this::isUpcomingEvent).count();
        return List.of(
                analyticsInsight("Events nearing capacity", nearCapacity + " managed event(s) are at 80% or above capacity.", nearCapacity > 0 ? "warning" : "neutral", "/dashboard/events"),
                analyticsInsight("Upcoming managed events", deadlineSoon + " managed event(s) are still upcoming.", deadlineSoon > 0 ? "neutral" : "success", "/dashboard/events"),
                analyticsInsight("Attendance activity", attendanceRecords.size() + " attendance record(s) were found for your events.", attendanceRecords.isEmpty() ? "neutral" : "success", "/dashboard/attendance")
        );
    }

    private List<AnalyticsActivityResponse> activityFromOverview(List<Event> events,
                                                                List<EventRegistration> registrations,
                                                                List<AttendanceRecord> attendanceRecords,
                                                                List<Certificate> certificates) {
        List<AnalyticsActivityResponse> activity = new ArrayList<>();
        registrations.stream().limit(3).forEach(registration -> activity.add(new AnalyticsActivityResponse(
                registration.getId(),
                registration.getRegistrationNumber(),
                displayName(registration.getParticipant()) + " registered for " + registration.getEvent().getTitle(),
                "Registration",
                registration.getRegistrationDate() == null ? null : registration.getRegistrationDate().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                toneForRegistration(registration.getRegistrationStatus()),
                "/dashboard/registrations"
        )));
        attendanceRecords.stream().limit(3).forEach(record -> activity.add(new AnalyticsActivityResponse(
                record.getId(),
                displayName(record.getParticipant()),
                "Attendance marked for " + record.getEvent().getTitle(),
                "Attendance",
                record.getCheckInTime() == null ? null : record.getCheckInTime().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                toneForAttendance(record.getAttendanceStatus()),
                "/dashboard/attendance"
        )));
        certificates.stream().limit(3).forEach(certificate -> activity.add(new AnalyticsActivityResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getRecipientName() + " received a " + certificate.getCertificateType().name().toLowerCase(Locale.ROOT) + " certificate",
                "Certificate",
                certificate.getGeneratedAt(),
                certificate.isRevoked() ? "danger" : "success",
                "/dashboard/certificates"
        )));
        activity.sort(Comparator.comparing(AnalyticsActivityResponse::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return activity.stream().limit(8).toList();
    }

    private List<AnalyticsActivityResponse> activityFromStudent(List<EventRegistration> registrations,
                                                                List<AttendanceRecord> attendanceRecords,
                                                                List<Certificate> certificates,
                                                                List<TeamInvitation> invitations) {
        List<AnalyticsActivityResponse> activity = new ArrayList<>();
        registrations.stream().limit(4).forEach(registration -> activity.add(new AnalyticsActivityResponse(
                registration.getId(),
                registration.getRegistrationNumber(),
                "Registered for " + registration.getEvent().getTitle(),
                "Registration",
                registration.getRegistrationDate() == null ? null : registration.getRegistrationDate().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                toneForRegistration(registration.getRegistrationStatus()),
                "/dashboard/registrations/me"
        )));
        invitations.stream().limit(2).forEach(invitation -> activity.add(new AnalyticsActivityResponse(
                invitation.getId(),
                invitation.getTeam().getTeamName(),
                "Team invitation " + invitation.getInvitationStatus().name().toLowerCase(Locale.ROOT),
                "Team",
                invitation.getInvitedAt() == null ? null : invitation.getInvitedAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                invitation.getInvitationStatus() == InvitationStatus.PENDING ? "warning" : "neutral",
                "/dashboard/registrations/teams"
        )));
        attendanceRecords.stream().limit(3).forEach(record -> activity.add(new AnalyticsActivityResponse(
                record.getId(),
                record.getRegistration() == null ? "-" : record.getRegistration().getRegistrationNumber(),
                "Attendance recorded for " + record.getEvent().getTitle(),
                "Attendance",
                record.getCheckInTime() == null ? null : record.getCheckInTime().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                toneForAttendance(record.getAttendanceStatus()),
                "/dashboard/attendance/history"
        )));
        certificates.stream().limit(3).forEach(certificate -> activity.add(new AnalyticsActivityResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                "Certificate issued for " + certificate.getEvent().getTitle(),
                "Certificate",
                certificate.getGeneratedAt(),
                certificate.isRevoked() ? "danger" : "success",
                "/dashboard/certificates"
        )));
        activity.sort(Comparator.comparing(AnalyticsActivityResponse::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return activity.stream().limit(8).toList();
    }

    private List<AnalyticsActivityResponse> activityFromRegistrations(List<EventRegistration> registrations) {
        return registrations.stream().limit(8).map(registration -> new AnalyticsActivityResponse(
                registration.getId(),
                registration.getRegistrationNumber(),
                displayName(registration.getParticipant()) + " - " + registration.getEvent().getTitle(),
                "Registration",
                registration.getRegistrationDate() == null ? null : registration.getRegistrationDate().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                toneForRegistration(registration.getRegistrationStatus()),
                "/dashboard/registrations"
        )).toList();
    }

    private List<AnalyticsActivityResponse> activityFromAttendance(List<AttendanceRecord> records) {
        return records.stream().limit(8).map(record -> new AnalyticsActivityResponse(
                record.getId(),
                displayName(record.getParticipant()),
                record.getAttendanceStatus().name() + " for " + record.getEvent().getTitle(),
                "Attendance",
                record.getCheckInTime() == null ? null : record.getCheckInTime().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                toneForAttendance(record.getAttendanceStatus()),
                "/dashboard/attendance"
        )).toList();
    }

    private List<AnalyticsActivityResponse> activityFromCertificates(List<Certificate> certificates) {
        return certificates.stream().limit(8).map(certificate -> new AnalyticsActivityResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getRecipientName() + " - " + certificate.getEvent().getTitle(),
                "Certificate",
                certificate.getGeneratedAt(),
                certificate.isRevoked() ? "danger" : "success",
                "/dashboard/certificates"
        )).toList();
    }

    private List<AnalyticsActivityResponse> activityFromDepartments(List<AnalyticsDepartmentRowResponse> rows) {
        return rows.stream().limit(8).map(row -> new AnalyticsActivityResponse(
                row.departmentId(),
                row.departmentName(),
                row.registrations() + " registration(s), " + row.certificatesIssued() + " certificate(s)",
                "Department",
                Instant.now(),
                "neutral",
                "/dashboard/institution-setup/departments"
        )).toList();
    }

    private Map<Long, List<EventRegistration>> registrationsByEvent(List<Event> events, ScopeContext scope, AnalyticsQuery query) {
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        return registrationsForScope(scope.user(), scope).stream()
                .filter(registration -> eventIds.contains(registration.getEvent().getId()))
                .filter(registration -> withinRange(registration.getRegistrationDate(), query.startDate(), query.endDate()))
                .filter(registration -> query.eventId() == null || Objects.equals(registration.getEvent().getId(), query.eventId()))
                .collect(Collectors.groupingBy(registration -> registration.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, List<AttendanceRecord>> attendanceByEvent(List<Event> events, ScopeContext scope, AnalyticsQuery query) {
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        return attendanceForScope(scope.user(), scope).stream()
                .filter(record -> eventIds.contains(record.getEvent().getId()))
                .filter(record -> withinRange(record.getCheckInTime(), query.startDate(), query.endDate()))
                .filter(record -> query.eventId() == null || Objects.equals(record.getEvent().getId(), query.eventId()))
                .collect(Collectors.groupingBy(record -> record.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, List<Certificate>> certificatesByEvent(List<Event> events, ScopeContext scope, AnalyticsQuery query) {
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        return certificatesForScope(scope.user(), scope).stream()
                .filter(certificate -> eventIds.contains(certificate.getEvent().getId()))
                .filter(certificate -> withinRange(certificate.getGeneratedAt() == null ? null : certificate.getGeneratedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(), query.startDate(), query.endDate()))
                .filter(certificate -> query.eventId() == null || Objects.equals(certificate.getEvent().getId(), query.eventId()))
                .collect(Collectors.groupingBy(certificate -> certificate.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private boolean matchesEvent(Event event, AnalyticsQuery query) {
        if (query.eventId() != null && !Objects.equals(event.getId(), query.eventId())) {
            return false;
        }
        if (query.categoryId() != null && (event.getEventCategory() == null || !Objects.equals(event.getEventCategory().getId(), query.categoryId()))) {
            return false;
        }
        if (query.typeId() != null && (event.getEventType() == null || !Objects.equals(event.getEventType().getId(), query.typeId()))) {
            return false;
        }
        if (query.departmentId() != null && (event.getOrganizingDepartment() == null || !Objects.equals(event.getOrganizingDepartment().getId(), query.departmentId()))) {
            return false;
        }
        if (query.mode() != null && !query.mode().isBlank() && (event.getMode() == null || !event.getMode().name().equalsIgnoreCase(query.mode().trim()))) {
            return false;
        }
        if (query.status() != null && !query.status().isBlank() && (event.getEventStatus() == null || !event.getEventStatus().name().equalsIgnoreCase(query.status().trim()))) {
            return false;
        }
        if (!withinRange(event.getStartDateTime(), query.startDate(), query.endDate())) {
            return false;
        }
        return matchesText(event.getTitle(), query.search())
                || matchesText(event.getEventCode(), query.search())
                || matchesText(event.getShortDescription(), query.search())
                || query.search() == null || query.search().isBlank();
    }

    private boolean matchesRegistration(EventRegistration registration, AnalyticsQuery query) {
        if (query.eventId() != null && !Objects.equals(registration.getEvent().getId(), query.eventId())) {
            return false;
        }
        if (query.status() != null && !query.status().isBlank() && (registration.getRegistrationStatus() == null || !registration.getRegistrationStatus().name().equalsIgnoreCase(query.status().trim()))) {
            return false;
        }
        if (!withinRange(registration.getRegistrationDate(), query.startDate(), query.endDate())) {
            return false;
        }
        return matchesText(registration.getParticipant() == null ? null : displayName(registration.getParticipant()), query.search())
                || matchesText(registration.getRegistrationNumber(), query.search())
                || matchesText(registration.getEvent() == null ? null : registration.getEvent().getTitle(), query.search())
                || query.search() == null || query.search().isBlank();
    }

    private boolean matchesAttendance(AttendanceRecord record, AnalyticsQuery query) {
        if (query.eventId() != null && !Objects.equals(record.getEvent().getId(), query.eventId())) {
            return false;
        }
        if (query.attendanceStatus() != null && !query.attendanceStatus().isBlank() && (record.getAttendanceStatus() == null || !record.getAttendanceStatus().name().equalsIgnoreCase(query.attendanceStatus().trim()))) {
            return false;
        }
        if (!withinRange(record.getCheckInTime(), query.startDate(), query.endDate())) {
            return false;
        }
        return matchesText(record.getParticipant() == null ? null : displayName(record.getParticipant()), query.search())
                || matchesText(record.getRegistration() == null ? null : record.getRegistration().getRegistrationNumber(), query.search())
                || matchesText(record.getEvent() == null ? null : record.getEvent().getTitle(), query.search())
                || query.search() == null || query.search().isBlank();
    }

    private boolean matchesCertificate(Certificate certificate, AnalyticsQuery query) {
        if (query.eventId() != null && !Objects.equals(certificate.getEvent().getId(), query.eventId())) {
            return false;
        }
        if (query.certificateType() != null && !query.certificateType().isBlank() && (certificate.getCertificateType() == null || !certificate.getCertificateType().name().equalsIgnoreCase(query.certificateType().trim()))) {
            return false;
        }
        if (!withinRange(certificate.getGeneratedAt() == null ? null : certificate.getGeneratedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(), query.startDate(), query.endDate())) {
            return false;
        }
        return matchesText(certificate.getRecipientName(), query.search())
                || matchesText(certificate.getCertificateNumber(), query.search())
                || matchesText(certificate.getEvent() == null ? null : certificate.getEvent().getTitle(), query.search())
                || query.search() == null || query.search().isBlank();
    }

    private boolean matchesDepartmentFilter(Department department, AnalyticsQuery query) {
        if (query.departmentId() != null && !Objects.equals(department.getId(), query.departmentId())) {
            return false;
        }
        return query.search() == null || query.search().isBlank()
                || matchesText(department.getDepartmentName(), query.search())
                || matchesText(department.getDepartmentCode(), query.search())
                || matchesText(department.getShortName(), query.search());
    }

    private AnalyticsMetricResponse metric(String key, String label, long value, String detail, String tone, String deltaLabel) {
        return new AnalyticsMetricResponse(key, label, value, detail, tone, deltaLabel);
    }

    private AnalyticsDistributionPointResponse distribution(String label, long value, String tone) {
        return new AnalyticsDistributionPointResponse(label, value, tone);
    }

    private AnalyticsInsightResponse analyticsInsight(String title, String description, String tone, String targetRoute) {
        return new AnalyticsInsightResponse(title, description, tone, targetRoute);
    }

    private boolean matchesText(String value, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(search.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesDepartment(String source, Department department) {
        if (source == null || source.isBlank() || department == null) {
            return false;
        }
        String normalizedSource = source.trim().toLowerCase(Locale.ROOT);
        return normalizedSource.equalsIgnoreCase(safeText(department.getDepartmentName()).toLowerCase(Locale.ROOT))
                || normalizedSource.equalsIgnoreCase(safeText(department.getShortName()).toLowerCase(Locale.ROOT))
                || normalizedSource.equalsIgnoreCase(safeText(department.getDepartmentCode()).toLowerCase(Locale.ROOT));
    }

    private boolean withinRange(LocalDateTime value, String startDate, String endDate) {
        if (value == null) {
            return true;
        }
        if (startDate != null && !startDate.isBlank()) {
            LocalDate start = LocalDate.parse(startDate);
            if (value.isBefore(start.atStartOfDay())) {
                return false;
            }
        }
        if (endDate != null && !endDate.isBlank()) {
            LocalDate end = LocalDate.parse(endDate);
            if (value.isAfter(end.plusDays(1).atStartOfDay().minusNanos(1))) {
                return false;
            }
        }
        return true;
    }

    private boolean isCurrentMonth(Event event) {
        if (event.getStartDateTime() == null) {
            return false;
        }
        return YearMonth.from(event.getStartDateTime()).equals(YearMonth.now());
    }

    private boolean isCurrentMonth(LocalDateTime value) {
        return value != null && YearMonth.from(value).equals(YearMonth.now());
    }

    private boolean isUpcomingEvent(Event event) {
        return event.getStartDateTime() != null && event.getStartDateTime().isAfter(LocalDateTime.now())
                && event.getEventStatus() != EventStatus.CANCELLED
                && event.getEventStatus() != EventStatus.ARCHIVED;
    }

    private String determineEventHealth(Event event, long registrationCount, int utilization) {
        if (event.getEventStatus() == EventStatus.CANCELLED) {
            return "Cancelled";
        }
        if (event.getEventStatus() == EventStatus.COMPLETED) {
            return "Completed";
        }
        if (utilization >= 100) {
            return "Full";
        }
        if (utilization >= 80) {
            return "Near capacity";
        }
        if (registrationCount == 0) {
            return "Quiet";
        }
        return "Healthy";
    }

    private List<AnalyticsMetricResponse> eventMetrics(List<AnalyticsEventRowResponse> rows, Institution institution, AnalyticsQuery query) {
        long total = rows.size();
        long published = rows.stream().filter(row -> Set.of("PUBLISHED", "REGISTRATION_OPEN", "REGISTRATION_CLOSED", "ONGOING").contains(row.status())).count();
        long upcoming = rows.stream().filter(row -> row.startDateTime() != null && row.startDateTime().isAfter(LocalDateTime.now())).count();
        long completed = rows.stream().filter(row -> "COMPLETED".equals(row.status())).count();
        long nearingCapacity = rows.stream().filter(row -> row.capacityUtilization() >= 80 && row.capacityUtilization() < 100).count();
        long full = rows.stream().filter(row -> row.capacityUtilization() >= 100).count();
        return List.of(
                metric("total", "Events", total, "Events matching the current filters.", "neutral", null),
                metric("published", "Live events", published, "Published and active event records.", "neutral", null),
                metric("upcoming", "Upcoming events", upcoming, "Events scheduled in the future.", "neutral", null),
                metric("completed", "Completed events", completed, "Events that are already complete.", "neutral", null),
                metric("near-capacity", "Near capacity", nearingCapacity, "Events at 80% or above capacity.", "warning", null),
                metric("full", "Full events", full, "Events at or above capacity.", "warning", null)
        );
    }

    private List<AnalyticsActivityResponse> activityFromEvents(List<AnalyticsEventRowResponse> rows) {
        return rows.stream().limit(8).map(row -> new AnalyticsActivityResponse(
                row.eventId(),
                row.eventTitle(),
                row.registrations() + " registration(s), " + row.attendanceCount() + " attendance record(s).",
                "Event",
                row.startDateTime() == null ? Instant.now() : row.startDateTime().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                toneForHealth(row.health()),
                row.targetRoute()
        )).toList();
    }

    private String toneForHealth(String health) {
        if (health == null) {
            return "neutral";
        }
        return switch (health.toLowerCase(Locale.ROOT)) {
            case "full", "near capacity", "cancelled" -> "warning";
            case "completed", "healthy" -> "success";
            case "quiet" -> "neutral";
            default -> "neutral";
        };
    }

    private String toneForRegistration(RegistrationStatus status) {
        if (status == null) {
            return "neutral";
        }
        return switch (status) {
            case APPROVED -> "success";
            case WAITLISTED -> "warning";
            case REJECTED, CANCELLED -> "danger";
            default -> "neutral";
        };
    }

    private String toneForAttendance(AttendanceStatus status) {
        if (status == null) {
            return "neutral";
        }
        return switch (status) {
            case PRESENT -> "success";
            case LATE -> "warning";
            case ABSENT -> "danger";
            case EXCUSED -> "neutral";
            default -> "neutral";
        };
    }

    private String roleCode(User user) {
        if (currentUserContext.isFaculty(user)) {
            return RoleCode.FACULTY_COORDINATOR.name();
        }
        if (currentUserContext.isStudent(user)) {
            return RoleCode.STUDENT.name();
        }
        return RoleCode.ADMINISTRATOR.name();
    }

    private String roleLabel(User user) {
        return switch (roleCode(user)) {
            case "FACULTY_COORDINATOR" -> "Faculty Coordinator";
            case "STUDENT" -> "Student";
            default -> "Administrator";
        };
    }

    private String displayName(User user) {
        if (user == null) {
            return "-";
        }
        String first = safeText(user.getFirstName());
        String last = safeText(user.getLastName());
        String joined = (first + " " + last).trim();
        if (!joined.isBlank()) {
            return joined;
        }
        return safeText(user.getEmail());
    }

    private String safeText(String value) {
        return value == null ? "-" : value.trim();
    }

    private ScopeContext scope(User user) {
        return new ScopeContext(user, user.getInstitution());
    }

    private User requireCurrentUser(String email) {
        return currentUserContext.requireCurrentUser(email);
    }

    private <T> PageResponse<T> page(List<T> content, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size <= 0 ? 20 : size, 100));
        int from = Math.min(safePage * safeSize, content.size());
        int to = Math.min(from + safeSize, content.size());
        List<T> slice = content.subList(from, to);
        long totalElements = content.size();
        int totalPages = (int) Math.ceil(totalElements / (double) safeSize);
        return PageResponse.of(slice, safePage, safeSize, totalElements, totalPages, safePage == 0, safePage >= Math.max(0, totalPages - 1));
    }

    private Comparator<AnalyticsEventRowResponse> eventRowComparator(String sort, String direction) {
        Comparator<AnalyticsEventRowResponse> comparator = switch (normalizeSort(sort)) {
            case "title" -> Comparator.comparing(AnalyticsEventRowResponse::eventTitle, Comparator.nullsLast(String::compareToIgnoreCase));
            case "capacity" -> Comparator.comparingInt(row -> row.capacity() == null ? 0 : row.capacity());
            case "attendance" -> Comparator.comparingLong(AnalyticsEventRowResponse::attendanceCount);
            case "certificates" -> Comparator.comparingLong(AnalyticsEventRowResponse::certificateCount);
            case "status" -> Comparator.comparing(AnalyticsEventRowResponse::status, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(AnalyticsEventRowResponse::startDateTime, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if (isDescending(direction)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private Comparator<AnalyticsRegistrationRowResponse> registrationRowComparator(String sort, String direction) {
        Comparator<AnalyticsRegistrationRowResponse> comparator = switch (normalizeSort(sort)) {
            case "participant" -> Comparator.comparing(AnalyticsRegistrationRowResponse::participantName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "event" -> Comparator.comparing(AnalyticsRegistrationRowResponse::eventTitle, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(AnalyticsRegistrationRowResponse::status, Comparator.nullsLast(String::compareToIgnoreCase));
            case "waitlist" -> Comparator.comparing(row -> row.waitlistPosition() == null ? Integer.MAX_VALUE : row.waitlistPosition());
            default -> Comparator.comparing(AnalyticsRegistrationRowResponse::registrationDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return isDescending(direction) ? comparator.reversed() : comparator;
    }

    private Comparator<AnalyticsAttendanceRowResponse> attendanceRowComparator(String sort, String direction) {
        Comparator<AnalyticsAttendanceRowResponse> comparator = switch (normalizeSort(sort)) {
            case "participant" -> Comparator.comparing(AnalyticsAttendanceRowResponse::participantName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "event" -> Comparator.comparing(AnalyticsAttendanceRowResponse::eventTitle, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(AnalyticsAttendanceRowResponse::attendanceStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(AnalyticsAttendanceRowResponse::checkInTime, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return isDescending(direction) ? comparator.reversed() : comparator;
    }

    private Comparator<AnalyticsCertificateRowResponse> certificateRowComparator(String sort, String direction) {
        Comparator<AnalyticsCertificateRowResponse> comparator = switch (normalizeSort(sort)) {
            case "recipient" -> Comparator.comparing(AnalyticsCertificateRowResponse::recipientName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "event" -> Comparator.comparing(AnalyticsCertificateRowResponse::eventTitle, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(AnalyticsCertificateRowResponse::certificateStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(AnalyticsCertificateRowResponse::generatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return isDescending(direction) ? comparator.reversed() : comparator;
    }

    private Comparator<AnalyticsDepartmentRowResponse> departmentRowComparator(String sort, String direction) {
        Comparator<AnalyticsDepartmentRowResponse> comparator = switch (normalizeSort(sort)) {
            case "registrations" -> Comparator.comparingLong(AnalyticsDepartmentRowResponse::registrations);
            case "attendance" -> Comparator.comparingLong(AnalyticsDepartmentRowResponse::attendanceCount);
            case "certificates" -> Comparator.comparingLong(AnalyticsDepartmentRowResponse::certificatesIssued);
            case "name" -> Comparator.comparing(AnalyticsDepartmentRowResponse::departmentName, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparingLong(AnalyticsDepartmentRowResponse::registrations);
        };
        return isDescending(direction) ? comparator.reversed() : comparator;
    }

    private String normalizeSort(String sort) {
        return sort == null ? "" : sort.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isDescending(String direction) {
        return direction != null && direction.trim().equalsIgnoreCase("desc");
    }

    private String buildEventCsv(List<AnalyticsEventRowResponse> rows) {
        StringBuilder builder = new StringBuilder("eventCode,eventTitle,category,type,department,status,mode,registrations,confirmed,waitlisted,cancelled,attendance,present,certificates,capacity,utilization,health\n");
        for (AnalyticsEventRowResponse row : rows) {
            builder.append(csv(row.eventCode())).append(',')
                    .append(csv(row.eventTitle())).append(',')
                    .append(csv(row.categoryName())).append(',')
                    .append(csv(row.typeName())).append(',')
                    .append(csv(row.departmentName())).append(',')
                    .append(csv(row.status())).append(',')
                    .append(csv(row.mode())).append(',')
                    .append(row.registrations()).append(',')
                    .append(row.confirmedRegistrations()).append(',')
                    .append(row.waitlistedRegistrations()).append(',')
                    .append(row.cancelledRegistrations()).append(',')
                    .append(row.attendanceCount()).append(',')
                    .append(row.presentCount()).append(',')
                    .append(row.certificateCount()).append(',')
                    .append(row.capacity() == null ? 0 : row.capacity()).append(',')
                    .append(row.capacityUtilization()).append(',')
                    .append(csv(row.health()))
                    .append('\n');
        }
        return builder.toString();
    }

    private String buildRegistrationCsv(List<AnalyticsRegistrationRowResponse> rows) {
        StringBuilder builder = new StringBuilder("registrationNumber,eventTitle,participantName,registrationType,status,registrationDate,department,academicYear,section,waitlistPosition\n");
        for (AnalyticsRegistrationRowResponse row : rows) {
            builder.append(csv(row.registrationNumber())).append(',')
                    .append(csv(row.eventTitle())).append(',')
                    .append(csv(row.participantName())).append(',')
                    .append(csv(row.registrationType())).append(',')
                    .append(csv(row.status())).append(',')
                    .append(csv(row.registrationDate() == null ? null : row.registrationDate().toString())).append(',')
                    .append(csv(row.departmentName())).append(',')
                    .append(csv(row.academicYear())).append(',')
                    .append(csv(row.section())).append(',')
                    .append(row.waitlistPosition() == null ? "" : row.waitlistPosition())
                    .append('\n');
        }
        return builder.toString();
    }

    private String buildAttendanceCsv(List<AnalyticsAttendanceRowResponse> rows) {
        StringBuilder builder = new StringBuilder("registrationNumber,eventTitle,participantName,attendanceSessionTitle,attendanceStatus,attendanceMethod,checkInTime,checkedInBy\n");
        for (AnalyticsAttendanceRowResponse row : rows) {
            builder.append(csv(row.registrationNumber())).append(',')
                    .append(csv(row.eventTitle())).append(',')
                    .append(csv(row.participantName())).append(',')
                    .append(csv(row.attendanceSessionTitle())).append(',')
                    .append(csv(row.attendanceStatus())).append(',')
                    .append(csv(row.attendanceMethod())).append(',')
                    .append(csv(row.checkInTime() == null ? null : row.checkInTime().toString())).append(',')
                    .append(csv(row.checkedInBy()))
                    .append('\n');
        }
        return builder.toString();
    }

    private String buildCertificateCsv(List<AnalyticsCertificateRowResponse> rows) {
        StringBuilder builder = new StringBuilder("certificateNumber,eventTitle,recipientName,certificateType,certificateStatus,verificationStatus,revoked,generatedAt,attendancePercentage\n");
        for (AnalyticsCertificateRowResponse row : rows) {
            builder.append(csv(row.certificateNumber())).append(',')
                    .append(csv(row.eventTitle())).append(',')
                    .append(csv(row.recipientName())).append(',')
                    .append(csv(row.certificateType())).append(',')
                    .append(csv(row.certificateStatus())).append(',')
                    .append(csv(row.verificationStatus())).append(',')
                    .append(row.revoked()).append(',')
                    .append(csv(row.generatedAt() == null ? null : row.generatedAt().toString())).append(',')
                    .append(row.attendancePercentage() == null ? "" : row.attendancePercentage())
                    .append('\n');
        }
        return builder.toString();
    }

    private String buildDepartmentCsv(List<AnalyticsDepartmentRowResponse> rows) {
        StringBuilder builder = new StringBuilder("departmentCode,departmentName,eventsOrganized,registrations,uniqueParticipants,attendanceCount,attendanceRate,certificatesIssued\n");
        for (AnalyticsDepartmentRowResponse row : rows) {
            builder.append(csv(row.departmentCode())).append(',')
                    .append(csv(row.departmentName())).append(',')
                    .append(row.eventsOrganized()).append(',')
                    .append(row.registrations()).append(',')
                    .append(row.uniqueParticipants()).append(',')
                    .append(row.attendanceCount()).append(',')
                    .append(row.attendanceRate()).append(',')
                    .append(row.certificatesIssued())
                    .append('\n');
        }
        return builder.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private boolean matchesEvent(EntityIdCarrier carrier, Long id) {
        return carrier != null && Objects.equals(carrier.id(), id);
    }

    private record ScopeContext(User user, Institution institution) {
    }

    private interface EntityIdCarrier {
        Long id();
    }
}
