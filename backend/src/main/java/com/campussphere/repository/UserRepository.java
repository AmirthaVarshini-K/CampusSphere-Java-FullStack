package com.campussphere.repository;

import com.campussphere.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.permissions"})
    Optional<User> findWithRolesByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.permissions"})
    Optional<User> findWithRolesByEmailIgnoreCaseAndDeletedFalse(String email);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.permissions"})
    Optional<User> findWithRolesByRegisterNumberIgnoreCaseAndDeletedFalse(String registerNumber);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.permissions"})
    Optional<User> findWithRolesByEmployeeIdIgnoreCaseAndDeletedFalse(String employeeId);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByRegisterNumberIgnoreCaseAndDeletedFalse(String registerNumber);

    boolean existsByEmployeeIdIgnoreCaseAndDeletedFalse(String employeeId);
}
