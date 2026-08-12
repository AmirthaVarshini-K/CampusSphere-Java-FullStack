package com.campussphere;

import com.campussphere.config.DataSeeder;
import com.campussphere.entity.RoleCode;
import com.campussphere.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityFoundationMigrationTests {

    @MockBean
    private DataSeeder dataSeeder;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void flywaySeedsSystemRolesForFreshDatabase() {
        assertThat(roleRepository.findByCodeAndDeletedFalse(RoleCode.ADMINISTRATOR)).isPresent();
        assertThat(roleRepository.findByCodeAndDeletedFalse(RoleCode.FACULTY_COORDINATOR)).isPresent();
        assertThat(roleRepository.findByCodeAndDeletedFalse(RoleCode.STUDENT)).isPresent();
    }
}
