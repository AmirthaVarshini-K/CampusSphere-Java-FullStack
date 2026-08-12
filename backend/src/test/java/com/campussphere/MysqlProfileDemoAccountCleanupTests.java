package com.campussphere;

import com.campussphere.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("mysql")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:campussphere-mysql-profile;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS campussphere",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.default_schema=campussphere",
        "spring.flyway.enabled=true",
        "spring.flyway.default-schema=campussphere",
        "campus-sphere.security.jwt.secret=campussphere-mysql-demo-secret"
})
class MysqlProfileDemoAccountCleanupTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void normalMysqlProfileDoesNotKeepDemoAccounts() {
        assertThat(userRepository.existsByEmailIgnoreCaseAndDeletedFalse("admin@campussphere.local")).isFalse();
        assertThat(userRepository.existsByEmailIgnoreCaseAndDeletedFalse("student@campussphere.local")).isFalse();
        assertThat(userRepository.existsByEmailIgnoreCaseAndDeletedFalse("coordinator@campussphere.local")).isFalse();
    }
}
