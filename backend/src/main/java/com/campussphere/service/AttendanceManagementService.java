package com.campussphere.service;

import com.campussphere.dto.PageResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceAuditResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceBulkRequest;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceCheckInRequest;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceDashboardResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceOperationResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceRecordResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceReportResponse;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceSessionRequest;
import com.campussphere.dto.attendance.AttendanceDtos.AttendanceSessionResponse;
import com.campussphere.dto.attendance.AttendanceDtos.ManualAttendanceRequest;
import com.campussphere.dto.attendance.AttendanceDtos.QRTokenRequest;
import com.campussphere.dto.attendance.AttendanceDtos.QRTokenResponse;
import com.campussphere.dto.attendance.AttendanceDtos.QRValidationRequest;
import com.campussphere.dto.attendance.AttendanceDtos.QRValidationResponse;
import com.campussphere.entity.AcademicYear;
import com.campussphere.entity.Institution;
import com.campussphere.entity.User;
import com.campussphere.entity.event.CoordinatorRole;
import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventRegistrationConfig;
import com.campussphere.entity.event.EventSession;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.attendance.AttendanceActionType;
import com.campussphere.entity.attendance.AttendanceAudit;
import com.campussphere.entity.attendance.AttendanceMethod;
import com.campussphere.entity.attendance.AttendanceRecord;
import com.campussphere.entity.attendance.AttendanceSession;
import com.campussphere.entity.attendance.AttendanceSessionStatus;
import com.campussphere.entity.attendance.QRToken;
import com.campussphere.entity.registration.AttendanceStatus;
import com.campussphere.entity.registration.EventRegistration;
import com.campussphere.entity.registration.NotificationType;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.entity.registration.RegistrationType;
import com.campussphere.exception.BusinessRuleViolationException;
import com.campussphere.exception.ConflictException;
import com.campussphere.exception.DuplicateResourceException;
import com.campussphere.exception.InvalidInstitutionRelationshipException;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.repository.AttendanceAuditRepository;
import com.campussphere.repository.AttendanceRecordRepository;
import com.campussphere.repository.AttendanceSessionRepository;
import com.campussphere.repository.EventCoordinatorRepository;
import com.campussphere.repository.EventRegistrationConfigRepository;
import com.campussphere.repository.EventRegistrationRepository;
import com.campussphere.repository.EventRepository;
import com.campussphere.repository.EventSessionRepository;
import com.campussphere.repository.InAppNotificationRepository;
import com.campussphere.repository.QRTokenRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.service.support.CurrentUserContext;
import com.campussphere.service.support.InstitutionScopeResolver;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceManagementService {

    private static final int DEFAULT_CERTIFICATE_THRESHOLD = 75;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceAuditRepository attendanceAuditRepository;
    private final QRTokenRepository qrTokenRepository;
    private final EventRepository eventRepository;
    private final EventSessionRepository eventSessionRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventRegistrationConfigRepository configRepository;
    private final EventCoordinatorRepository coordinatorRepository;
    private final InAppNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserContext currentUserContext;
    private final InstitutionScopeResolver scopeResolver;

    public AttendanceManagementService(
            AttendanceSessionRepository attendanceSessionRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            AttendanceAuditRepository attendanceAuditRepository,
            QRTokenRepository qrTokenRepository,
            EventRepository eventRepository,
            EventSessionRepository eventSessionRepository,
            EventRegistrationRepository registrationRepository,
            EventRegistrationConfigRepository configRepository,
            EventCoordinatorRepository coordinatorRepository,
            InAppNotificationRepository notificationRepository,
            UserRepository userRepository,
            CurrentUserContext currentUserContext,
            InstitutionScopeResolver scopeResolver
    ) {
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.attendanceAuditRepository = attendanceAuditRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.eventRepository = eventRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.registrationRepository = registrationRepository;
        this.configRepository = configRepository;
        this.coordinatorRepository = coordinatorRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.currentUserContext = currentUserContext;
        this.scopeResolver = scopeResolver;
    }

    @Transactional(readOnly = true)
    public AttendanceDashboardResponse getDashboard(String email, Long eventId) {
        User currentUser = requireCurrentUser(email);
        ScopeData scopeData = resolveScope(currentUser, eventId, true);
        List<AttendanceRecord> records = loadVisibleRecords(currentUser, scopeData);
        List<AttendanceSession> sessions = loadVisibleSessions(scopeData);
        long total = records.size();
        long present = count(records, AttendanceStatus.PRESENT);
        long absent = count(records, AttendanceStatus.ABSENT);
        long late = count(records, AttendanceStatus.LATE);
        long excused = count(records, AttendanceStatus.EXCUSED);
        long liveCheckIns = records.stream().filter(record -> record.getCheckInTime() != null && record.getCheckInTime().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusHours(24))).count();
        long attendancePercentage = total == 0 ? 0 : Math.round(((double) (present + late) * 100d) / total);
        List<AttendanceSessionResponse> upcomingSessions = sessions.stream()
                .filter(session -> session.getAttendanceSessionStatus() == AttendanceSessionStatus.OPEN || session.getOpenedAt() == null || session.getOpenedAt().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusDays(1)))
                .limit(6)
                .map(this::toSessionResponse)
                .toList();
        List<AttendanceRecordResponse> recentScans = records.stream().limit(8).map(record -> toRecordResponse(record, DEFAULT_CERTIFICATE_THRESHOLD)).toList();
        List<AttendanceRecordResponse> liveItems = records.stream()
                .filter(record -> record.getCheckInTime() != null && record.getCheckInTime().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusHours(3)))
                .limit(5)
                .map(record -> toRecordResponse(record, DEFAULT_CERTIFICATE_THRESHOLD))
                .toList();
        long certificateEligibleCount = records.stream().filter(record -> record.getRegistration().isCertificateEligible()).count();
        long certificateReadyCount = certificateEligibleCount;
        return new AttendanceDashboardResponse(
                scopeData.event != null ? registrationRepository.findByEvent_IdAndDeletedFalseOrderByRegistrationDateDesc(scopeData.event.getId()).size() : records.stream().map(record -> record.getRegistration().getId()).distinct().count(),
                present,
                absent,
                late,
                excused,
                attendancePercentage,
                liveCheckIns,
                upcomingSessions.size(),
                certificateEligibleCount,
                certificateReadyCount,
                DEFAULT_CERTIFICATE_THRESHOLD,
                upcomingSessions,
                recentScans,
                liveItems
        );
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> listAttendanceSessions(String email, Long eventId) {
        User currentUser = requireCurrentUser(email);
        ScopeData scopeData = resolveScope(currentUser, eventId, true);
        List<AttendanceSession> sessions = loadVisibleSessions(scopeData);
        return sessions.stream().map(this::toSessionResponse).toList();
    }

    public AttendanceSessionResponse openAttendanceSession(String email, AttendanceSessionRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(request.eventId());
        Institution institution = scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        ensureManagerForEvent(currentUser, event);
        EventSession eventSession = request.eventSessionId() == null ? null : requireEventSession(request.eventSessionId(), event.getId());
        validateEventCanTakeAttendance(event, eventSession);

        AttendanceSession session = new AttendanceSession();
        session.setInstitution(institution);
        session.setEvent(event);
        session.setEventSession(eventSession);
        session.setSessionTitle(trimToDefault(request.sessionTitle(), eventSession != null ? eventSession.getTitle() : event.getTitle() + " attendance"));
        session.setAttendanceSessionStatus(AttendanceSessionStatus.OPEN);
        session.setOpenedAt(now());
        session.setOpenedBy(currentUser);
        session.setRemarks(trimToNull(request.remarks()));
        attendanceSessionRepository.save(session);
        auditSession(session, currentUser, AttendanceActionType.SESSION_OPENED, null, null, "Attendance session opened.");
        return toSessionResponse(session);
    }

    public AttendanceSessionResponse closeAttendanceSession(String email, Long sessionId) {
        User currentUser = requireCurrentUser(email);
        AttendanceSession session = requireAttendanceSession(sessionId);
        ensureManagerForEvent(currentUser, session.getEvent());
        scopeResolver.resolveForWrite(currentUser, session.getInstitution().getId());
        if (session.getAttendanceSessionStatus() == AttendanceSessionStatus.CLOSED) {
            return toSessionResponse(session);
        }
        session.setAttendanceSessionStatus(AttendanceSessionStatus.CLOSED);
        session.setClosedAt(now());
        session.setClosedBy(currentUser);
        attendanceSessionRepository.save(session);
        auditSession(session, currentUser, AttendanceActionType.SESSION_CLOSED, null, null, "Attendance session closed.");
        return toSessionResponse(session);
    }

    public QRTokenResponse generateQrToken(String email, QRTokenRequest request) {
        User currentUser = requireCurrentUser(email);
        EventRegistration registration = requireRegistration(request.registrationId());
        ensureEventAccess(currentUser, registration.getEvent());
        ensureManagerForEvent(currentUser, registration.getEvent());
        QRToken token = qrTokenRepository.findByRegistration_IdAndDeletedFalse(registration.getId()).orElseGet(QRToken::new);
        String rawToken = buildRawToken(registration.getId());
        token.setInstitution(registration.getInstitution());
        token.setRegistration(registration);
        token.setTokenPrefix(rawToken.substring(0, Math.min(12, rawToken.length())));
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(now().plusMinutes(request.expiresInMinutes() == null || request.expiresInMinutes() <= 0 ? 30 : request.expiresInMinutes()));
        token.setOneTimeUse(request.oneTimeUse() == null || request.oneTimeUse());
        token.setInvalidatedAt(null);
        token.setUsedAt(null);
        token.setRegeneratedAt(token.getId() == null ? null : now());
        token.setGeneratedBy(currentUser);
        qrTokenRepository.save(token);
        auditToken(token, currentUser, AttendanceActionType.QR_GENERATED, "QR token generated.");
        return toQrResponse(token, rawToken);
    }

    @Transactional(readOnly = true)
    public QRValidationResponse validateQrToken(String email, QRValidationRequest request) {
        User currentUser = requireCurrentUser(email);
        QRToken token = requireToken(request.token());
        ensureEventAccess(currentUser, token.getRegistration().getEvent());
        if (request.eventId() != null && !Objects.equals(token.getRegistration().getEvent().getId(), request.eventId())) {
            throw new InvalidInstitutionRelationshipException("QR token does not belong to the selected event.");
        }
        AttendanceSession session = request.attendanceSessionId() == null ? null : requireAttendanceSession(request.attendanceSessionId());
        if (session != null && !Objects.equals(session.getEvent().getId(), token.getRegistration().getEvent().getId())) {
            throw new InvalidInstitutionRelationshipException("QR token cannot be used for another event.");
        }
        boolean expired = token.getExpiresAt() != null && token.getExpiresAt().isBefore(now());
        boolean used = token.getUsedAt() != null;
        boolean alreadyMarked = session != null && attendanceRecordRepository.existsByAttendanceSession_IdAndRegistration_IdAndDeletedFalse(session.getId(), token.getRegistration().getId());
        boolean valid = !expired && !used && token.getInvalidatedAt() == null && !alreadyMarked;
        String message = valid ? "QR token is valid." : expired ? "QR token has expired." : used ? "QR token has already been used." : alreadyMarked ? "Attendance was already recorded for this session." : "QR token is invalid.";
        return new QRValidationResponse(
                valid,
                message,
                token.getRegistration().getId(),
                token.getRegistration().getEvent().getId(),
                token.getRegistration().getEvent().getTitle(),
                displayName(token.getRegistration().getParticipant()),
                session == null ? null : session.getId(),
                expired,
                used,
                alreadyMarked
        );
    }

    public AttendanceOperationResponse checkIn(String email, AttendanceCheckInRequest request) {
        User currentUser = requireCurrentUser(email);
        QRToken token = requireToken(request.token());
        AttendanceSession session = requireAttendanceSession(request.attendanceSessionId());
        EventRegistration registration = token.getRegistration();
        ensureEventAccess(currentUser, registration.getEvent());
        ensureManagerForEvent(currentUser, session.getEvent());
        validateAttendanceSession(session, registration);
        validateEventTimeWindow(session.getEvent(), session);
        validateTokenUsage(token);
        if (attendanceRecordRepository.existsByAttendanceSession_IdAndRegistration_IdAndDeletedFalse(session.getId(), registration.getId())) {
            throw new DuplicateResourceException("Attendance has already been recorded for this registration.");
        }
        AttendanceRecord record = createAttendanceRecord(session, registration, request.attendanceStatus(), request.attendanceMethod(), currentUser, token, request.remarks(), request.deviceInfo(), request.ipAddress());
        token.setUsedAt(now());
        qrTokenRepository.save(token);
        attendanceRecordRepository.save(record);
        auditRecord(record, currentUser, AttendanceActionType.CHECK_IN_CREATED, null, record.getAttendanceStatus(), "Attendance recorded using QR.");
        updateRegistrationAttendanceState(registration, record.getAttendanceStatus());
        notifyParticipant(registration, record.getAttendanceStatus());
        return new AttendanceOperationResponse("Attendance recorded successfully.", toRecordResponse(record, certificateThresholdFor(registration.getEvent())), toSessionResponse(session), toQrResponse(token, null), List.of());
    }

    public AttendanceOperationResponse markManualAttendance(String email, ManualAttendanceRequest request) {
        User currentUser = requireCurrentUser(email);
        AttendanceSession session = requireAttendanceSession(request.attendanceSessionId());
        EventRegistration registration = requireRegistration(request.registrationId());
        ensureEventAccess(currentUser, registration.getEvent());
        ensureManagerForEvent(currentUser, session.getEvent());
        validateAttendanceSession(session, registration);
        validateEventTimeWindow(session.getEvent(), session);

        AttendanceRecord record = attendanceRecordRepository.findByAttendanceSession_IdAndDeletedFalseOrderByCheckInTimeDesc(session.getId()).stream()
                .filter(item -> Objects.equals(item.getRegistration().getId(), registration.getId()))
                .findFirst()
                .orElse(null);
        AttendanceStatus previousStatus = record == null ? null : record.getAttendanceStatus();
        if (record == null) {
            record = createAttendanceRecord(session, registration, request.attendanceStatus(), AttendanceMethod.MANUAL, currentUser, null, request.remarks(), request.deviceInfo(), request.ipAddress());
        } else {
            record.setAttendanceStatus(request.attendanceStatus());
            record.setAttendanceMethod(AttendanceMethod.MANUAL);
            record.setRemarks(trimToNull(request.remarks()));
            record.setDeviceInfo(trimToNull(request.deviceInfo()));
            record.setIpAddress(trimToNull(request.ipAddress()));
            record.setCheckedInBy(currentUser);
            record.setCheckInTime(now());
        }
        attendanceRecordRepository.save(record);
        auditRecord(record, currentUser, previousStatus == null ? AttendanceActionType.CHECK_IN_CREATED : AttendanceActionType.CHECK_IN_UPDATED, previousStatus, record.getAttendanceStatus(), "Manual attendance updated.");
        updateRegistrationAttendanceState(registration, record.getAttendanceStatus());
        notifyParticipant(registration, record.getAttendanceStatus());
        return new AttendanceOperationResponse("Attendance saved successfully.", toRecordResponse(record, certificateThresholdFor(registration.getEvent())), toSessionResponse(session), null, List.of());
    }

    public AttendanceOperationResponse undoAttendance(String email, Long recordId, String remarks) {
        User currentUser = requireCurrentUser(email);
        AttendanceRecord record = requireAttendanceRecord(recordId);
        ensureEventAccess(currentUser, record.getEvent());
        ensureManagerForEvent(currentUser, record.getEvent());
        AttendanceStatus previous = record.getAttendanceStatus();
        record.setAttendanceStatus(AttendanceStatus.CANCELLED);
        record.setRemarks(trimToNull(remarks));
        attendanceRecordRepository.save(record);
        auditRecord(record, currentUser, AttendanceActionType.CHECK_IN_UNDONE, previous, AttendanceStatus.CANCELLED, "Attendance was undone.");
        updateRegistrationAttendanceState(record.getRegistration(), AttendanceStatus.CANCELLED);
        notifyParticipant(record.getRegistration(), AttendanceStatus.CANCELLED);
        return new AttendanceOperationResponse("Attendance has been undone.", toRecordResponse(record, certificateThresholdFor(record.getEvent())), toSessionResponse(record.getAttendanceSession()), null, List.of());
    }

    public AttendanceOperationResponse bulkAttendance(String email, AttendanceBulkRequest request) {
        User currentUser = requireCurrentUser(email);
        AttendanceSession session = requireAttendanceSession(request.attendanceSessionId());
        ensureManagerForEvent(currentUser, session.getEvent());
        List<AttendanceRecordResponse> saved = new ArrayList<>();
        for (Long registrationId : request.registrationIds()) {
            EventRegistration registration = requireRegistration(registrationId);
            ensureEventAccess(currentUser, registration.getEvent());
            validateAttendanceSession(session, registration);
            AttendanceRecord record = attendanceRecordRepository.findByAttendanceSession_IdAndDeletedFalseOrderByCheckInTimeDesc(session.getId()).stream()
                    .filter(item -> Objects.equals(item.getRegistration().getId(), registrationId))
                    .findFirst()
                    .orElse(null);
            AttendanceStatus previousStatus = record == null ? null : record.getAttendanceStatus();
            if (record == null) {
                record = createAttendanceRecord(session, registration, request.attendanceStatus(), AttendanceMethod.MANUAL, currentUser, null, request.remarks(), request.deviceInfo(), request.ipAddress());
            } else {
                record.setAttendanceStatus(request.attendanceStatus());
                record.setAttendanceMethod(AttendanceMethod.MANUAL);
                record.setRemarks(trimToNull(request.remarks()));
                record.setCheckedInBy(currentUser);
                record.setCheckInTime(now());
            }
            attendanceRecordRepository.save(record);
            auditRecord(record, currentUser, previousStatus == null ? AttendanceActionType.CHECK_IN_CREATED : AttendanceActionType.CHECK_IN_UPDATED, previousStatus, record.getAttendanceStatus(), "Bulk attendance update.");
            updateRegistrationAttendanceState(registration, record.getAttendanceStatus());
            saved.add(toRecordResponse(record, certificateThresholdFor(registration.getEvent())));
            notifyParticipant(registration, record.getAttendanceStatus());
        }
        return new AttendanceOperationResponse("Bulk attendance update completed.", null, toSessionResponse(session), null, saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceRecordResponse> listHistory(String email, Long eventId, Long attendanceSessionId, Long registrationId, Long participantId, AttendanceStatus status, String search, int page, int size) {
        User currentUser = requireCurrentUser(email);
        ScopeData scopeData = resolveScope(currentUser, eventId, false);
        List<AttendanceRecord> records = loadVisibleRecords(currentUser, scopeData);
        if (attendanceSessionId != null) {
            records = records.stream().filter(record -> Objects.equals(record.getAttendanceSession().getId(), attendanceSessionId)).toList();
        }
        if (registrationId != null) {
            records = records.stream().filter(record -> Objects.equals(record.getRegistration().getId(), registrationId)).toList();
        }
        if (participantId != null) {
            records = records.stream().filter(record -> Objects.equals(record.getParticipant().getId(), participantId)).toList();
        }
        if (status != null) {
            records = records.stream().filter(record -> record.getAttendanceStatus() == status).toList();
        }
        if (search != null && !search.isBlank()) {
            String needle = search.toLowerCase();
            records = records.stream().filter(record ->
                    contains(record.getRegistration().getRegistrationNumber(), needle)
                            || contains(displayName(record.getParticipant()), needle)
                            || contains(record.getEvent().getTitle(), needle)
                            || contains(record.getAttendanceSession().getSessionTitle(), needle)
            ).toList();
        }
        records = records.stream().sorted(Comparator.comparing(AttendanceRecord::getCheckInTime, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
        return page(records.stream().map(record -> toRecordResponse(record, certificateThresholdFor(record.getEvent()))).toList(), page, size);
    }

    @Transactional(readOnly = true)
    public AttendanceReportResponse getReport(String email, Long eventId, Long attendanceSessionId) {
        User currentUser = requireCurrentUser(email);
        ScopeData scopeData = resolveScope(currentUser, eventId, false);
        AttendanceSession session = attendanceSessionId == null ? null : requireAttendanceSession(attendanceSessionId);
        if (session != null) {
            ensureEventAccess(currentUser, session.getEvent());
        }
        Event event = session != null ? session.getEvent() : scopeData.event;
        List<AttendanceRecord> records = session != null
                ? attendanceRecordRepository.findByAttendanceSession_IdAndDeletedFalseOrderByCheckInTimeDesc(session.getId())
                : event != null
                ? attendanceRecordRepository.findByEvent_IdAndDeletedFalseOrderByCheckInTimeDesc(event.getId())
                : loadVisibleRecords(currentUser, scopeData);
        long present = count(records, AttendanceStatus.PRESENT);
        long absent = count(records, AttendanceStatus.ABSENT);
        long late = count(records, AttendanceStatus.LATE);
        long excused = count(records, AttendanceStatus.EXCUSED);
        long total = records.size();
        long percentage = total == 0 ? 0 : Math.round(((double) (present + late) * 100d) / total);
        List<AttendanceRecordResponse> rows = records.stream().map(record -> toRecordResponse(record, certificateThresholdFor(record.getEvent()))).toList();
        return new AttendanceReportResponse(
                event == null ? "Attendance report" : event.getTitle(),
                event == null ? null : event.getId(),
                session == null ? null : session.getId(),
                session == null ? null : session.getSessionTitle(),
                total,
                present,
                absent,
                late,
                excused,
                percentage,
                rows
        );
    }

    public void invalidateToken(String email, Long tokenId) {
        User currentUser = requireCurrentUser(email);
        QRToken token = requireToken(tokenId);
        ensureEventAccess(currentUser, token.getRegistration().getEvent());
        ensureManagerForEvent(currentUser, token.getRegistration().getEvent());
        token.setInvalidatedAt(now());
        qrTokenRepository.save(token);
        auditToken(token, currentUser, AttendanceActionType.QR_INVALIDATED, "QR token invalidated.");
    }

    @Transactional(readOnly = true)
    public AttendanceOperationResponse validateAndReturn(String email, QRValidationRequest request) {
        return new AttendanceOperationResponse(
                validateQrToken(email, request).message(),
                null,
                null,
                null,
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public QRTokenResponse getTokenForRegistration(String email, Long registrationId) {
        User currentUser = requireCurrentUser(email);
        QRToken token = qrTokenRepository.findByRegistration_IdAndDeletedFalse(registrationId).orElseThrow(() -> new ResourceNotFoundException("QR token not found."));
        ensureEventAccess(currentUser, token.getRegistration().getEvent());
        return toQrResponse(token, null);
    }

    private AttendanceRecord createAttendanceRecord(AttendanceSession session, EventRegistration registration, AttendanceStatus status, AttendanceMethod method, User checkedInBy, QRToken token, String remarks, String deviceInfo, String ipAddress) {
        AttendanceRecord record = new AttendanceRecord();
        record.setInstitution(session.getInstitution());
        record.setEvent(session.getEvent());
        record.setEventSession(session.getEventSession());
        record.setAttendanceSession(session);
        record.setRegistration(registration);
        record.setParticipant(registration.getParticipant());
        record.setAttendanceStatus(status);
        record.setAttendanceMethod(method);
        record.setCheckInTime(now());
        record.setCheckedInBy(checkedInBy);
        record.setQrToken(token);
        record.setDeviceInfo(trimToNull(deviceInfo));
        record.setIpAddress(trimToNull(ipAddress));
        record.setRemarks(trimToNull(remarks));
        return record;
    }

    private void updateRegistrationAttendanceState(EventRegistration registration, AttendanceStatus newStatus) {
        registration.setAttendanceStatus(newStatus);
        registration.setCertificateEligible(isCertificateReady(registration, newStatus));
        registrationRepository.save(registration);
    }

    private boolean isCertificateReady(EventRegistration registration, AttendanceStatus status) {
        EventRegistrationConfig config = configRepository.findByEvent_IdAndDeletedFalse(registration.getEvent().getId()).orElse(null);
        if (config == null || !config.isCertificateEnabled()) {
            return false;
        }
        if (!config.isAttendanceRequiredForCertificate()) {
            return true;
        }
        long totalExpected = Math.max(1, attendanceRecordRepository.findByEvent_IdAndDeletedFalseOrderByCheckInTimeDesc(registration.getEvent().getId()).stream()
                .filter(record -> Objects.equals(record.getParticipant().getId(), registration.getParticipant().getId()))
                .filter(record -> record.getAttendanceStatus() != AttendanceStatus.CANCELLED)
                .count());
        long positive = attendanceRecordRepository.findByEvent_IdAndDeletedFalseOrderByCheckInTimeDesc(registration.getEvent().getId()).stream()
                .filter(record -> Objects.equals(record.getParticipant().getId(), registration.getParticipant().getId()))
                .filter(record -> record.getAttendanceStatus() == AttendanceStatus.PRESENT || record.getAttendanceStatus() == AttendanceStatus.LATE || record.getAttendanceStatus() == AttendanceStatus.EXCUSED)
                .count();
        int percentage = (int) Math.round(((double) positive * 100d) / totalExpected);
        return percentage >= DEFAULT_CERTIFICATE_THRESHOLD && (status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE || status == AttendanceStatus.EXCUSED);
    }

    private int certificateThresholdFor(Event event) {
        return DEFAULT_CERTIFICATE_THRESHOLD;
    }

    private AttendanceSessionResponse toSessionResponse(AttendanceSession session) {
        long recordCount = attendanceRecordRepository.findByAttendanceSession_IdAndDeletedFalseOrderByCheckInTimeDesc(session.getId()).size();
        long present = attendanceRecordRepository.countByAttendanceSession_IdAndAttendanceStatusAndDeletedFalse(session.getId(), AttendanceStatus.PRESENT);
        long absent = attendanceRecordRepository.countByAttendanceSession_IdAndAttendanceStatusAndDeletedFalse(session.getId(), AttendanceStatus.ABSENT);
        long late = attendanceRecordRepository.countByAttendanceSession_IdAndAttendanceStatusAndDeletedFalse(session.getId(), AttendanceStatus.LATE);
        long excused = attendanceRecordRepository.countByAttendanceSession_IdAndAttendanceStatusAndDeletedFalse(session.getId(), AttendanceStatus.EXCUSED);
        int completion = recordCount == 0 ? 0 : (int) Math.round(((double) (present + late + excused) * 100d) / recordCount);
        return new AttendanceSessionResponse(
                session.getId(),
                session.getInstitution().getId(),
                session.getEvent().getId(),
                session.getEvent().getTitle(),
                session.getEventSession() == null ? null : session.getEventSession().getId(),
                session.getEventSession() == null ? null : session.getEventSession().getTitle(),
                session.getSessionTitle(),
                session.getAttendanceSessionStatus(),
                session.getOpenedAt(),
                session.getClosedAt(),
                displayName(session.getOpenedBy()),
                displayName(session.getClosedBy()),
                session.getRemarks(),
                recordCount,
                present,
                absent,
                late,
                excused,
                completion >= DEFAULT_CERTIFICATE_THRESHOLD,
                DEFAULT_CERTIFICATE_THRESHOLD,
                completion
        );
    }

    private AttendanceRecordResponse toRecordResponse(AttendanceRecord record, int threshold) {
        Long eventId = record.getEvent() == null ? null : record.getEvent().getId();
        Long participantId = record.getParticipant() == null ? null : record.getParticipant().getId();
        Long sessionId = record.getAttendanceSession() == null ? null : record.getAttendanceSession().getId();
        String sessionTitle = record.getAttendanceSession() == null ? null : record.getAttendanceSession().getSessionTitle();
        List<AttendanceRecord> participantRecords = eventId == null || participantId == null
                ? List.of()
                : attendanceRecordRepository.findByEvent_IdAndDeletedFalseOrderByCheckInTimeDesc(eventId).stream()
                .filter(item -> item.getParticipant() != null && Objects.equals(item.getParticipant().getId(), participantId))
                .toList();
        long positive = participantRecords.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.PRESENT || item.getAttendanceStatus() == AttendanceStatus.LATE || item.getAttendanceStatus() == AttendanceStatus.EXCUSED).count();
        int completion = participantRecords.isEmpty() ? 0 : (int) Math.round(((double) positive * 100d) / participantRecords.size());
        return new AttendanceRecordResponse(
                record.getId(),
                record.getInstitution().getId(),
                eventId,
                record.getEvent() == null ? null : record.getEvent().getTitle(),
                sessionId,
                sessionTitle,
                record.getEventSession() == null ? null : record.getEventSession().getId(),
                record.getEventSession() == null ? null : record.getEventSession().getTitle(),
                record.getRegistration() == null ? null : record.getRegistration().getId(),
                record.getRegistration() == null ? null : record.getRegistration().getRegistrationNumber(),
                participantId,
                displayName(record.getParticipant()),
                record.getAttendanceStatus(),
                record.getAttendanceMethod(),
                record.getCheckInTime(),
                displayName(record.getCheckedInBy()),
                record.getQrToken() == null ? null : record.getQrToken().getId(),
                record.getDeviceInfo(),
                record.getIpAddress(),
                record.getRemarks(),
                record.getRegistration() != null && record.getRegistration().isCertificateEligible(),
                threshold,
                completion,
                completion >= threshold
        );
    }

    private AttendanceAuditResponse toAuditResponse(AttendanceAudit audit) {
        return new AttendanceAuditResponse(
                audit.getId(),
                audit.getAttendanceRecord() == null ? null : audit.getAttendanceRecord().getId(),
                audit.getActionType(),
                audit.getPreviousStatus(),
                audit.getNewStatus(),
                displayName(audit.getActor()),
                audit.getOccurredAt(),
                audit.getReason(),
                audit.getDetails()
        );
    }

    private QRTokenResponse toQrResponse(QRToken token, String rawToken) {
        return new QRTokenResponse(
                token.getId(),
                token.getRegistration().getId(),
                token.getRegistration().getEvent().getId(),
                token.getRegistration().getEvent().getTitle(),
                rawToken,
                token.getTokenPrefix(),
                token.getExpiresAt(),
                token.getUsedAt(),
                token.getInvalidatedAt(),
                token.isOneTimeUse()
        );
    }

    private void auditSession(AttendanceSession session, User actor, AttendanceActionType actionType, AttendanceStatus previous, AttendanceStatus next, String details) {
        AttendanceAudit audit = new AttendanceAudit();
        audit.setAttendanceRecord(null);
        audit.setActionType(actionType);
        audit.setActor(actor);
        audit.setOccurredAt(now());
        audit.setDetails(details);
        attendanceAuditRepository.save(audit);
    }

    private void auditToken(QRToken token, User actor, AttendanceActionType actionType, String details) {
        AttendanceAudit audit = new AttendanceAudit();
        audit.setAttendanceRecord(null);
        audit.setActionType(actionType);
        audit.setActor(actor);
        audit.setOccurredAt(now());
        audit.setDetails(details);
        attendanceAuditRepository.save(audit);
    }

    private void auditRecord(AttendanceRecord record, User actor, AttendanceActionType actionType, AttendanceStatus previous, AttendanceStatus next, String details) {
        AttendanceAudit audit = new AttendanceAudit();
        audit.setAttendanceRecord(record);
        audit.setActionType(actionType);
        audit.setPreviousStatus(previous);
        audit.setNewStatus(next);
        audit.setActor(actor);
        audit.setOccurredAt(now());
        audit.setReason(details);
        audit.setDetails(details);
        attendanceAuditRepository.save(audit);
    }

    private void notifyParticipant(EventRegistration registration, AttendanceStatus status) {
        if (registration.getParticipant() == null) {
            return;
        }
        NotificationType notificationType = switch (status) {
            case PRESENT, LATE, EXCUSED, ABSENT -> NotificationType.ATTENDANCE_RECORDED;
            case CANCELLED -> NotificationType.REGISTRATION_CANCELLED;
            default -> NotificationType.ATTENDANCE_UPDATED;
        };
        String title = switch (status) {
            case PRESENT -> "Attendance recorded";
            case LATE -> "Attendance marked late";
            case EXCUSED -> "Attendance marked excused";
            case ABSENT -> "Attendance marked absent";
            case CANCELLED -> "Attendance was cancelled";
            default -> "Attendance updated";
        };
        String message = "Your attendance for " + registration.getEvent().getTitle() + " was updated to " + status.name().toLowerCase(Locale.ROOT).replace('_', ' ') + ".";
        saveNotification(registration.getParticipant(), notificationType, title, message, registration.getId(), "AttendanceRecord");
    }

    private void saveNotification(User recipient, NotificationType notificationType, String title, String message, Long relatedEntityId, String relatedEntityType) {
        if (recipient == null) {
            return;
        }
        com.campussphere.entity.registration.InAppNotification notification = new com.campussphere.entity.registration.InAppNotification();
        notification.setRecipient(recipient);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        notificationRepository.save(notification);
    }

    private void validateTokenUsage(QRToken token) {
        if (token.getInvalidatedAt() != null) {
            throw new ConflictException("QR token has been invalidated.");
        }
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now())) {
            throw new ConflictException("QR token has expired.");
        }
        if (token.isOneTimeUse() && token.getUsedAt() != null) {
            throw new ConflictException("QR token has already been used.");
        }
    }

    private void validateAttendanceSession(AttendanceSession session, EventRegistration registration) {
        if (!Objects.equals(session.getEvent().getId(), registration.getEvent().getId())) {
            throw new InvalidInstitutionRelationshipException("Attendance session does not belong to the selected event.");
        }
        if (session.getAttendanceSessionStatus() == AttendanceSessionStatus.CLOSED) {
            throw new BusinessRuleViolationException("Attendance session is already closed.");
        }
    }

    private void validateEventTimeWindow(Event event, AttendanceSession session) {
        if (event.getEventStatus() == EventStatus.CANCELLED || event.getEventStatus() == EventStatus.ARCHIVED || event.getEventStatus() == EventStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Attendance cannot be recorded for this event.");
        }
        LocalDateTime now = now();
        if (event.getStartDateTime() != null && now.isBefore(event.getStartDateTime())) {
            throw new BusinessRuleViolationException("Attendance cannot be recorded before the event starts.");
        }
        if (event.getEndDateTime() != null && now.isAfter(event.getEndDateTime().plusMinutes(15))) {
            throw new BusinessRuleViolationException("Attendance cannot be recorded after the event closes.");
        }
        if (session.getEventSession() != null) {
            EventSession eventSession = session.getEventSession();
            if (eventSession.getSessionStart() != null && now.isBefore(eventSession.getSessionStart().minusMinutes(15))) {
                throw new BusinessRuleViolationException("Attendance cannot be recorded before the session starts.");
            }
            if (eventSession.getSessionEnd() != null && now.isAfter(eventSession.getSessionEnd().plusMinutes(15))) {
                throw new BusinessRuleViolationException("Attendance cannot be recorded after the session ends.");
            }
        }
    }

    private ScopeData resolveScope(User currentUser, Long eventId, boolean allowNullEvent) {
        Institution institution;
        Event event = null;
        if (eventId != null) {
            event = requireEvent(eventId);
            ensureEventAccess(currentUser, event);
            institution = scopeResolver.resolveForRead(currentUser, event.getInstitution().getId());
        } else if (currentUserContext.isStudent(currentUser)) {
            institution = currentUser.getInstitution();
        } else if (currentUser.getInstitution() != null) {
            institution = currentUser.getInstitution();
        } else if (allowNullEvent || currentUserContext.isAdministrator(currentUser)) {
            institution = null;
        } else {
            throw new BusinessRuleViolationException("Institution scope is required.");
        }
        return new ScopeData(institution, event);
    }

    private List<AttendanceRecord> loadVisibleRecords(User currentUser, ScopeData scopeData) {
        if (scopeData.event != null) {
            return attendanceRecordRepository.findByEvent_IdAndDeletedFalseOrderByCheckInTimeDesc(scopeData.event.getId());
        }
        if (currentUserContext.isStudent(currentUser)) {
            return attendanceRecordRepository.findByParticipant_IdAndDeletedFalseOrderByCheckInTimeDesc(currentUser.getId());
        }
        if (scopeData.institution != null) {
            return attendanceRecordRepository.findByInstitution_IdAndDeletedFalseOrderByCheckInTimeDesc(scopeData.institution.getId());
        }
        return attendanceRecordRepository.findAll().stream()
                .filter(record -> !record.isDeleted())
                .sorted(Comparator.comparing(AttendanceRecord::getCheckInTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<AttendanceSession> loadVisibleSessions(ScopeData scopeData) {
        if (scopeData.event != null) {
            return attendanceSessionRepository.findByEvent_IdAndDeletedFalseOrderByOpenedAtDesc(scopeData.event.getId());
        }
        if (scopeData.institution != null) {
            return attendanceSessionRepository.findByInstitution_IdAndDeletedFalseOrderByOpenedAtDesc(scopeData.institution.getId());
        }
        return attendanceSessionRepository.findAll().stream()
                .filter(session -> !session.isDeleted())
                .sorted(Comparator.comparing(AttendanceSession::getOpenedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private void ensureEventAccess(User user, Event event) {
        scopeResolver.resolveForRead(user, event.getInstitution().getId());
        if (currentUserContext.isAdministrator(user)) {
            return;
        }
        if (currentUserContext.isFaculty(user) && coordinatorRepository.existsByEvent_IdAndUser_IdAndDeletedFalse(event.getId(), user.getId())) {
            return;
        }
        if (currentUserContext.isStudent(user)) {
            return;
        }
        throw new BusinessRuleViolationException("You do not have access to this event.");
    }

    private void ensureManagerForEvent(User user, Event event) {
        scopeResolver.resolveForWrite(user, event.getInstitution().getId());
        if (currentUserContext.isAdministrator(user)) {
            return;
        }
        if (currentUserContext.isFaculty(user) && coordinatorRepository.existsByEvent_IdAndUser_IdAndDeletedFalse(event.getId(), user.getId())) {
            return;
        }
        throw new BusinessRuleViolationException("You do not have permission to manage attendance for this event.");
    }

    private void validateEventCanTakeAttendance(Event event, EventSession eventSession) {
        if (eventSession != null && !Objects.equals(eventSession.getEvent().getId(), event.getId())) {
            throw new InvalidInstitutionRelationshipException("The selected session does not belong to this event.");
        }
    }

    private User requireCurrentUser(String email) {
        return currentUserContext.requireCurrentUser(email);
    }

    private Event requireEvent(Long eventId) {
        return eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));
    }

    private EventSession requireEventSession(Long sessionId, Long eventId) {
        EventSession session = eventSessionRepository.findByIdAndDeletedFalse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Event session not found."));
        if (!Objects.equals(session.getEvent().getId(), eventId)) {
            throw new InvalidInstitutionRelationshipException("The selected session does not belong to the event.");
        }
        return session;
    }

    private EventRegistration requireRegistration(Long registrationId) {
        return registrationRepository.findByIdAndDeletedFalse(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found."));
    }

    private QRToken requireToken(String tokenValue) {
        return qrTokenRepository.findByTokenHashAndDeletedFalse(hashToken(tokenValue))
                .orElseThrow(() -> new ResourceNotFoundException("QR token not found."));
    }

    private QRToken requireToken(Long tokenId) {
        return qrTokenRepository.findByIdAndDeletedFalse(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("QR token not found."));
    }

    private AttendanceSession requireAttendanceSession(Long sessionId) {
        return attendanceSessionRepository.findByIdAndDeletedFalse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found."));
    }

    private AttendanceRecord requireAttendanceRecord(Long recordId) {
        return attendanceRecordRepository.findByIdAndDeletedFalse(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found."));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private String buildRawToken(Long registrationId) {
        byte[] random = new byte[24];
        SECURE_RANDOM.nextBytes(random);
        return "CSQR-" + registrationId + "-" + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash QR token.", exception);
        }
    }

    private long count(List<AttendanceRecord> records, AttendanceStatus status) {
        return records.stream().filter(record -> record.getAttendanceStatus() == status).count();
    }

    private PageResponse<AttendanceRecordResponse> page(List<AttendanceRecordResponse> rows, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min(safePage * safeSize, rows.size());
        int toIndex = Math.min(fromIndex + safeSize, rows.size());
        List<AttendanceRecordResponse> content = rows.subList(fromIndex, toIndex);
        int totalPages = (int) Math.ceil(rows.size() / (double) safeSize);
        return PageResponse.of(content, safePage, safeSize, rows.size(), totalPages, safePage == 0, safePage >= Math.max(totalPages - 1, 0));
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private String displayName(User user) {
        if (user == null) {
            return null;
        }
        String firstName = trimToNull(user.getFirstName());
        String lastName = trimToNull(user.getLastName());
        if (firstName == null && lastName == null) {
            return trimToNull(user.getEmail());
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    private record ScopeData(Institution institution, Event event) {
    }
}
