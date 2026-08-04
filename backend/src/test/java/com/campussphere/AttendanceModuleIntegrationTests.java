package com.campussphere;

import com.campussphere.entity.Institution;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.Role;
import com.campussphere.entity.RoleCode;
import com.campussphere.entity.User;
import com.campussphere.entity.UserRole;
import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventCategory;
import com.campussphere.entity.event.EventMode;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.event.EventType;
import com.campussphere.entity.event.EventVisibility;
import com.campussphere.entity.event.Venue;
import com.campussphere.entity.event.VenueType;
import com.campussphere.entity.event.EventRegistrationConfig;
import com.campussphere.entity.registration.AttendanceStatus;
import com.campussphere.entity.registration.EventRegistration;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.entity.registration.RegistrationType;
import com.campussphere.repository.AttendanceRecordRepository;
import com.campussphere.repository.AttendanceSessionRepository;
import com.campussphere.repository.EventCategoryRepository;
import com.campussphere.repository.EventRegistrationConfigRepository;
import com.campussphere.repository.EventRegistrationRepository;
import com.campussphere.repository.EventRepository;
import com.campussphere.repository.EventTypeRepository;
import com.campussphere.repository.InstitutionRepository;
import com.campussphere.repository.RoleRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.repository.UserRoleRepository;
import com.campussphere.repository.VenueRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AttendanceModuleIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationConfigRepository configRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Test
    void adminCanOpenAttendanceSessionAndRecordCheckIn() throws Exception {
        Event event = createEvent("ATT-" + uniqueSuffix(), 10);
        User student = createStudent("att-student-" + uniqueSuffix() + "@campussphere.edu", "24CS" + uniqueSuffix());
        createRegistration(event, student);
        String adminToken = login("admin@campussphere.edu", "Admin@1234!");

        JsonNode openSessionResponse = performApiCall(adminToken, post("/api/attendance/sessions"), """
                {"eventId":%d,"eventSessionId":null,"sessionTitle":"Main hall attendance","remarks":"Opening check-in"}
                """.formatted(event.getId()), status().isOk());
        Long sessionId = openSessionResponse.path("data").path("id").asLong();

        JsonNode tokenResponse = performApiCall(adminToken, post("/api/attendance/qr-tokens"), """
                {"registrationId":%d,"expiresInMinutes":30,"oneTimeUse":true}
                """.formatted(registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(student.getId()).get(0).getId()), status().isOk());
        String rawToken = tokenResponse.path("data").path("token").asText();

        JsonNode checkInResponse = performApiCall(adminToken, post("/api/attendance/check-in"), """
                {"token":"%s","attendanceSessionId":%d,"attendanceStatus":"PRESENT","attendanceMethod":"QR","remarks":"Checked in"}
                """.formatted(rawToken, sessionId), status().isOk());
        assertThat(checkInResponse.path("data").path("record").path("attendanceStatus").asText()).isEqualTo("PRESENT");

        mockMvc.perform(post("/api/attendance/check-in")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","attendanceSessionId":%d,"attendanceStatus":"PRESENT","attendanceMethod":"QR","remarks":"Duplicate"}
                                """.formatted(rawToken, sessionId)))
                .andExpect(status().isConflict());
    }

    @Test
    void studentCannotOpenAttendanceSession() throws Exception {
        User student = createStudent("attendance-student-" + uniqueSuffix() + "@campussphere.edu", "24IT" + uniqueSuffix());
        String studentToken = login(student.getEmail(), "Campus@1234!");
        mockMvc.perform(post("/api/attendance/sessions")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventId":1,"eventSessionId":null,"sessionTitle":"Blocked","remarks":""}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void attendanceDashboardIsAccessibleForAdmin() throws Exception {
        String adminToken = login("admin@campussphere.edu", "Admin@1234!");
        mockMvc.perform(get("/api/attendance/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private void createRegistration(Event event, User student) {
        EventRegistration registration = new EventRegistration();
        registration.setInstitution(event.getInstitution());
        registration.setEvent(event);
        registration.setParticipant(student);
        registration.setRegistrationNumber("REG-" + uniqueSuffix());
        registration.setRegistrationType(RegistrationType.INDIVIDUAL);
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        registration.setRegistrationDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(1));
        registration.setAttendanceStatus(AttendanceStatus.NOT_MARKED);
        registration.setCertificateEligible(false);
        registrationRepository.saveAndFlush(registration);
    }

    private Event createEvent(String code, int maximumParticipants) {
        Institution institution = institutionRepository.findAll().stream().findFirst().orElseThrow();
        EventCategory category = eventCategoryRepository.findAll().stream().findFirst().orElseGet(() -> {
            EventCategory created = new EventCategory();
            created.setInstitution(institution);
            created.setCategoryCode("CAT-" + uniqueSuffix());
            created.setCategoryName("Category " + uniqueSuffix());
            created.setDescription("Test category");
            return eventCategoryRepository.save(created);
        });
        EventType type = eventTypeRepository.findAll().stream().findFirst().orElseGet(() -> {
            EventType created = new EventType();
            created.setInstitution(institution);
            created.setTypeCode("TYPE-" + uniqueSuffix());
            created.setTypeName("Type " + uniqueSuffix());
            created.setDescription("Test type");
            return eventTypeRepository.save(created);
        });
        Venue venue = venueRepository.findAll().stream().findFirst().orElseGet(() -> {
            Venue created = new Venue();
            created.setInstitution(institution);
            created.setVenueCode("VEN-" + uniqueSuffix());
            created.setVenueName("Venue " + uniqueSuffix());
            created.setVenueType(VenueType.AUDITORIUM);
            created.setCapacity(100);
            created.setStatus(RecordStatus.ACTIVE);
            return venueRepository.save(created);
        });

        LocalDateTime start = LocalDateTime.now(java.time.ZoneOffset.UTC).minusHours(2).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(4);

        Event event = new Event();
        event.setInstitution(institution);
        event.setTitle("Attendance Test " + code);
        event.setEventCode(code);
        event.setSlug(code.toLowerCase(Locale.ROOT));
        event.setShortDescription("Test event");
        event.setFullDescription("Test event description");
        event.setEventCategory(category);
        event.setEventType(type);
        event.setVenue(venue);
        event.setMode(EventMode.OFFLINE);
        event.setVisibility(EventVisibility.INSTITUTION_ONLY);
        event.setEventStatus(EventStatus.ONGOING);
        event.setStartDateTime(start);
        event.setEndDateTime(end);
        event.setRegistrationStartDateTime(start.minusDays(1));
        event.setRegistrationEndDateTime(start.minusHours(1));
        event.setMaximumParticipants(maximumParticipants);
        event.setMinimumParticipants(1);
        event.setContactEmail("events@campussphere.edu");
        event.setContactPhone("9000000000");
        eventRepository.saveAndFlush(event);

        EventRegistrationConfig config = new EventRegistrationConfig();
        config.setEvent(event);
        config.setRegistrationRequired(true);
        config.setApprovalRequired(false);
        config.setWaitlistEnabled(true);
        config.setTeamEvent(false);
        config.setAllowExternalParticipants(false);
        config.setAllowMultipleRegistrations(false);
        config.setCertificateEnabled(true);
        config.setAttendanceRequiredForCertificate(true);
        config.setCancellationAllowed(true);
        configRepository.saveAndFlush(config);
        return event;
    }

    private User createStudent(String email, String registerNumber) {
        Institution institution = institutionRepository.findAll().stream().findFirst().orElseThrow();
        Role studentRole = roleRepository.findByCodeAndDeletedFalse(RoleCode.STUDENT).orElseThrow();

        User user = new User();
        user.setInstitution(institution);
        user.setFirstName("Test");
        user.setLastName("Student");
        user.setEmail(email.toLowerCase(Locale.ROOT));
        user.setRegisterNumber(registerNumber);
        user.setDepartment("Computer Science");
        user.setAcademicYear("III");
        user.setSection("A");
        user.setPhoneNumber("9876543210");
        user.setProfilePictureUrl(null);
        user.setPasswordHash(passwordEncoder.encode("Campus@1234!"));
        user.setTermsAccepted(true);
        user.setStatus(RecordStatus.ACTIVE);
        userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(studentRole);
        userRoleRepository.save(userRole);
        return user;
    }

    private JsonNode performApiCall(String token, org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, String body, org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
        var builder = request.header("Authorization", "Bearer " + token);
        if (body != null) {
            builder = builder.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        String response = mockMvc.perform(builder)
                .andExpect(matcher)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String login(String identifier, String password) throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s","rememberMe":false}
                                """.formatted(identifier, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode data = objectMapper.readTree(loginResponse).path("data");
        return data.path("accessToken").asText();
    }

    private String uniqueSuffix() {
        return String.valueOf(Math.abs(System.nanoTime() % 100000));
    }
}
