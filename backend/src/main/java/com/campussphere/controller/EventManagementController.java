package com.campussphere.controller;

import com.campussphere.dto.ApiResponse;
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
import com.campussphere.entity.event.EventMode;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.event.VenueType;
import com.campussphere.service.EventManagementService;
import com.campussphere.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EventManagementController {

    private final EventManagementService service;

    public EventManagementController(EventManagementService service) {
        this.service = service;
    }

    @GetMapping("/event-categories")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<EventCategoryResponse>> listCategories(@RequestParam(required = false) Long institutionId,
                                                                            @RequestParam(required = false) String search,
                                                                            @RequestParam(required = false) Boolean active,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Event categories retrieved successfully.", service.listCategories(currentUserEmail(), institutionId, search, active, page, size));
    }

    @GetMapping("/event-categories/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EventCategoryResponse> getCategory(@PathVariable Long id) {
        return ApiResponseFactory.success("Event category retrieved successfully.", service.getCategory(currentUserEmail(), id));
    }

    @PostMapping("/event-categories")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventCategoryResponse> createCategory(@Valid @RequestBody EventCategoryRequest request) {
        return ApiResponseFactory.success("Event category created successfully.", service.createCategory(currentUserEmail(), request));
    }

    @PutMapping("/event-categories/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventCategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody EventCategoryRequest request) {
        return ApiResponseFactory.success("Event category updated successfully.", service.updateCategory(currentUserEmail(), id, request));
    }

    @PatchMapping("/event-categories/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventCategoryResponse> updateCategoryStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Event category status updated successfully.", service.updateCategoryStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/event-types")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<EventTypeResponse>> listTypes(@RequestParam(required = false) Long institutionId,
                                                                   @RequestParam(required = false) String search,
                                                                   @RequestParam(required = false) Boolean active,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Event types retrieved successfully.", service.listTypes(currentUserEmail(), institutionId, search, active, page, size));
    }

    @GetMapping("/event-types/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EventTypeResponse> getType(@PathVariable Long id) {
        return ApiResponseFactory.success("Event type retrieved successfully.", service.getType(currentUserEmail(), id));
    }

    @PostMapping("/event-types")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventTypeResponse> createType(@Valid @RequestBody EventTypeRequest request) {
        return ApiResponseFactory.success("Event type created successfully.", service.createType(currentUserEmail(), request));
    }

    @PutMapping("/event-types/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventTypeResponse> updateType(@PathVariable Long id, @Valid @RequestBody EventTypeRequest request) {
        return ApiResponseFactory.success("Event type updated successfully.", service.updateType(currentUserEmail(), id, request));
    }

    @PatchMapping("/event-types/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventTypeResponse> updateTypeStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Event type status updated successfully.", service.updateTypeStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/venues")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<VenueResponse>> listVenues(@RequestParam(required = false) Long institutionId,
                                                               @RequestParam(required = false) String search,
                                                               @RequestParam(required = false) VenueType venueType,
                                                               @RequestParam(required = false) Boolean active,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Venues retrieved successfully.", service.listVenues(currentUserEmail(), institutionId, search, venueType, active, page, size));
    }

    @GetMapping("/venues/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<VenueResponse> getVenue(@PathVariable Long id) {
        return ApiResponseFactory.success("Venue retrieved successfully.", service.getVenue(currentUserEmail(), id));
    }

    @PostMapping("/venues")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        return ApiResponseFactory.success("Venue created successfully.", service.createVenue(currentUserEmail(), request));
    }

    @PutMapping("/venues/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<VenueResponse> updateVenue(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
        return ApiResponseFactory.success("Venue updated successfully.", service.updateVenue(currentUserEmail(), id, request));
    }

    @PatchMapping("/venues/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<VenueResponse> updateVenueStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Venue status updated successfully.", service.updateVenueStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/events")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<EventSummaryResponse>> listEvents(@RequestParam(required = false) Long institutionId,
                                                                       @RequestParam(required = false) String search,
                                                                       @RequestParam(required = false) EventStatus status,
                                                                       @RequestParam(required = false) EventMode mode,
                                                                       @RequestParam(required = false) Long categoryId,
                                                                       @RequestParam(required = false) Long typeId,
                                                                       @RequestParam(required = false) Long departmentId,
                                                                       @RequestParam(required = false) Long venueId,
                                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Events retrieved successfully.", service.listEvents(currentUserEmail(), institutionId, search, status, mode, categoryId, typeId, departmentId, venueId, from, to, page, size));
    }

    @GetMapping("/events/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EventResponse> getEvent(@PathVariable Long id) {
        return ApiResponseFactory.success("Event retrieved successfully.", service.getEvent(currentUserEmail(), id));
    }

    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ApiResponseFactory.success("Event created successfully.", service.createEvent(currentUserEmail(), request));
    }

    @PutMapping("/events/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventResponse> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ApiResponseFactory.success("Event updated successfully.", service.updateEvent(currentUserEmail(), id, request));
    }

    @PatchMapping("/events/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventResponse> updateEventStatus(@PathVariable Long id, @Valid @RequestBody EventStatusRequest request) {
        return ApiResponseFactory.success("Event status updated successfully.", service.updateEventStatus(currentUserEmail(), id, request));
    }

    @GetMapping("/events/{eventId}/overview")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EventOverviewResponse> getEventOverview(@PathVariable Long eventId) {
        return ApiResponseFactory.success("Event overview retrieved successfully.", service.getEventOverview(currentUserEmail(), eventId));
    }

    @GetMapping("/events/{eventId}/sessions")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<EventSessionResponse>> listSessions(@PathVariable Long eventId) {
        return ApiResponseFactory.success("Event sessions retrieved successfully.", service.listEventSessions(currentUserEmail(), eventId));
    }

    @PostMapping("/events/{eventId}/sessions")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventSessionResponse> createSession(@PathVariable Long eventId, @Valid @RequestBody EventSessionRequest request) {
        return ApiResponseFactory.success("Event session created successfully.", service.createEventSession(currentUserEmail(), eventId, request));
    }

    @PutMapping("/events/{eventId}/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventSessionResponse> updateSession(@PathVariable Long eventId, @PathVariable Long sessionId, @Valid @RequestBody EventSessionRequest request) {
        return ApiResponseFactory.success("Event session updated successfully.", service.updateEventSession(currentUserEmail(), eventId, sessionId, request));
    }

    @PatchMapping("/events/{eventId}/sessions/{sessionId}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventSessionResponse> updateSessionStatus(@PathVariable Long eventId, @PathVariable Long sessionId, @RequestParam boolean active) {
        return ApiResponseFactory.success("Event session status updated successfully.", service.updateEventSessionStatus(currentUserEmail(), eventId, sessionId, active));
    }

    @GetMapping("/events/{eventId}/coordinators")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<EventCoordinatorResponse>> listCoordinators(@PathVariable Long eventId) {
        return ApiResponseFactory.success("Event coordinators retrieved successfully.", service.listEventCoordinators(currentUserEmail(), eventId));
    }

    @PostMapping("/events/{eventId}/coordinators")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventCoordinatorResponse> assignCoordinator(@PathVariable Long eventId, @Valid @RequestBody EventCoordinatorRequest request) {
        return ApiResponseFactory.success("Event coordinator assigned successfully.", service.assignCoordinator(currentUserEmail(), eventId, request));
    }

    @PutMapping("/events/{eventId}/coordinators/{coordinatorId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventCoordinatorResponse> updateCoordinator(@PathVariable Long eventId, @PathVariable Long coordinatorId, @Valid @RequestBody EventCoordinatorRequest request) {
        return ApiResponseFactory.success("Event coordinator updated successfully.", service.updateCoordinator(currentUserEmail(), eventId, coordinatorId, request));
    }

    @DeleteMapping("/events/{eventId}/coordinators/{coordinatorId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<String> removeCoordinator(@PathVariable Long eventId, @PathVariable Long coordinatorId) {
        service.removeCoordinator(currentUserEmail(), eventId, coordinatorId);
        return ApiResponseFactory.success("Event coordinator removed successfully.", "Coordinator assignment removed.");
    }

    @GetMapping("/events/{eventId}/eligibility-rules")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<EventEligibilityRuleResponse>> listEligibilityRules(@PathVariable Long eventId) {
        return ApiResponseFactory.success("Event eligibility rules retrieved successfully.", service.listEligibilityRules(currentUserEmail(), eventId));
    }

    @PostMapping("/events/{eventId}/eligibility-rules")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventEligibilityRuleResponse> createEligibilityRule(@PathVariable Long eventId, @Valid @RequestBody EventEligibilityRuleRequest request) {
        return ApiResponseFactory.success("Event eligibility rule created successfully.", service.createEligibilityRule(currentUserEmail(), eventId, request));
    }

    @PutMapping("/events/{eventId}/eligibility-rules/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventEligibilityRuleResponse> updateEligibilityRule(@PathVariable Long eventId, @PathVariable Long ruleId, @Valid @RequestBody EventEligibilityRuleRequest request) {
        return ApiResponseFactory.success("Event eligibility rule updated successfully.", service.updateEligibilityRule(currentUserEmail(), eventId, ruleId, request));
    }

    @PatchMapping("/events/{eventId}/eligibility-rules/{ruleId}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventEligibilityRuleResponse> updateEligibilityRuleStatus(@PathVariable Long eventId, @PathVariable Long ruleId, @RequestParam boolean active) {
        return ApiResponseFactory.success("Event eligibility rule status updated successfully.", service.updateEligibilityStatus(currentUserEmail(), eventId, ruleId, active));
    }

    @GetMapping("/events/{eventId}/registration-config")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EventRegistrationConfigResponse> getRegistrationConfig(@PathVariable Long eventId) {
        return ApiResponseFactory.success("Event registration configuration retrieved successfully.", service.getRegistrationConfig(currentUserEmail(), eventId));
    }

    @PutMapping("/events/{eventId}/registration-config")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','FACULTY_COORDINATOR')")
    public ApiResponse<EventRegistrationConfigResponse> upsertRegistrationConfig(@PathVariable Long eventId, @Valid @RequestBody EventRegistrationConfigRequest request) {
        return ApiResponseFactory.success("Event registration configuration saved successfully.", service.upsertRegistrationConfig(currentUserEmail(), eventId, request));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
