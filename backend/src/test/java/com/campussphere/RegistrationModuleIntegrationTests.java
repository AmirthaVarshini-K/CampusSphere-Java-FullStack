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
import com.campussphere.entity.event.EventRegistrationConfig;
import com.campussphere.entity.event.EventStatus;
import com.campussphere.entity.event.EventType;
import com.campussphere.entity.event.EventVisibility;
import com.campussphere.entity.event.Venue;
import com.campussphere.entity.event.VenueType;
import com.campussphere.entity.registration.EventRegistration;
import com.campussphere.entity.registration.RegistrationStatus;
import com.campussphere.entity.registration.RegistrationType;
import com.campussphere.repository.EventCategoryRepository;
import com.campussphere.repository.EventRegistrationConfigRepository;
import com.campussphere.repository.EventRegistrationRepository;
import com.campussphere.repository.EventRepository;
import com.campussphere.repository.EventTypeRepository;
import com.campussphere.repository.InstitutionRepository;
import com.campussphere.repository.RoleRepository;
import com.campussphere.repository.TeamInvitationRepository;
import com.campussphere.repository.TeamMemberRepository;
import com.campussphere.repository.TeamRepository;
import com.campussphere.repository.UserRoleRepository;
import com.campussphere.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationModuleIntegrationTests {

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
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamInvitationRepository teamInvitationRepository;

    @Test
    void duplicateRegistrationIsRejected() throws Exception {
        Event event = createEvent("DUP-" + uniqueSuffix(), 10);
        User student = createStudent("dup-" + uniqueSuffix() + "@campussphere.edu", "22CS" + uniqueSuffix());
        String token = login(student.getEmail(), "Campus@1234!");

        register(token, event.getId());
        mockMvc.perform(post("/api/events/" + event.getId() + "/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationType":"INDIVIDUAL","teamName":"","teamCode":"","remarks":"retry"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void overlappingRegistrationIsRejected() throws Exception {
        Event first = createEvent("OVR-A-" + uniqueSuffix(), 10);
        Event second = createEvent("OVR-B-" + uniqueSuffix(), 10);
        second.setStartDateTime(first.getStartDateTime().plusMinutes(30));
        second.setEndDateTime(first.getEndDateTime().plusMinutes(30));
        eventRepository.saveAndFlush(second);

        User student = createStudent("conflict-" + uniqueSuffix() + "@campussphere.edu", "22EC" + uniqueSuffix());
        String token = login(student.getEmail(), "Campus@1234!");

        register(token, first.getId());
        mockMvc.perform(post("/api/events/" + second.getId() + "/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationType":"INDIVIDUAL","teamName":"","teamCode":"","remarks":"conflict"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void waitlistParticipantIsPromotedAfterCancellation() throws Exception {
        Event event = createEvent("WAIT-" + uniqueSuffix(), 1);
        User firstStudent = createStudent("wait-a-" + uniqueSuffix() + "@campussphere.edu", "22ME" + uniqueSuffix());
        User secondStudent = createStudent("wait-b-" + uniqueSuffix() + "@campussphere.edu", "22ME" + uniqueSuffix());
        String firstToken = login(firstStudent.getEmail(), "Campus@1234!");
        String secondToken = login(secondStudent.getEmail(), "Campus@1234!");

        register(firstToken, event.getId());
        register(secondToken, event.getId());

        EventRegistration firstRegistration = registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(firstStudent.getId()).get(0);
        EventRegistration secondRegistration = registrationRepository.findByParticipant_IdAndDeletedFalseOrderByRegistrationDateDesc(secondStudent.getId()).get(0);
        assertThat(firstRegistration.getRegistrationStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(secondRegistration.getRegistrationStatus()).isEqualTo(RegistrationStatus.WAITLISTED);

        mockMvc.perform(post("/api/registrations/" + firstRegistration.getId() + "/cancel")
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk());

        EventRegistration promoted = registrationRepository.findByIdAndDeletedFalse(secondRegistration.getId()).orElseThrow();
        assertThat(promoted.getRegistrationStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(promoted.getWaitlistPosition()).isNull();
    }

    @Test
    void teamLifecycleSupportsInvitationAcceptanceTransferRemovalLeaveAndDelete() throws Exception {
        Event event = createTeamEvent("TEAM-" + uniqueSuffix(), 10, 3);
        User leader = createStudent("lead-" + uniqueSuffix() + "@campussphere.edu", "22CS" + uniqueSuffix());
        User memberOne = createStudent("mem1-" + uniqueSuffix() + "@campussphere.edu", "22CS" + uniqueSuffix());
        User memberTwo = createStudent("mem2-" + uniqueSuffix() + "@campussphere.edu", "22CS" + uniqueSuffix());

        String leaderToken = login(leader.getEmail(), "Campus@1234!");
        String memberOneToken = login(memberOne.getEmail(), "Campus@1234!");
        String memberTwoToken = login(memberTwo.getEmail(), "Campus@1234!");

        JsonNode createTeamResponse = performApiCall(leaderToken, post("/api/events/" + event.getId() + "/teams"), """
                {"teamName":"Campus Coders","teamCode":"CODERS"}
                """, status().isOk());
        Long teamId = createTeamResponse.path("data").path("id").asLong();

        JsonNode invitationResponse = performApiCall(leaderToken, post("/api/teams/" + teamId + "/invitations"), """
                {"invitedUserId":%d,"message":"Join the team"}
                """.formatted(memberOne.getId()), status().isOk());
        Long invitationId = invitationResponse.path("data").path("id").asLong();

        mockMvc.perform(post("/api/teams/" + teamId + "/invitations")
                        .header("Authorization", "Bearer " + leaderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"invitedUserId":%d,"message":"Duplicate"}
                                """.formatted(memberOne.getId())))
                .andExpect(status().isConflict());

        performApiCall(memberOneToken, post("/api/team-invitations/" + invitationId + "/accept"), null, status().isOk());

        assertThat(teamMemberRepository.findByTeam_IdAndDeletedFalse(teamId)).hasSize(2);

        JsonNode inviteTwoResponse = performApiCall(leaderToken, post("/api/teams/" + teamId + "/invitations"), """
                {"invitedUserId":%d,"message":"Join as well"}
                """.formatted(memberTwo.getId()), status().isOk());
        Long invitationTwoId = inviteTwoResponse.path("data").path("id").asLong();
        performApiCall(memberTwoToken, post("/api/team-invitations/" + invitationTwoId + "/accept"), null, status().isOk());
        assertThat(teamMemberRepository.findByTeam_IdAndDeletedFalse(teamId)).hasSize(3);

        performApiCall(leaderToken, get("/api/teams/" + teamId + "/members"), null, status().isOk());
        Long memberTwoTeamMemberId = teamMemberRepository.findByTeam_IdAndUser_IdAndDeletedFalse(teamId, memberTwo.getId()).orElseThrow().getId();

        performApiCall(leaderToken, put("/api/teams/" + teamId + "/transfer"), """
                {"newLeaderUserId":%d}
                """.formatted(memberOne.getId()), status().isOk());

        performApiCall(leaderToken, post("/api/teams/" + teamId + "/leave"), null, status().isOk());
        assertThat(teamMemberRepository.findByTeam_IdAndDeletedFalse(teamId)).hasSize(2);

        performApiCall(memberOneToken, post("/api/teams/" + teamId + "/members/" + memberTwoTeamMemberId + "/remove"), null, status().isOk());
        assertThat(teamMemberRepository.findByTeam_IdAndDeletedFalse(teamId)).hasSize(1);

        performApiCall(memberOneToken, post("/api/teams/" + teamId + "/delete"), null, status().isOk());
        assertThat(teamRepository.findByIdAndDeletedFalse(teamId)).isEmpty();
    }

    @Test
    void oversizedTeamInvitationAcceptanceIsRejected() throws Exception {
        Event event = createTeamEvent("OVRTEAM-" + uniqueSuffix(), 10, 2);
        User leader = createStudent("ovr-lead-" + uniqueSuffix() + "@campussphere.edu", "22IT" + uniqueSuffix());
        User memberOne = createStudent("ovr-m1-" + uniqueSuffix() + "@campussphere.edu", "22IT" + uniqueSuffix());
        User memberTwo = createStudent("ovr-m2-" + uniqueSuffix() + "@campussphere.edu", "22IT" + uniqueSuffix());

        String leaderToken = login(leader.getEmail(), "Campus@1234!");
        String memberOneToken = login(memberOne.getEmail(), "Campus@1234!");
        String memberTwoToken = login(memberTwo.getEmail(), "Campus@1234!");

        JsonNode createTeamResponse = performApiCall(leaderToken, post("/api/events/" + event.getId() + "/teams"), """
                {"teamName":"Limit Breakers","teamCode":"LIMIT"}
                """, status().isOk());
        Long teamId = createTeamResponse.path("data").path("id").asLong();

        JsonNode firstInvite = performApiCall(leaderToken, post("/api/teams/" + teamId + "/invitations"), """
                {"invitedUserId":%d,"message":"Join"}
                """.formatted(memberOne.getId()), status().isOk());
        performApiCall(memberOneToken, post("/api/team-invitations/" + firstInvite.path("data").path("id").asLong() + "/accept"), null, status().isOk());

        JsonNode secondInvite = performApiCall(leaderToken, post("/api/teams/" + teamId + "/invitations"), """
                {"invitedUserId":%d,"message":"Join too"}
                """.formatted(memberTwo.getId()), status().isOk());
        performApiCall(memberTwoToken, post("/api/team-invitations/" + secondInvite.path("data").path("id").asLong() + "/accept"), null, status().isBadRequest());
    }

    private void register(String token, Long eventId) throws Exception {
        mockMvc.perform(post("/api/events/" + eventId + "/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationType":"INDIVIDUAL","teamName":"","teamCode":"","remarks":""}
                                """))
                .andExpect(status().isOk());
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
        event.setTitle("Registration Test " + code);
        event.setEventCode(code);
        event.setSlug(code.toLowerCase(Locale.ROOT));
        event.setShortDescription("Test event");
        event.setFullDescription("Test event description");
        event.setEventCategory(category);
        event.setEventType(type);
        event.setVenue(venue);
        event.setMode(EventMode.OFFLINE);
        event.setVisibility(EventVisibility.INSTITUTION_ONLY);
        event.setEventStatus(EventStatus.REGISTRATION_OPEN);
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
        config.setWaitlistEnabled(true);
        config.setTeamEvent(false);
        config.setAllowExternalParticipants(false);
        config.setAllowMultipleRegistrations(false);
        config.setCertificateEnabled(false);
        config.setAttendanceRequiredForCertificate(false);
        config.setCancellationAllowed(true);
        configRepository.saveAndFlush(config);
        return event;
    }

    private Event createTeamEvent(String code, int maximumParticipants, int maximumTeamSize) {
        Event event = createEvent(code, maximumParticipants);
        EventRegistrationConfig config = configRepository.findByEvent_IdAndDeletedFalse(event.getId()).orElseThrow();
        config.setTeamEvent(true);
        config.setMinimumTeamSize(1);
        config.setMaximumTeamSize(maximumTeamSize);
        configRepository.saveAndFlush(config);
        return event;
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

    private String uniqueSuffix() {
        return String.valueOf(Math.abs(System.nanoTime() % 100000));
    }
}
