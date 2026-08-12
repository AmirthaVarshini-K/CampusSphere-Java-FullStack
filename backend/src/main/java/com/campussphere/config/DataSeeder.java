package com.campussphere.config;

import com.campussphere.entity.Institution;
import com.campussphere.entity.InstitutionType;
import com.campussphere.entity.Permission;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.Role;
import com.campussphere.entity.RoleCode;
import com.campussphere.entity.User;
import com.campussphere.entity.UserRole;
import com.campussphere.entity.AcademicYear;
import com.campussphere.entity.Department;
import com.campussphere.entity.DepartmentProgramme;
import com.campussphere.entity.Programme;
import com.campussphere.entity.ProgrammeLevel;
import com.campussphere.entity.Section;
import com.campussphere.entity.Semester;
import com.campussphere.repository.InstitutionRepository;
import com.campussphere.repository.AcademicYearRepository;
import com.campussphere.repository.DepartmentProgrammeRepository;
import com.campussphere.repository.DepartmentRepository;
import com.campussphere.repository.ProgrammeRepository;
import com.campussphere.repository.PermissionRepository;
import com.campussphere.repository.RoleRepository;
import com.campussphere.repository.SectionRepository;
import com.campussphere.repository.SemesterRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Component
@Profile({"local", "dev", "test", "mysql-demo"})
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ProgrammeRepository programmeRepository;
    private final DepartmentProgrammeRepository departmentProgrammeRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties applicationProperties;
    private final Environment environment;

    public DataSeeder(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            AcademicYearRepository academicYearRepository,
            ProgrammeRepository programmeRepository,
            DepartmentProgrammeRepository departmentProgrammeRepository,
            SemesterRepository semesterRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            ApplicationProperties applicationProperties,
            Environment environment
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.academicYearRepository = academicYearRepository;
        this.programmeRepository = programmeRepository;
        this.departmentProgrammeRepository = departmentProgrammeRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationProperties = applicationProperties;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        seedPermissions();
        seedRoles();
        Institution institution = seedInstitution();
        seedAdminUser();
        seedFacultyUser(institution);
        if (isDevelopmentDataProfile()) {
            seedLocalAcademicFoundation(institution);
            seedStudentUser(institution);
        }
    }

    private void seedPermissions() {
        List.of(
                "USER_READ", "USER_WRITE", "USER_DELETE", "ROLE_ASSIGN",
                "EVENT_READ", "EVENT_WRITE", "ATTENDANCE_VERIFY", "CERTIFICATE_GENERATE",
                "REPORT_VIEW", "SYSTEM_CONFIG"
        ).forEach(code -> {
            if (!permissionRepository.existsByCodeAndDeletedFalse(code)) {
                Permission permission = new Permission();
                permission.setCode(code);
                permission.setName(code.replace('_', ' '));
                permission.setModule("AUTH");
                permission.setDescription("Auto-seeded permission for CampusSphere foundation.");
                permissionRepository.save(permission);
            }
        });
    }

    private void seedRoles() {
        ensureRole(RoleCode.ADMINISTRATOR, "Administrator", "Full system access", true,
                List.of("USER_READ", "USER_WRITE", "USER_DELETE", "ROLE_ASSIGN", "EVENT_READ", "EVENT_WRITE", "ATTENDANCE_VERIFY", "CERTIFICATE_GENERATE", "REPORT_VIEW", "SYSTEM_CONFIG"));
        ensureRole(RoleCode.FACULTY_COORDINATOR, "Faculty Coordinator", "Academic event coordination access", true,
                List.of("EVENT_READ", "EVENT_WRITE", "ATTENDANCE_VERIFY", "CERTIFICATE_GENERATE", "REPORT_VIEW"));
        ensureRole(RoleCode.STUDENT, "Student", "Student portal access", true,
                List.of("EVENT_READ", "REPORT_VIEW"));
    }

    private void ensureRole(RoleCode roleCode, String name, String description, boolean systemRole, List<String> permissionCodes) {
        Role role = roleRepository.findByCodeAndDeletedFalse(roleCode).orElseGet(Role::new);
        role.setCode(roleCode);
        role.setName(name);
        role.setDescription(description);
        role.setSystemRole(systemRole);
        role.setPermissions(new LinkedHashSet<>(permissionRepository.findAll().stream()
                .filter(permission -> permissionCodes.contains(permission.getCode()))
                .toList()));
        roleRepository.save(role);
    }

    private Institution seedInstitution() {
        return institutionRepository.findByInstitutionCodeIgnoreCaseAndDeletedFalse("CS-001").orElseGet(() -> {
            Institution institution = new Institution();
            institution.setInstitutionCode("CS-001");
            institution.setInstitutionName("CampusSphere Institute of Technology");
            institution.setShortName("CSIT");
            institution.setInstitutionType(InstitutionType.COLLEGE);
            institution.setAffiliation("Autonomous");
            institution.setEmail("info@campussphere-institute.edu");
            institution.setPhone("+91 90000 00001");
            institution.setCity("Bengaluru");
            institution.setState("Karnataka");
            institution.setCountry("India");
            institution.setStatus(RecordStatus.ACTIVE);
            return institutionRepository.save(institution);
        });
    }

    private void seedAdminUser() {
        seedAccount(
                applicationProperties.getSeed().getAdminEmail(),
                applicationProperties.getSeed().getAdminPassword(),
                "System",
                "Administrator",
                RoleCode.ADMINISTRATOR,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private void seedFacultyUser(Institution institution) {
        seedAccount(
                applicationProperties.getSeed().getFacultyEmail(),
                applicationProperties.getSeed().getFacultyPassword(),
                "Faculty",
                "Coordinator",
                RoleCode.FACULTY_COORDINATOR,
                institution,
                resolveFacultyEmployeeId(),
                "Computer Science",
                null,
                null,
                null
        );
    }

    private void seedStudentUser(Institution institution) {
        seedAccount(
                LOCAL_STUDENT_EMAIL,
                LOCAL_STUDENT_PASSWORD,
                "Asha",
                "Menon",
                RoleCode.STUDENT,
                institution,
                null,
                "Computer Science and Engineering",
                "III",
                "A",
                LOCAL_STUDENT_REGISTER_NUMBER
        );
    }

    private void seedLocalAcademicFoundation(Institution institution) {
        Department department = departmentRepository.findAll().stream()
                .filter(existing -> existing.getInstitution() != null
                        && existing.getInstitution().getId() != null
                        && existing.getInstitution().getId().equals(institution.getId())
                        && "CSE".equalsIgnoreCase(existing.getDepartmentCode()))
                .findFirst()
                .orElseGet(() -> {
                    Department created = new Department();
                    created.setInstitution(institution);
                    created.setDepartmentCode("CSE");
                    created.setDepartmentName("Computer Science and Engineering");
                    created.setShortName("CSE");
                    created.setDescription("Department seeded for local student registration and development login.");
                    created.setDepartmentEmail("cse@campussphere.local");
                    created.setDepartmentPhone("+91 90000 00010");
                    return departmentRepository.save(created);
                });

        AcademicYear academicYear = academicYearRepository.findAll().stream()
                .filter(existing -> existing.getInstitution() != null
                        && existing.getInstitution().getId() != null
                        && existing.getInstitution().getId().equals(institution.getId())
                        && "2026-2027".equalsIgnoreCase(existing.getYearLabel()))
                .findFirst()
                .orElseGet(() -> {
                    AcademicYear created = new AcademicYear();
                    created.setInstitution(institution);
                    created.setYearLabel("2026-2027");
                    created.setStartDate(LocalDate.of(2026, 6, 1));
                    created.setEndDate(LocalDate.of(2027, 5, 31));
                    created.setCurrentYear(true);
                    created.setRegistrationOpen(true);
                    return academicYearRepository.save(created);
                });

        Programme programme = programmeRepository.findAll().stream()
                .filter(existing -> existing.getInstitution() != null
                        && existing.getInstitution().getId() != null
                        && existing.getInstitution().getId().equals(institution.getId())
                        && "BTECH-CSE".equalsIgnoreCase(existing.getProgrammeCode()))
                .findFirst()
                .orElseGet(() -> {
                    Programme created = new Programme();
                    created.setInstitution(institution);
                    created.setProgrammeCode("BTECH-CSE");
                    created.setProgrammeName("B.Tech Computer Science and Engineering");
                    created.setProgrammeLevel(ProgrammeLevel.UNDERGRADUATE);
                    created.setDurationYears(4);
                    created.setDurationSemesters(8);
                    return programmeRepository.save(created);
                });

        if (!departmentProgrammeRepository.existsByInstitution_IdAndDepartment_IdAndProgramme_IdAndDeletedFalse(institution.getId(), department.getId(), programme.getId())) {
            DepartmentProgramme mapping = new DepartmentProgramme();
            mapping.setInstitution(institution);
            mapping.setDepartment(department);
            mapping.setProgramme(programme);
            mapping.setAcademicYear(academicYear);
            mapping.setIntakeCapacity(60);
            departmentProgrammeRepository.save(mapping);
        }

        Semester semester = semesterRepository.findAll().stream()
                .filter(existing -> existing.getInstitution() != null
                        && existing.getInstitution().getId() != null
                        && existing.getInstitution().getId().equals(institution.getId())
                        && existing.getProgramme() != null
                        && existing.getProgramme().getId() != null
                        && existing.getProgramme().getId().equals(programme.getId())
                        && existing.getSemesterNumber() == 5)
                .findFirst()
                .orElseGet(() -> {
                    Semester created = new Semester();
                    created.setInstitution(institution);
                    created.setProgramme(programme);
                    created.setSemesterNumber(5);
                    created.setDisplayName("Semester 5");
                    return semesterRepository.save(created);
                });

        if (!sectionRepository.existsByInstitution_IdAndDepartment_IdAndProgramme_IdAndAcademicYear_IdAndSemester_IdAndSectionNameIgnoreCaseAndDeletedFalse(
                institution.getId(), department.getId(), programme.getId(), academicYear.getId(), semester.getId(), "A")) {
            Section section = new Section();
            section.setInstitution(institution);
            section.setDepartment(department);
            section.setProgramme(programme);
            section.setAcademicYear(academicYear);
            section.setSemester(semester);
            section.setSectionName("A");
            section.setStudyYear(3);
            section.setCapacity(60);
            sectionRepository.save(section);
        }
    }

    private void seedAccount(String email, String password, String firstName, String lastName, RoleCode roleCode,
                             Institution institution,
                             String employeeId, String department, String academicYear, String section, String registerNumber) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(email)) {
            return;
        }
        User user = new User();
        user.setInstitution(institution);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email.toLowerCase(Locale.ROOT));
        user.setEmployeeId(employeeId);
        user.setDepartment(department);
        user.setAcademicYear(academicYear);
        user.setSection(section);
        user.setRegisterNumber(registerNumber);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(RecordStatus.ACTIVE);
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(roleRepository.findByCodeAndDeletedFalse(roleCode).orElseThrow());
        user.getUserRoles().add(userRole);
        userRoleRepository.save(userRole);

        log.info("Seeded {} account: {}", roleCode, email);
    }

    private boolean isDevelopmentDataProfile() {
        return List.of(environment.getActiveProfiles()).stream()
                .map(profile -> profile == null ? "" : profile.trim().toLowerCase(Locale.ROOT))
                .anyMatch(profile -> "local".equals(profile) || "dev".equals(profile) || "test".equals(profile) || "mysql-demo".equals(profile));
    }

    private String resolveFacultyEmployeeId() {
        return List.of(environment.getActiveProfiles()).stream()
                .map(profile -> profile == null ? "" : profile.trim().toLowerCase(Locale.ROOT))
                .anyMatch("mysql-demo"::equals)
                ? "FAC-1001"
                : "FAC-2001";
    }

    private static final String LOCAL_STUDENT_EMAIL = "student@campussphere.local";
    private static final String LOCAL_STUDENT_PASSWORD = "Student@Local123!";
    private static final String LOCAL_STUDENT_REGISTER_NUMBER = "24CSE0001";
}
