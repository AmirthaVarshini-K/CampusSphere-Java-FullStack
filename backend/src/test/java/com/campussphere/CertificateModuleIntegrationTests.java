package com.campussphere;

import com.campussphere.entity.Institution;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.Role;
import com.campussphere.entity.RoleCode;
import com.campussphere.entity.User;
import com.campussphere.entity.UserRole;
import com.campussphere.entity.certificate.CertificateTemplate;
import com.campussphere.entity.certificate.CertificateTemplateOrientation;
import com.campussphere.entity.certificate.CertificateType;
import com.campussphere.entity.event.Event;
import com.campussphere.entity.event.EventCategory;
import com.campussphere.entity.event.EventMode;
import com.campussphere.entity.event.EventRegistrationConfig;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.event.EventType;
import com.campussphere.entity.event.EventVisibility;
import com.campussphere.entity.event.Venue;
import com.campussphere.entity.event.VenueType;
import com.campussphere.entity.registration.AttendanceStatus;
import com.campussphere.entity.registration.EventRegistration;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.entity.registration.RegistrationType;
import com.campussphere.repository.CertificateRepository;
import com.campussphere.repository.CertificateTemplateRepository;
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
class CertificateModuleIntegrationTests {

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
    private CertificateTemplateRepository templateRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Test
    void issueVerifyAndRevokeCertificateFlowWorks() throws Exception {
        Event event = createEvent("CERT-" + uniqueSuffix(), 100);
        User student = createStudent("cert-student-" + uniqueSuffix() + "@campussphere.edu", "24CE" + uniqueSuffix());
        createEligibleRegistration(event, student);
        CertificateTemplate template = createTemplate(event.getInstitution());

        String adminToken = login("admin@campussphere.edu", "Admin@1234!");

        JsonNode issueResponse = performApiCall(adminToken, post("/api/certificates"), """
                {
                  "eventId": %d,
                  "recipientUserId": %d,
                  "certificateType": "PARTICIPATION",
                  "templateId": %d,
                  "recipientRole": "Participant",
                  "attendancePercentage": 92,
                  "adminOverride": false,
                  "remarks": "Issued for testing"
                }
                """.formatted(event.getId(), student.getId(), template.getId()), status().isOk());

        String token = issueResponse.path("data").path("verificationToken").asText();
        assertThat(token).isNotBlank();
        assertThat(certificateRepository.findByVerificationTokenAndDeletedFalse(token)).isPresent();

        JsonNode verifyResponse = performApiCall(null, get("/api/certificates/verify/" + token), null, status().isOk());
        assertThat(verifyResponse.path("data").path("valid").asBoolean()).isTrue();

        Long certificateId = issueResponse.path("data").path("id").asLong();
        performApiCall(adminToken, post("/api/certificates/" + certificateId + "/revoke"), """
                {"reason":"Testing revocation"}
                """, status().isOk());

        JsonNode revokedResponse = performApiCall(null, get("/api/certificates/verify/" + token), null, status().isOk());
        assertThat(revokedResponse.path("data").path("revoked").asBoolean()).isTrue();
    }

    private CertificateTemplate createTemplate(Institution institution) {
        CertificateTemplate template = new CertificateTemplate();
        template.setInstitution(institution);
        template.setTemplateCode("CERT-" + uniqueSuffix());
        template.setTemplateName("Participation Template " + uniqueSuffix());
        template.setCertificateType(CertificateType.PARTICIPATION);
        template.setOrientation(CertificateTemplateOrientation.PORTRAIT);
        template.setDescription("Test template");
        template.setVerificationUrlBase("http://localhost:5173");
        template.setQrCodeEnabled(true);
        template.setActive(true);
        return templateRepository.saveAndFlush(template);
    }

    private void createEligibleRegistration(Event event, User student) {
        EventRegistration registration = new EventRegistration();
        registration.setInstitution(event.getInstitution());
        registration.setEvent(event);
        registration.setParticipant(student);
        registration.setRegistrationNumber("REG-" + uniqueSuffix());
        registration.setRegistrationType(RegistrationType.INDIVIDUAL);
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        registration.setRegistrationDate(LocalDateTime.now().plusMinutes(5));
        registration.setAttendanceStatus(AttendanceStatus.PRESENT);
        registration.setCertificateEligible(true);
        registration.setRemarks("Eligible for certificates");
        registrationRepository.saveAndFlush(registration);
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

        JsonNode loginJson = objectMapper.readTree(loginResponse).path("data");
        return loginJson.path("accessToken").asText();
    }

    private JsonNode performApiCall(String token, org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, String body, org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
        var builder = request;
        if (token != null) {
            builder = builder.header("Authorization", "Bearer " + token);
        }
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

        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        Event event = new Event();
        event.setInstitution(institution);
        event.setTitle("Certificate Test " + code);
        event.setEventCode(code);
        event.setSlug(code.toLowerCase(Locale.ROOT));
        event.setShortDescription("Certificate test event");
        event.setFullDescription("Certificate test event description");
        event.setEventCategory(category);
        event.setEventType(type);
        event.setVenue(venue);
        event.setMode(EventMode.OFFLINE);
        event.setVisibility(EventVisibility.INSTITUTION_ONLY);
        event.setEventStatus(EventStatus.COMPLETED);
        event.setStartDateTime(start);
        event.setEndDateTime(end);
        event.setRegistrationStartDateTime(start.minusDays(1));
        event.setRegistrationEndDateTime(end.minusHours(1));
        event.setMaximumParticipants(maximumParticipants);
        event.setMinimumParticipants(1);
        event.setContactEmail("events@campussphere.edu");
        event.setContactPhone("9000000000");
        eventRepository.saveAndFlush(event);

        EventRegistrationConfig config = new EventRegistrationConfig();
        config.setEvent(event);
        config.setRegistrationRequired(true);
        config.setApprovalRequired(false);
        config.setWaitlistEnabled(false);
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
        user.setFirstName("Certificate");
        user.setLastName("Student");
        user.setEmail(email.toLowerCase(Locale.ROOT));
        user.setRegisterNumber(registerNumber);
        user.setDepartment("Computer Science");
        user.setAcademicYear("III");
        user.setSection("A");
        user.setPhoneNumber("9876543210");
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

    private String uniqueSuffix() {
        return String.valueOf(Math.abs(System.nanoTime() % 100000));
    }
}
