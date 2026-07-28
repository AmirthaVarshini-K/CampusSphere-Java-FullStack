package com.campussphere.config;

import com.campussphere.entity.Institution;
import com.campussphere.entity.InstitutionType;
import com.campussphere.entity.Permission;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.Role;
import com.campussphere.entity.RoleCode;
import com.campussphere.entity.User;
import com.campussphere.entity.UserRole;
import com.campussphere.repository.InstitutionRepository;
import com.campussphere.repository.PermissionRepository;
import com.campussphere.repository.RoleRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Component
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties applicationProperties;

    public DataSeeder(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            InstitutionRepository institutionRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            ApplicationProperties applicationProperties
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationProperties = applicationProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        seedPermissions();
        seedRoles();
        Institution institution = seedInstitution();
        seedAdminUser();
        seedFacultyUser(institution);
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
                "FAC-1001",
                "Computer Science",
                null,
                null,
                null
        );
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
}
