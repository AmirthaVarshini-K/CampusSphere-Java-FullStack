package com.campussphere.service;

import com.campussphere.dto.PageResponse;
import com.campussphere.dto.event.EventCategoryDtos.EventCategoryRequest;
import com.campussphere.dto.event.EventCategoryDtos.EventCategoryResponse;
import com.campussphere.dto.event.EventCoordinatorDtos.EventCoordinatorRequest;
import com.campussphere.dto.event.EventCoordinatorDtos.EventCoordinatorResponse;
import com.campussphere.dto.event.EventDtos.EventOverviewResponse;
import com.campussphere.dto.event.EventDtos.EventRequest;
import com.campussphere.dto.event.EventDtos.EventResponse;
import com.campussphere.dto.event.EventDtos.EventStatusRequest;
import com.campussphere.dto.event.EventDtos.EventSummaryResponse;
import com.campussphere.dto.event.EventEligibilityRuleDtos.EventEligibilityRuleRequest;
import com.campussphere.dto.event.EventEligibilityRuleDtos.EventEligibilityRuleResponse;
import com.campussphere.dto.event.EventRegistrationConfigDtos.EventRegistrationConfigRequest;
import com.campussphere.dto.event.EventRegistrationConfigDtos.EventRegistrationConfigResponse;
import com.campussphere.dto.event.EventSessionDtos.EventSessionRequest;
import com.campussphere.dto.event.EventSessionDtos.EventSessionResponse;
import com.campussphere.dto.event.EventTypeDtos.EventTypeRequest;
import com.campussphere.dto.event.EventTypeDtos.EventTypeResponse;
import com.campussphere.dto.event.VenueDtos.VenueRequest;
import com.campussphere.dto.event.VenueDtos.VenueResponse;
import com.campussphere.entity.AcademicYear;
import com.campussphere.entity.Department;
import com.campussphere.entity.DepartmentProgramme;
import com.campussphere.entity.Institution;
import com.campussphere.entity.Programme;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.Section;
import com.campussphere.entity.User;
import com.campussphere.entity.event.CoordinatorRole;
import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventCategory;
import com.campussphere.entity.event.EventCoordinator;
import com.campussphere.entity.event.EventEligibilityRule;
import com.campussphere.entity.event.EventMode;
import com.campussphere.entity.event.EventRegistrationConfig;
import com.campussphere.entity.event.EventSession;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.event.EventType;
import com.campussphere.entity.event.EventVisibility;
import com.campussphere.entity.event.ParticipantType;
import com.campussphere.entity.event.Venue;
import com.campussphere.entity.event.VenueType;
import com.campussphere.exception.BusinessRuleViolationException;
import com.campussphere.exception.DuplicateResourceException;
import com.campussphere.exception.InvalidInstitutionRelationshipException;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.repository.AcademicYearRepository;
import com.campussphere.repository.DepartmentProgrammeRepository;
import com.campussphere.repository.DepartmentRepository;
import com.campussphere.repository.EventCategoryRepository;
import com.campussphere.repository.EventCoordinatorRepository;
import com.campussphere.repository.EventEligibilityRuleRepository;
import com.campussphere.repository.EventRegistrationConfigRepository;
import com.campussphere.repository.EventRepository;
import com.campussphere.repository.EventSessionRepository;
import com.campussphere.repository.EventTypeRepository;
import com.campussphere.repository.InstitutionRepository;
import com.campussphere.repository.ProgrammeRepository;
import com.campussphere.repository.SectionRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.repository.VenueRepository;
import com.campussphere.service.support.CurrentUserContext;
import com.campussphere.service.support.InstitutionScopeResolver;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class EventManagementService {

    private final EventCategoryRepository eventCategoryRepository;
    private final EventTypeRepository eventTypeRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final EventSessionRepository eventSessionRepository;
    private final EventCoordinatorRepository eventCoordinatorRepository;
    private final EventEligibilityRuleRepository eventEligibilityRuleRepository;
    private final EventRegistrationConfigRepository eventRegistrationConfigRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentProgrammeRepository departmentProgrammeRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ProgrammeRepository programmeRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final CurrentUserContext currentUserContext;
    private final InstitutionScopeResolver scopeResolver;

    public EventManagementService(
            EventCategoryRepository eventCategoryRepository,
            EventTypeRepository eventTypeRepository,
            VenueRepository venueRepository,
            EventRepository eventRepository,
            EventSessionRepository eventSessionRepository,
            EventCoordinatorRepository eventCoordinatorRepository,
            EventEligibilityRuleRepository eventEligibilityRuleRepository,
            EventRegistrationConfigRepository eventRegistrationConfigRepository,
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            DepartmentProgrammeRepository departmentProgrammeRepository,
            AcademicYearRepository academicYearRepository,
            ProgrammeRepository programmeRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            CurrentUserContext currentUserContext,
            InstitutionScopeResolver scopeResolver
    ) {
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.eventCoordinatorRepository = eventCoordinatorRepository;
        this.eventEligibilityRuleRepository = eventEligibilityRuleRepository;
        this.eventRegistrationConfigRepository = eventRegistrationConfigRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.departmentProgrammeRepository = departmentProgrammeRepository;
        this.academicYearRepository = academicYearRepository;
        this.programmeRepository = programmeRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.currentUserContext = currentUserContext;
        this.scopeResolver = scopeResolver;
    }

    @Transactional(readOnly = true)
    public PageResponse<EventCategoryResponse> listCategories(String email, Long institutionId, String search, Boolean active, int page, int size) {
        User currentUser = requireCurrentUser(email);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<EventCategoryResponse> content = eventCategoryRepository.findAll().stream()
                .filter(record -> !record.isDeleted())
                .filter(record -> scope == null || Objects.equals(record.getInstitution().getId(), scope.getId()))
                .filter(record -> active == null || (record.getStatus() == RecordStatus.ACTIVE) == active)
                .filter(record -> matches(record.getCategoryCode(), search) || matches(record.getCategoryName(), search) || matches(record.getDescription(), search))
                .sorted(Comparator.comparing(EventCategory::getCategoryName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCategoryResponse)
                .toList();
        return page(content, page, size);
    }

    @Transactional(readOnly = true)
    public EventCategoryResponse getCategory(String email, Long id) {
        User currentUser = requireCurrentUser(email);
        EventCategory category = requireCategory(id);
        scopeResolver.resolveForRead(currentUser, category.getInstitution().getId());
        return toCategoryResponse(category);
    }

    public EventCategoryResponse createCategory(String email, EventCategoryRequest request) {
        User currentUser = requireCurrentUser(email);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        validateCategoryUnique(institution.getId(), request.categoryCode(), request.categoryName(), null);
        EventCategory category = new EventCategory();
        category.setInstitution(institution);
        category.setCategoryCode(trim(request.categoryCode()));
        category.setCategoryName(trim(request.categoryName()));
        category.setDescription(trimToNull(request.description()));
        eventCategoryRepository.save(category);
        return toCategoryResponse(category);
    }

    public EventCategoryResponse updateCategory(String email, Long id, EventCategoryRequest request) {
        User currentUser = requireCurrentUser(email);
        EventCategory category = requireCategory(id);
        scopeResolver.resolveForWrite(currentUser, request.institutionId());
        if (!Objects.equals(category.getInstitution().getId(), request.institutionId())) {
            throw new InvalidInstitutionRelationshipException("Event category cannot be moved to another institution.");
        }
        validateCategoryUnique(category.getInstitution().getId(), request.categoryCode(), request.categoryName(), category.getId());
        category.setCategoryCode(trim(request.categoryCode()));
        category.setCategoryName(trim(request.categoryName()));
        category.setDescription(trimToNull(request.description()));
        eventCategoryRepository.save(category);
        return toCategoryResponse(category);
    }

    public EventCategoryResponse updateCategoryStatus(String email, Long id, boolean active) {
        User currentUser = requireCurrentUser(email);
        EventCategory category = requireCategory(id);
        scopeResolver.resolveForWrite(currentUser, category.getInstitution().getId());
        category.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        eventCategoryRepository.save(category);
        return toCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventTypeResponse> listTypes(String email, Long institutionId, String search, Boolean active, int page, int size) {
        User currentUser = requireCurrentUser(email);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<EventTypeResponse> content = eventTypeRepository.findAll().stream()
                .filter(record -> !record.isDeleted())
                .filter(record -> scope == null || Objects.equals(record.getInstitution().getId(), scope.getId()))
                .filter(record -> active == null || (record.getStatus() == RecordStatus.ACTIVE) == active)
                .filter(record -> matches(record.getTypeCode(), search) || matches(record.getTypeName(), search) || matches(record.getDescription(), search))
                .sorted(Comparator.comparing(EventType::getTypeName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toTypeResponse)
                .toList();
        return page(content, page, size);
    }

    @Transactional(readOnly = true)
    public EventTypeResponse getType(String email, Long id) {
        User currentUser = requireCurrentUser(email);
        EventType type = requireType(id);
        scopeResolver.resolveForRead(currentUser, type.getInstitution().getId());
        return toTypeResponse(type);
    }

    public EventTypeResponse createType(String email, EventTypeRequest request) {
        User currentUser = requireCurrentUser(email);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        validateTypeUnique(institution.getId(), request.typeCode(), request.typeName(), null);
        EventType type = new EventType();
        type.setInstitution(institution);
        type.setTypeCode(trim(request.typeCode()));
        type.setTypeName(trim(request.typeName()));
        type.setDescription(trimToNull(request.description()));
        eventTypeRepository.save(type);
        return toTypeResponse(type);
    }

    public EventTypeResponse updateType(String email, Long id, EventTypeRequest request) {
        User currentUser = requireCurrentUser(email);
        EventType type = requireType(id);
        scopeResolver.resolveForWrite(currentUser, request.institutionId());
        if (!Objects.equals(type.getInstitution().getId(), request.institutionId())) {
            throw new InvalidInstitutionRelationshipException("Event type cannot be moved to another institution.");
        }
        validateTypeUnique(type.getInstitution().getId(), request.typeCode(), request.typeName(), type.getId());
        type.setTypeCode(trim(request.typeCode()));
        type.setTypeName(trim(request.typeName()));
        type.setDescription(trimToNull(request.description()));
        eventTypeRepository.save(type);
        return toTypeResponse(type);
    }

    public EventTypeResponse updateTypeStatus(String email, Long id, boolean active) {
        User currentUser = requireCurrentUser(email);
        EventType type = requireType(id);
        scopeResolver.resolveForWrite(currentUser, type.getInstitution().getId());
        type.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        eventTypeRepository.save(type);
        return toTypeResponse(type);
    }

    @Transactional(readOnly = true)
    public PageResponse<VenueResponse> listVenues(String email, Long institutionId, String search, VenueType venueType, Boolean active, int page, int size) {
        User currentUser = requireCurrentUser(email);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<VenueResponse> content = venueRepository.findAll().stream()
                .filter(record -> !record.isDeleted())
                .filter(record -> scope == null || Objects.equals(record.getInstitution().getId(), scope.getId()))
                .filter(record -> active == null || (record.getStatus() == RecordStatus.ACTIVE) == active)
                .filter(record -> venueType == null || record.getVenueType() == venueType)
                .filter(record -> matches(record.getVenueCode(), search) || matches(record.getVenueName(), search) || matches(record.getBuilding(), search) || matches(record.getRoomNumber(), search))
                .sorted(Comparator.comparing(Venue::getVenueName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toVenueResponse)
                .toList();
        return page(content, page, size);
    }

    @Transactional(readOnly = true)
    public VenueResponse getVenue(String email, Long id) {
        User currentUser = requireCurrentUser(email);
        Venue venue = requireVenue(id);
        scopeResolver.resolveForRead(currentUser, venue.getInstitution().getId());
        return toVenueResponse(venue);
    }

    public VenueResponse createVenue(String email, VenueRequest request) {
        User currentUser = requireCurrentUser(email);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        validateVenueUnique(institution.getId(), request.venueCode(), request.venueName(), null);
        Venue venue = new Venue();
        venue.setInstitution(institution);
        applyVenueRequest(venue, request);
        venue.setStatus(RecordStatus.ACTIVE);
        venueRepository.save(venue);
        return toVenueResponse(venue);
    }

    public VenueResponse updateVenue(String email, Long id, VenueRequest request) {
        User currentUser = requireCurrentUser(email);
        Venue venue = requireVenue(id);
        scopeResolver.resolveForWrite(currentUser, request.institutionId());
        if (!Objects.equals(venue.getInstitution().getId(), request.institutionId())) {
            throw new InvalidInstitutionRelationshipException("Venue cannot be moved to another institution.");
        }
        validateVenueUnique(venue.getInstitution().getId(), request.venueCode(), request.venueName(), venue.getId());
        applyVenueRequest(venue, request);
        venueRepository.save(venue);
        return toVenueResponse(venue);
    }

    public VenueResponse updateVenueStatus(String email, Long id, boolean active) {
        User currentUser = requireCurrentUser(email);
        Venue venue = requireVenue(id);
        scopeResolver.resolveForWrite(currentUser, venue.getInstitution().getId());
        venue.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        venueRepository.save(venue);
        return toVenueResponse(venue);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventSummaryResponse> listEvents(String email, Long institutionId, String search, EventStatus status, EventMode mode, Long categoryId, Long typeId, Long departmentId, Long venueId, LocalDate from, LocalDate to, int page, int size) {
        User currentUser = requireCurrentUser(email);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<EventSummaryResponse> content = eventRepository.findAll().stream()
                .filter(record -> !record.isDeleted())
                .filter(record -> scope == null || Objects.equals(record.getInstitution().getId(), scope.getId()))
                .filter(record -> status == null || record.getEventStatus() == status)
                .filter(record -> mode == null || record.getMode() == mode)
                .filter(record -> categoryId == null || (record.getEventCategory() != null && Objects.equals(record.getEventCategory().getId(), categoryId)))
                .filter(record -> typeId == null || (record.getEventType() != null && Objects.equals(record.getEventType().getId(), typeId)))
                .filter(record -> departmentId == null || (record.getOrganizingDepartment() != null && Objects.equals(record.getOrganizingDepartment().getId(), departmentId)))
                .filter(record -> venueId == null || (record.getVenue() != null && Objects.equals(record.getVenue().getId(), venueId)))
                .filter(record -> from == null || (record.getStartDateTime() != null && !record.getStartDateTime().toLocalDate().isBefore(from)))
                .filter(record -> to == null || (record.getEndDateTime() != null && !record.getEndDateTime().toLocalDate().isAfter(to)))
                .filter(record -> matches(record.getTitle(), search) || matches(record.getEventCode(), search) || matches(record.getShortDescription(), search) || matches(record.getFullDescription(), search))
                .sorted(Comparator.comparing(Event::getStartDateTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toEventSummary)
                .toList();
        return page(content, page, size);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(String email, Long id) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(id);
        scopeResolver.resolveForRead(currentUser, event.getInstitution().getId());
        return toEventResponse(event);
    }

    public EventResponse createEvent(String email, EventRequest request) {
        User currentUser = requireCurrentUser(email);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        validateEventUnique(institution.getId(), request.eventCode(), request.slug(), null);
        EventCategory category = requireCategoryForInstitution(institution.getId(), request.eventCategoryId());
        EventType type = requireTypeForInstitution(institution.getId(), request.eventTypeId());
        Department department = request.organizingDepartmentId() == null ? null : requireDepartmentForInstitution(institution.getId(), request.organizingDepartmentId());
        AcademicYear academicYear = request.academicYearId() == null ? null : requireAcademicYearForInstitution(institution.getId(), request.academicYearId());
        Venue venue = request.venueId() == null ? null : requireVenueForInstitution(institution.getId(), request.venueId());
        validateEventRelationships(institution.getId(), department, type, venue, request.mode());
        validateEventSchedule(request.startDateTime(), request.endDateTime(), request.registrationStartDateTime(), request.registrationEndDateTime(), request.cancellationDeadline());
        validateParticipantCounts(request.minimumParticipants(), request.maximumParticipants(), venue, request.mode());

        Event event = new Event();
        event.setInstitution(institution);
        applyEventRequest(event, request, category, type, department, academicYear, venue);
        event.setEventStatus(EventStatus.DRAFT);
        event.setStatus(RecordStatus.ACTIVE);
        eventRepository.save(event);
        return toEventResponse(event);
    }

    public EventResponse updateEvent(String email, Long id, EventRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(id);
        scopeResolver.resolveForWrite(currentUser, request.institutionId());
        if (!Objects.equals(event.getInstitution().getId(), request.institutionId())) {
            throw new InvalidInstitutionRelationshipException("Event cannot be moved to another institution.");
        }
        if (event.getEventStatus() == EventStatus.COMPLETED || event.getEventStatus() == EventStatus.ARCHIVED) {
            throw new BusinessRuleViolationException("Completed or archived events cannot be edited.");
        }
        validateEventUnique(event.getInstitution().getId(), request.eventCode(), request.slug(), event.getId());
        EventCategory category = requireCategoryForInstitution(event.getInstitution().getId(), request.eventCategoryId());
        EventType type = requireTypeForInstitution(event.getInstitution().getId(), request.eventTypeId());
        Department department = request.organizingDepartmentId() == null ? null : requireDepartmentForInstitution(event.getInstitution().getId(), request.organizingDepartmentId());
        AcademicYear academicYear = request.academicYearId() == null ? null : requireAcademicYearForInstitution(event.getInstitution().getId(), request.academicYearId());
        Venue venue = request.venueId() == null ? null : requireVenueForInstitution(event.getInstitution().getId(), request.venueId());
        validateEventRelationships(event.getInstitution().getId(), department, type, venue, request.mode());
        validateEventSchedule(request.startDateTime(), request.endDateTime(), request.registrationStartDateTime(), request.registrationEndDateTime(), request.cancellationDeadline());
        validateParticipantCounts(request.minimumParticipants(), request.maximumParticipants(), venue, request.mode());
        applyEventRequest(event, request, category, type, department, academicYear, venue);
        eventRepository.save(event);
        return toEventResponse(event);
    }

    public EventResponse updateEventStatus(String email, Long id, EventStatusRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(id);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        EventStatus nextStatus = parseStatus(request.status());
        validateTransition(event, nextStatus);
        event.setEventStatus(nextStatus);
        if (nextStatus == EventStatus.PUBLISHED && event.getPublishedAt() == null) {
            event.setPublishedAt(LocalDateTime.now());
        }
        eventRepository.save(event);
        return toEventResponse(event);
    }

    @Transactional(readOnly = true)
    public EventOverviewResponse getEventOverview(String email, Long eventId) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(currentUser, event.getInstitution().getId());
        int coordinatorCount = eventCoordinatorRepository.findByEvent_IdAndDeletedFalse(event.getId()).size();
        int sessionCount = eventSessionRepository.findByEvent_IdAndDeletedFalseOrderBySequenceNumberAsc(event.getId()).size();
        int ruleCount = eventEligibilityRuleRepository.findByEvent_IdAndDeletedFalse(event.getId()).size();
        boolean registrationReady = eventRegistrationConfigRepository.findByEvent_IdAndDeletedFalse(event.getId()).isPresent();
        boolean ready = isPublicationReady(event, registrationReady, coordinatorCount, sessionCount);
        return new EventOverviewResponse(event.getId(), event.getTitle(), event.getEventCode(), event.getEventStatus().name(), event.getMode().name(), ready, coordinatorCount, sessionCount, ruleCount, registrationReady, ready ? "Ready to publish" : "Complete the missing setup items");
    }

    @Transactional(readOnly = true)
    public List<EventSessionResponse> listEventSessions(String email, Long eventId) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(currentUser, event.getInstitution().getId());
        return eventSessionRepository.findByEvent_IdAndDeletedFalseOrderBySequenceNumberAsc(event.getId()).stream().map(this::toSessionResponse).toList();
    }

    public EventSessionResponse createEventSession(String email, Long eventId, EventSessionRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        validateEventReference(request.eventId(), eventId);
        validateSessionWindow(event, request.sessionStart(), request.sessionEnd());
        if (eventSessionRepository.existsByEvent_IdAndSequenceNumberAndDeletedFalse(event.getId(), request.sequenceNumber())) {
            throw new DuplicateResourceException("Session sequence already exists for this event.");
        }
        EventSession session = new EventSession();
        session.setEvent(event);
        session.setTitle(trim(request.title()));
        session.setDescription(trimToNull(request.description()));
        session.setSessionStart(request.sessionStart());
        session.setSessionEnd(request.sessionEnd());
        session.setVenue(request.venueId() == null ? null : requireVenueForInstitution(event.getInstitution().getId(), request.venueId()));
        session.setSpeakerName(trimToNull(request.speakerName()));
        session.setSequenceNumber(request.sequenceNumber());
        session.setStatus(RecordStatus.ACTIVE);
        eventSessionRepository.save(session);
        return toSessionResponse(session);
    }

    public EventSessionResponse updateEventSession(String email, Long eventId, Long sessionId, EventSessionRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        EventSession session = requireSession(sessionId);
        validateEventReference(request.eventId(), eventId);
        if (!Objects.equals(session.getEvent().getId(), event.getId())) {
            throw new InvalidInstitutionRelationshipException("Session does not belong to the selected event.");
        }
        validateSessionWindow(event, request.sessionStart(), request.sessionEnd());
        if (request.sequenceNumber() != session.getSequenceNumber() && eventSessionRepository.existsByEvent_IdAndSequenceNumberAndDeletedFalse(event.getId(), request.sequenceNumber())) {
            throw new DuplicateResourceException("Session sequence already exists for this event.");
        }
        session.setTitle(trim(request.title()));
        session.setDescription(trimToNull(request.description()));
        session.setSessionStart(request.sessionStart());
        session.setSessionEnd(request.sessionEnd());
        session.setVenue(request.venueId() == null ? null : requireVenueForInstitution(event.getInstitution().getId(), request.venueId()));
        session.setSpeakerName(trimToNull(request.speakerName()));
        session.setSequenceNumber(request.sequenceNumber());
        eventSessionRepository.save(session);
        return toSessionResponse(session);
    }

    public EventSessionResponse updateEventSessionStatus(String email, Long eventId, Long sessionId, boolean active) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        EventSession session = requireSession(sessionId);
        if (!Objects.equals(session.getEvent().getId(), event.getId())) {
            throw new InvalidInstitutionRelationshipException("Session does not belong to the selected event.");
        }
        session.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        eventSessionRepository.save(session);
        return toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public List<EventCoordinatorResponse> listEventCoordinators(String email, Long eventId) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(currentUser, event.getInstitution().getId());
        return eventCoordinatorRepository.findByEvent_IdAndDeletedFalse(event.getId()).stream().map(this::toCoordinatorResponse).toList();
    }

    public EventCoordinatorResponse assignCoordinator(String email, Long eventId, EventCoordinatorRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        validateEventReference(request.eventId(), eventId);
        User coordinator = requireInstitutionUser(event.getInstitution().getId(), request.userId());
        if (eventCoordinatorRepository.existsByEvent_IdAndUser_IdAndCoordinatorRoleAndDeletedFalse(event.getId(), coordinator.getId(), request.coordinatorRole())) {
            throw new DuplicateResourceException("This coordinator assignment already exists.");
        }
        EventCoordinator assignment = new EventCoordinator();
        assignment.setEvent(event);
        assignment.setUser(coordinator);
        assignment.setCoordinatorRole(request.coordinatorRole());
        assignment.setPrimaryCoordinator(request.primaryCoordinator());
        assignment.setStatus(RecordStatus.ACTIVE);
        if (request.primaryCoordinator()) {
            eventCoordinatorRepository.findByEvent_IdAndDeletedFalse(event.getId()).forEach(existing -> {
                existing.setPrimaryCoordinator(false);
                eventCoordinatorRepository.save(existing);
            });
        }
        eventCoordinatorRepository.save(assignment);
        return toCoordinatorResponse(assignment);
    }

    public EventCoordinatorResponse updateCoordinator(String email, Long eventId, Long coordinatorId, EventCoordinatorRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        validateEventReference(request.eventId(), eventId);
        EventCoordinator assignment = requireCoordinator(coordinatorId);
        if (!Objects.equals(assignment.getEvent().getId(), event.getId())) {
            throw new InvalidInstitutionRelationshipException("Coordinator does not belong to the selected event.");
        }
        User coordinator = requireInstitutionUser(event.getInstitution().getId(), request.userId());
        if (!Objects.equals(assignment.getUser().getId(), coordinator.getId())
                && eventCoordinatorRepository.existsByEvent_IdAndUser_IdAndCoordinatorRoleAndDeletedFalse(event.getId(), coordinator.getId(), request.coordinatorRole())) {
            throw new DuplicateResourceException("This coordinator assignment already exists.");
        }
        assignment.setUser(coordinator);
        assignment.setCoordinatorRole(request.coordinatorRole());
        assignment.setPrimaryCoordinator(request.primaryCoordinator());
        if (request.primaryCoordinator()) {
            eventCoordinatorRepository.findByEvent_IdAndDeletedFalse(event.getId()).stream()
                    .filter(existing -> !Objects.equals(existing.getId(), assignment.getId()))
                    .forEach(existing -> {
                        existing.setPrimaryCoordinator(false);
                        eventCoordinatorRepository.save(existing);
                    });
        }
        eventCoordinatorRepository.save(assignment);
        return toCoordinatorResponse(assignment);
    }

    public void removeCoordinator(String email, Long eventId, Long coordinatorId) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        EventCoordinator assignment = requireCoordinator(coordinatorId);
        if (!Objects.equals(assignment.getEvent().getId(), event.getId())) {
            throw new InvalidInstitutionRelationshipException("Coordinator does not belong to the selected event.");
        }
        assignment.setDeleted(true);
        assignment.setStatus(RecordStatus.INACTIVE);
        eventCoordinatorRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<EventEligibilityRuleResponse> listEligibilityRules(String email, Long eventId) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(currentUser, event.getInstitution().getId());
        return eventEligibilityRuleRepository.findByEvent_IdAndDeletedFalse(event.getId()).stream().map(this::toEligibilityResponse).toList();
    }

    public EventEligibilityRuleResponse createEligibilityRule(String email, Long eventId, EventEligibilityRuleRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        validateEventReference(request.eventId(), eventId);
        EventEligibilityRule rule = new EventEligibilityRule();
        applyEligibilityRequest(event, request, rule);
        eventEligibilityRuleRepository.save(rule);
        return toEligibilityResponse(rule);
    }

    public EventEligibilityRuleResponse updateEligibilityRule(String email, Long eventId, Long ruleId, EventEligibilityRuleRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        validateEventReference(request.eventId(), eventId);
        EventEligibilityRule rule = requireEligibilityRule(ruleId);
        if (!Objects.equals(rule.getEvent().getId(), event.getId())) {
            throw new InvalidInstitutionRelationshipException("Eligibility rule does not belong to the selected event.");
        }
        applyEligibilityRequest(event, request, rule);
        eventEligibilityRuleRepository.save(rule);
        return toEligibilityResponse(rule);
    }

    public EventEligibilityRuleResponse updateEligibilityStatus(String email, Long eventId, Long ruleId, boolean active) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        EventEligibilityRule rule = requireEligibilityRule(ruleId);
        if (!Objects.equals(rule.getEvent().getId(), event.getId())) {
            throw new InvalidInstitutionRelationshipException("Eligibility rule does not belong to the selected event.");
        }
        rule.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        eventEligibilityRuleRepository.save(rule);
        return toEligibilityResponse(rule);
    }

    @Transactional(readOnly = true)
    public EventRegistrationConfigResponse getRegistrationConfig(String email, Long eventId) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForRead(currentUser, event.getInstitution().getId());
        return eventRegistrationConfigRepository.findByEvent_IdAndDeletedFalse(event.getId()).map(this::toRegistrationConfigResponse).orElse(null);
    }

    public EventRegistrationConfigResponse upsertRegistrationConfig(String email, Long eventId, EventRegistrationConfigRequest request) {
        User currentUser = requireCurrentUser(email);
        Event event = requireEvent(eventId);
        scopeResolver.resolveForWrite(currentUser, event.getInstitution().getId());
        validateEventReference(request.eventId(), eventId);
        validateRegistrationConfig(request);
        EventRegistrationConfig config = eventRegistrationConfigRepository.findByEvent_IdAndDeletedFalse(event.getId()).orElseGet(EventRegistrationConfig::new);
        config.setEvent(event);
        config.setRegistrationRequired(request.registrationRequired());
        config.setApprovalRequired(request.approvalRequired());
        config.setWaitlistEnabled(request.waitlistEnabled());
        config.setTeamEvent(request.teamEvent());
        config.setMinimumTeamSize(request.minimumTeamSize());
        config.setMaximumTeamSize(request.maximumTeamSize());
        config.setAllowExternalParticipants(request.allowExternalParticipants());
        config.setAllowMultipleRegistrations(request.allowMultipleRegistrations());
        config.setCertificateEnabled(request.certificateEnabled());
        config.setAttendanceRequiredForCertificate(request.attendanceRequiredForCertificate());
        config.setCancellationAllowed(request.cancellationAllowed());
        config.setCancellationDeadline(request.cancellationDeadline());
        config.setStatus(RecordStatus.ACTIVE);
        eventRegistrationConfigRepository.save(config);
        return toRegistrationConfigResponse(config);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventSummaryResponse> dashboardSummary(String email, Long institutionId, int page, int size) {
        return listEvents(email, institutionId, null, null, null, null, null, null, null, null, null, page, size);
    }

    private void applyVenueRequest(Venue venue, VenueRequest request) {
        venue.setVenueCode(trim(request.venueCode()));
        venue.setVenueName(trim(request.venueName()));
        venue.setBuilding(trimToNull(request.building()));
        venue.setFloor(trimToNull(request.floor()));
        venue.setRoomNumber(trimToNull(request.roomNumber()));
        venue.setAddress(trimToNull(request.address()));
        venue.setCapacity(request.capacity());
        venue.setVenueType(request.venueType());
    }

    private void applyEventRequest(Event event, EventRequest request, EventCategory category, EventType type, Department department, AcademicYear academicYear, Venue venue) {
        event.setTitle(trim(request.title()));
        event.setEventCode(trim(request.eventCode()));
        event.setSlug(trimToNull(request.slug()) != null ? slugify(request.slug()) : slugify(request.title()));
        event.setShortDescription(trimToNull(request.shortDescription()));
        event.setFullDescription(trimToNull(request.fullDescription()));
        event.setEventCategory(category);
        event.setEventType(type);
        event.setOrganizingDepartment(department);
        event.setAcademicYear(academicYear);
        event.setVenue(venue);
        event.setMode(request.mode());
        event.setVisibility(request.visibility());
        event.setStartDateTime(request.startDateTime());
        event.setEndDateTime(request.endDateTime());
        event.setRegistrationStartDateTime(request.registrationStartDateTime());
        event.setRegistrationEndDateTime(request.registrationEndDateTime());
        event.setCancellationDeadline(request.cancellationDeadline());
        event.setOnlineMeetingUrl(trimToNull(request.onlineMeetingUrl()));
        event.setMaximumParticipants(request.maximumParticipants());
        event.setMinimumParticipants(request.minimumParticipants());
        event.setRegistrationFee(request.registrationFee());
        event.setCurrency(trimToNull(request.currency()));
        event.setBannerImageUrl(trimToNull(request.bannerImageUrl()));
        event.setContactEmail(trimToNull(request.contactEmail()));
        event.setContactPhone(trimToNull(request.contactPhone()));
    }

    private void applyEligibilityRequest(Event event, EventEligibilityRuleRequest request, EventEligibilityRule rule) {
        Department department = request.departmentId() == null ? null : requireDepartmentForInstitution(event.getInstitution().getId(), request.departmentId());
        Programme programme = request.programmeId() == null ? null : requireProgrammeForInstitution(event.getInstitution().getId(), request.programmeId());
        Section section = request.sectionId() == null ? null : requireSectionForInstitution(event.getInstitution().getId(), request.sectionId());
        if (department != null && programme != null && !departmentProgrammeRepository.existsByInstitution_IdAndDepartment_IdAndProgramme_IdAndDeletedFalse(event.getInstitution().getId(), department.getId(), programme.getId())) {
            throw new InvalidInstitutionRelationshipException("Programme is not mapped to the selected department.");
        }
        if (section != null) {
            if (department != null && !Objects.equals(section.getDepartment().getId(), department.getId())) {
                throw new InvalidInstitutionRelationshipException("Section does not belong to the selected department.");
            }
            if (programme != null && !Objects.equals(section.getProgramme().getId(), programme.getId())) {
                throw new InvalidInstitutionRelationshipException("Section does not belong to the selected programme.");
            }
        }
        if (request.minimumYear() != null && request.maximumYear() != null && request.minimumYear() > request.maximumYear()) {
            throw new BusinessRuleViolationException("Minimum year cannot exceed maximum year.");
        }
        rule.setEvent(event);
        rule.setDepartment(department);
        rule.setProgramme(programme);
        rule.setSection(section);
        rule.setParticipantType(request.participantType());
        rule.setRuleType(request.ruleType());
        rule.setMinimumYear(request.minimumYear());
        rule.setMaximumYear(request.maximumYear());
        rule.setStatus(RecordStatus.ACTIVE);
    }

    private void validateEventUnique(Long institutionId, String eventCode, String slug, Long id) {
        String normalizedCode = trim(eventCode);
        String normalizedSlug = trimToNull(slug);
        if (id == null) {
            if (eventRepository.existsByInstitution_IdAndEventCodeIgnoreCaseAndDeletedFalse(institutionId, normalizedCode)) {
                throw new DuplicateResourceException("Event code already exists for this institution.");
            }
            if (normalizedSlug != null && eventRepository.existsByInstitution_IdAndSlugIgnoreCaseAndDeletedFalse(institutionId, normalizedSlug)) {
                throw new DuplicateResourceException("Event slug already exists for this institution.");
            }
        } else {
            if (eventRepository.existsByInstitution_IdAndEventCodeIgnoreCaseAndIdNotAndDeletedFalse(institutionId, normalizedCode, id)) {
                throw new DuplicateResourceException("Event code already exists for this institution.");
            }
            if (normalizedSlug != null && eventRepository.existsByInstitution_IdAndSlugIgnoreCaseAndIdNotAndDeletedFalse(institutionId, normalizedSlug, id)) {
                throw new DuplicateResourceException("Event slug already exists for this institution.");
            }
        }
    }

    private void validateCategoryUnique(Long institutionId, String code, String name, Long id) {
        String normalizedCode = trim(code);
        String normalizedName = trim(name);
        if (id == null) {
            if (eventCategoryRepository.existsByInstitution_IdAndCategoryCodeIgnoreCaseAndDeletedFalse(institutionId, normalizedCode)) {
                throw new DuplicateResourceException("Event category code already exists for this institution.");
            }
            if (eventCategoryRepository.existsByInstitution_IdAndCategoryNameIgnoreCaseAndDeletedFalse(institutionId, normalizedName)) {
                throw new DuplicateResourceException("Event category name already exists for this institution.");
            }
        } else {
            if (eventCategoryRepository.existsByInstitution_IdAndCategoryCodeIgnoreCaseAndIdNotAndDeletedFalse(institutionId, normalizedCode, id)) {
                throw new DuplicateResourceException("Event category code already exists for this institution.");
            }
            if (eventCategoryRepository.existsByInstitution_IdAndCategoryNameIgnoreCaseAndIdNotAndDeletedFalse(institutionId, normalizedName, id)) {
                throw new DuplicateResourceException("Event category name already exists for this institution.");
            }
        }
    }

    private void validateTypeUnique(Long institutionId, String code, String name, Long id) {
        String normalizedCode = trim(code);
        String normalizedName = trim(name);
        if (id == null) {
            if (eventTypeRepository.existsByInstitution_IdAndTypeCodeIgnoreCaseAndDeletedFalse(institutionId, normalizedCode)) {
                throw new DuplicateResourceException("Event type code already exists for this institution.");
            }
            if (eventTypeRepository.existsByInstitution_IdAndTypeNameIgnoreCaseAndDeletedFalse(institutionId, normalizedName)) {
                throw new DuplicateResourceException("Event type name already exists for this institution.");
            }
        } else {
            if (eventTypeRepository.existsByInstitution_IdAndTypeCodeIgnoreCaseAndIdNotAndDeletedFalse(institutionId, normalizedCode, id)) {
                throw new DuplicateResourceException("Event type code already exists for this institution.");
            }
            if (eventTypeRepository.existsByInstitution_IdAndTypeNameIgnoreCaseAndIdNotAndDeletedFalse(institutionId, normalizedName, id)) {
                throw new DuplicateResourceException("Event type name already exists for this institution.");
            }
        }
    }

    private void validateVenueUnique(Long institutionId, String code, String name, Long id) {
        String normalizedCode = trim(code);
        String normalizedName = trim(name);
        if (id == null) {
            if (venueRepository.existsByInstitution_IdAndVenueCodeIgnoreCaseAndDeletedFalse(institutionId, normalizedCode)) {
                throw new DuplicateResourceException("Venue code already exists for this institution.");
            }
            if (venueRepository.existsByInstitution_IdAndVenueNameIgnoreCaseAndDeletedFalse(institutionId, normalizedName)) {
                throw new DuplicateResourceException("Venue name already exists for this institution.");
            }
        } else {
            if (venueRepository.existsByInstitution_IdAndVenueCodeIgnoreCaseAndIdNotAndDeletedFalse(institutionId, normalizedCode, id)) {
                throw new DuplicateResourceException("Venue code already exists for this institution.");
            }
            if (venueRepository.existsByInstitution_IdAndVenueNameIgnoreCaseAndIdNotAndDeletedFalse(institutionId, normalizedName, id)) {
                throw new DuplicateResourceException("Venue name already exists for this institution.");
            }
        }
    }

    private void validateEventRelationships(Long institutionId, Department department, EventType type, Venue venue, EventMode mode) {
        if (department != null && !Objects.equals(department.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Organising department does not belong to the selected institution.");
        }
        if (type != null && !Objects.equals(type.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Event type does not belong to the selected institution.");
        }
        if (venue != null && !Objects.equals(venue.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Venue does not belong to the selected institution.");
        }
        if (mode == EventMode.OFFLINE && venue == null) {
            throw new BusinessRuleViolationException("Offline events require a venue.");
        }
        if ((mode == EventMode.ONLINE || mode == EventMode.HYBRID) && venue != null && venue.getVenueType() == VenueType.ONLINE) {
            return;
        }
    }

    private void validateEventSchedule(LocalDateTime start, LocalDateTime end, LocalDateTime registrationStart, LocalDateTime registrationEnd, LocalDateTime cancellationDeadline) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BusinessRuleViolationException("Event end date and time must be after the start date and time.");
        }
        if (registrationStart != null && registrationEnd != null && !registrationEnd.isAfter(registrationStart)) {
            throw new BusinessRuleViolationException("Registration end must be after registration start.");
        }
        if (registrationEnd != null && start != null && registrationEnd.isAfter(start)) {
            throw new BusinessRuleViolationException("Registration end cannot be after the event starts.");
        }
        if (cancellationDeadline != null && registrationEnd != null && cancellationDeadline.isAfter(registrationEnd)) {
            throw new BusinessRuleViolationException("Cancellation deadline must not be after registration closes.");
        }
    }

    private void validateParticipantCounts(Integer minimum, Integer maximum, Venue venue, EventMode mode) {
        if (minimum != null && maximum != null && minimum > maximum) {
            throw new BusinessRuleViolationException("Minimum participants cannot exceed maximum participants.");
        }
        if (venue != null && venue.getCapacity() != null && maximum != null && maximum > venue.getCapacity() && mode == EventMode.OFFLINE) {
            throw new BusinessRuleViolationException("Maximum participants cannot exceed venue capacity.");
        }
    }

    private void validateSessionWindow(Event event, LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BusinessRuleViolationException("Session end must be after session start.");
        }
        if (event.getStartDateTime() != null && start != null && start.isBefore(event.getStartDateTime())) {
            throw new BusinessRuleViolationException("Session must fall within the event period.");
        }
        if (event.getEndDateTime() != null && end != null && end.isAfter(event.getEndDateTime())) {
            throw new BusinessRuleViolationException("Session must fall within the event period.");
        }
    }

    private void validateRegistrationConfig(EventRegistrationConfigRequest request) {
        if (request.teamEvent()) {
            if (request.minimumTeamSize() == null || request.maximumTeamSize() == null) {
                throw new BusinessRuleViolationException("Team events require minimum and maximum team sizes.");
            }
            if (request.minimumTeamSize() > request.maximumTeamSize()) {
                throw new BusinessRuleViolationException("Minimum team size cannot exceed maximum team size.");
            }
        } else if (request.minimumTeamSize() != null || request.maximumTeamSize() != null) {
            throw new BusinessRuleViolationException("Team size rules apply only to team events.");
        }
        if (request.cancellationAllowed() && request.cancellationDeadline() == null) {
            throw new BusinessRuleViolationException("Cancellation deadline is required when cancellations are allowed.");
        }
        if (!request.cancellationAllowed() && request.cancellationDeadline() != null) {
            throw new BusinessRuleViolationException("Cancellation deadline should only be set when cancellations are allowed.");
        }
        if (!request.certificateEnabled() && request.attendanceRequiredForCertificate()) {
            throw new BusinessRuleViolationException("Attendance requirement for certificates applies only when certificates are enabled.");
        }
    }

    private void validateTransition(Event event, EventStatus nextStatus) {
        EventStatus current = event.getEventStatus();
        List<EventStatus> valid = switch (current) {
            case DRAFT -> List.of(EventStatus.PENDING_APPROVAL, EventStatus.CANCELLED, EventStatus.ARCHIVED);
            case PENDING_APPROVAL -> List.of(EventStatus.DRAFT, EventStatus.PUBLISHED, EventStatus.CANCELLED, EventStatus.ARCHIVED);
            case PUBLISHED -> List.of(EventStatus.REGISTRATION_OPEN, EventStatus.CANCELLED, EventStatus.ARCHIVED);
            case REGISTRATION_OPEN -> List.of(EventStatus.REGISTRATION_CLOSED, EventStatus.ONGOING, EventStatus.CANCELLED, EventStatus.ARCHIVED);
            case REGISTRATION_CLOSED -> List.of(EventStatus.ONGOING, EventStatus.CANCELLED, EventStatus.ARCHIVED);
            case ONGOING -> List.of(EventStatus.COMPLETED, EventStatus.CANCELLED, EventStatus.ARCHIVED);
            case COMPLETED -> List.of(EventStatus.ARCHIVED);
            case CANCELLED -> List.of(EventStatus.ARCHIVED);
            case ARCHIVED -> List.of();
        };
        if (!valid.contains(nextStatus)) {
            throw new BusinessRuleViolationException("Invalid event status transition from " + current + " to " + nextStatus + ".");
        }
        if (nextStatus == EventStatus.PUBLISHED && !isPublicationReady(event, true, eventCoordinatorRepository.findByEvent_IdAndDeletedFalse(event.getId()).size(), eventSessionRepository.findByEvent_IdAndDeletedFalseOrderBySequenceNumberAsc(event.getId()).size())) {
            throw new BusinessRuleViolationException("Complete the required setup before publishing the event.");
        }
        if (nextStatus == EventStatus.REGISTRATION_OPEN && event.getStartDateTime() == null) {
            throw new BusinessRuleViolationException("Publish the event schedule before opening registration.");
        }
    }

    private boolean isPublicationReady(Event event, boolean registrationReady, int coordinatorCount, int sessionCount) {
        return event.getTitle() != null
                && event.getEventCode() != null
                && event.getEventCategory() != null
                && event.getEventType() != null
                && event.getStartDateTime() != null
                && event.getEndDateTime() != null
                && coordinatorCount > 0
                && sessionCount >= 0
                && registrationReady;
    }

    private EventCategory requireCategory(Long id) {
        return eventCategoryRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Event category not found."));
    }

    private EventCategory requireCategoryForInstitution(Long institutionId, Long id) {
        EventCategory category = requireCategory(id);
        if (!Objects.equals(category.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Event category does not belong to the selected institution.");
        }
        return category;
    }

    private EventType requireType(Long id) {
        return eventTypeRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Event type not found."));
    }

    private EventType requireTypeForInstitution(Long institutionId, Long id) {
        EventType type = requireType(id);
        if (!Objects.equals(type.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Event type does not belong to the selected institution.");
        }
        return type;
    }

    private Venue requireVenue(Long id) {
        return venueRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Venue not found."));
    }

    private Event requireEvent(Long id) {
        return eventRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Event not found."));
    }

    private EventSession requireSession(Long id) {
        return eventSessionRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Session not found."));
    }

    private EventCoordinator requireCoordinator(Long id) {
        return eventCoordinatorRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Coordinator assignment not found."));
    }

    private EventEligibilityRule requireEligibilityRule(Long id) {
        return eventEligibilityRuleRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Eligibility rule not found."));
    }

    private Department requireDepartmentForInstitution(Long institutionId, Long departmentId) {
        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId).orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        if (!Objects.equals(department.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Department does not belong to the selected institution.");
        }
        return department;
    }

    private Programme requireProgrammeForInstitution(Long institutionId, Long programmeId) {
        Programme programme = programmeRepository.findByIdAndDeletedFalse(programmeId).orElseThrow(() -> new ResourceNotFoundException("Programme not found."));
        if (!Objects.equals(programme.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Programme does not belong to the selected institution.");
        }
        return programme;
    }

    private AcademicYear requireAcademicYearForInstitution(Long institutionId, Long academicYearId) {
        AcademicYear academicYear = academicYearRepository.findByIdAndDeletedFalse(academicYearId).orElseThrow(() -> new ResourceNotFoundException("Academic year not found."));
        if (!Objects.equals(academicYear.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Academic year does not belong to the selected institution.");
        }
        return academicYear;
    }

    private Venue requireVenueForInstitution(Long institutionId, Long venueId) {
        Venue venue = requireVenue(venueId);
        if (!Objects.equals(venue.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Venue does not belong to the selected institution.");
        }
        return venue;
    }

    private Section requireSectionForInstitution(Long institutionId, Long sectionId) {
        Section section = sectionRepository.findByIdAndDeletedFalse(sectionId).orElseThrow(() -> new ResourceNotFoundException("Section not found."));
        if (!Objects.equals(section.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Section does not belong to the selected institution.");
        }
        return section;
    }

    private User requireInstitutionUser(Long institutionId, Long userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (user.getInstitution() == null || !Objects.equals(user.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("User does not belong to the selected institution.");
        }
        return user;
    }

    private User requireCurrentUser(String email) {
        return currentUserContext.requireCurrentUser(email);
    }

    private void validateEventReference(Long requestEventId, Long pathEventId) {
        if (requestEventId != null && !Objects.equals(requestEventId, pathEventId)) {
            throw new InvalidInstitutionRelationshipException("The event in the request does not match the selected event.");
        }
    }

    private EventStatus parseStatus(String value) {
        try {
            return EventStatus.valueOf(trim(value).toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BusinessRuleViolationException("Unknown event status: " + value);
        }
    }

    private String slugify(String value) {
        String slug = trimToNull(value) == null ? null : trim(value).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return slug == null || slug.isBlank() ? null : slug;
    }

    private boolean matches(String value, String search) {
        return search == null || search.isBlank() || (value != null && value.toLowerCase(Locale.ROOT).contains(search.trim().toLowerCase(Locale.ROOT)));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private EventCategoryResponse toCategoryResponse(EventCategory category) {
        return new EventCategoryResponse(category.getId(), category.getInstitution().getId(), category.getInstitution().getInstitutionCode(), category.getCategoryCode(), category.getCategoryName(), category.getDescription(), category.getStatus() == RecordStatus.ACTIVE, category.getCreatedAt(), category.getUpdatedAt());
    }

    private EventTypeResponse toTypeResponse(EventType type) {
        return new EventTypeResponse(type.getId(), type.getInstitution().getId(), type.getInstitution().getInstitutionCode(), type.getTypeCode(), type.getTypeName(), type.getDescription(), type.getStatus() == RecordStatus.ACTIVE, type.getCreatedAt(), type.getUpdatedAt());
    }

    private VenueResponse toVenueResponse(Venue venue) {
        return new VenueResponse(venue.getId(), venue.getInstitution().getId(), venue.getInstitution().getInstitutionCode(), venue.getVenueCode(), venue.getVenueName(), venue.getBuilding(), venue.getFloor(), venue.getRoomNumber(), venue.getAddress(), venue.getCapacity(), venue.getVenueType(), venue.getStatus() == RecordStatus.ACTIVE, venue.getCreatedAt(), venue.getUpdatedAt());
    }

    private EventSummaryResponse toEventSummary(Event event) {
        return new EventSummaryResponse(event.getId(), event.getEventCode(), event.getTitle(), event.getEventStatus().name(), event.getMode(), event.getEventCategory().getCategoryName(), event.getEventType().getTypeName(), event.getOrganizingDepartment() == null ? null : event.getOrganizingDepartment().getDepartmentName(), event.getVenue() == null ? null : event.getVenue().getVenueName(), event.getStartDateTime(), event.getEndDateTime(), event.getMaximumParticipants());
    }

    private EventResponse toEventResponse(Event event) {
        return new EventResponse(event.getId(), event.getInstitution().getId(), event.getInstitution().getInstitutionCode(), event.getTitle(), event.getEventCode(), event.getSlug(), event.getShortDescription(), event.getFullDescription(), event.getEventCategory().getId(), event.getEventCategory().getCategoryCode(), event.getEventCategory().getCategoryName(), event.getEventType().getId(), event.getEventType().getTypeCode(), event.getEventType().getTypeName(), event.getOrganizingDepartment() == null ? null : event.getOrganizingDepartment().getId(), event.getOrganizingDepartment() == null ? null : event.getOrganizingDepartment().getDepartmentName(), event.getAcademicYear() == null ? null : event.getAcademicYear().getId(), event.getAcademicYear() == null ? null : event.getAcademicYear().getYearLabel(), event.getVenue() == null ? null : event.getVenue().getId(), event.getVenue() == null ? null : event.getVenue().getVenueName(), event.getMode(), event.getVisibility(), event.getStartDateTime(), event.getEndDateTime(), event.getRegistrationStartDateTime(), event.getRegistrationEndDateTime(), event.getCancellationDeadline(), event.getOnlineMeetingUrl(), event.getMaximumParticipants(), event.getMinimumParticipants(), event.getRegistrationFee(), event.getCurrency(), event.getBannerImageUrl(), event.getContactEmail(), event.getContactPhone(), event.getEventStatus().name(), event.getCreatedAt(), event.getUpdatedAt());
    }

    private EventSessionResponse toSessionResponse(EventSession session) {
        return new EventSessionResponse(session.getId(), session.getEvent().getId(), session.getEvent().getEventCode(), session.getTitle(), session.getDescription(), session.getSessionStart(), session.getSessionEnd(), session.getVenue() == null ? null : session.getVenue().getId(), session.getVenue() == null ? null : session.getVenue().getVenueName(), session.getSpeakerName(), session.getSequenceNumber(), session.getStatus().name(), session.getCreatedAt(), session.getUpdatedAt());
    }

    private EventCoordinatorResponse toCoordinatorResponse(EventCoordinator coordinator) {
        return new EventCoordinatorResponse(coordinator.getId(), coordinator.getEvent().getId(), coordinator.getUser().getId(), coordinator.getUser().getFirstName() + " " + coordinator.getUser().getLastName(), coordinator.getUser().getEmail(), coordinator.getCoordinatorRole(), coordinator.isPrimaryCoordinator(), coordinator.getStatus().name(), coordinator.getCreatedAt(), coordinator.getUpdatedAt());
    }

    private EventEligibilityRuleResponse toEligibilityResponse(EventEligibilityRule rule) {
        return new EventEligibilityRuleResponse(rule.getId(), rule.getEvent().getId(), rule.getDepartment() == null ? null : rule.getDepartment().getId(), rule.getDepartment() == null ? null : rule.getDepartment().getDepartmentName(), rule.getProgramme() == null ? null : rule.getProgramme().getId(), rule.getProgramme() == null ? null : rule.getProgramme().getProgrammeName(), rule.getSection() == null ? null : rule.getSection().getId(), rule.getSection() == null ? null : rule.getSection().getSectionName(), rule.getParticipantType(), rule.getRuleType(), rule.getMinimumYear(), rule.getMaximumYear(), rule.getStatus().name(), rule.getCreatedAt(), rule.getUpdatedAt());
    }

    private EventRegistrationConfigResponse toRegistrationConfigResponse(EventRegistrationConfig config) {
        return new EventRegistrationConfigResponse(config.getId(), config.getEvent().getId(), config.isRegistrationRequired(), config.isApprovalRequired(), config.isWaitlistEnabled(), config.isTeamEvent(), config.getMinimumTeamSize(), config.getMaximumTeamSize(), config.isAllowExternalParticipants(), config.isAllowMultipleRegistrations(), config.isCertificateEnabled(), config.isAttendanceRequiredForCertificate(), config.isCancellationAllowed(), config.getCancellationDeadline(), config.getStatus().name(), config.getCreatedAt(), config.getUpdatedAt());
    }

    private <T> PageResponse<T> page(List<T> content, int page, int size) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 0);
        int totalElements = content.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / safeSize));
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        List<T> slice = new ArrayList<>(content.subList(fromIndex, toIndex));
        return PageResponse.of(slice, safePage, safeSize, totalElements, totalPages, safePage == 0, safePage >= totalPages - 1);
    }
}
