package com.campussphere;

import com.campussphere.entity.RoleCode;
import com.campussphere.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("mysql-demo")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:campussphere-demo;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS campussphere",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.default_schema=campussphere",
        "spring.flyway.enabled=true",
        "spring.flyway.default-schema=campussphere",
        "campus-sphere.security.jwt.secret=campussphere-mysql-demo-secret"
})
@Transactional
class DemoAccountSeedTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void flywaySeedsTheThreeMySqlDemoAccountsWithBcryptPasswordsAndRoles() {
        var admin = userRepository.findWithRolesByEmailIgnoreCaseAndDeletedFalse("admin@campussphere.local").orElseThrow();
        var faculty = userRepository.findWithRolesByEmailIgnoreCaseAndDeletedFalse("coordinator@campussphere.local").orElseThrow();
        var student = userRepository.findWithRolesByEmailIgnoreCaseAndDeletedFalse("student@campussphere.local").orElseThrow();

        assertThat(admin.getPasswordHash()).startsWith("$2a$");
        assertThat(faculty.getPasswordHash()).startsWith("$2a$");
        assertThat(student.getPasswordHash()).startsWith("$2a$");

        assertThat(passwordEncoder.matches("Admin@Local123!", admin.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("Faculty@Local123!", faculty.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("Student@Local123!", student.getPasswordHash())).isTrue();

        assertThat(admin.getUserRoles()).extracting(userRole -> userRole.getRole().getCode())
                .contains(RoleCode.ADMINISTRATOR);
        assertThat(faculty.getUserRoles()).extracting(userRole -> userRole.getRole().getCode())
                .contains(RoleCode.FACULTY_COORDINATOR);
        assertThat(student.getUserRoles()).extracting(userRole -> userRole.getRole().getCode())
                .contains(RoleCode.STUDENT);

        assertThat(faculty.getInstitution()).isNotNull();
        assertThat(faculty.getInstitution().getInstitutionCode()).isEqualTo("CS-001");
        assertThat(student.getInstitution()).isNotNull();
        assertThat(student.getInstitution().getInstitutionCode()).isEqualTo("CS-001");
        assertThat(faculty.getEmployeeId()).isEqualTo("FAC-1001");
    }
}
