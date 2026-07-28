package com.campussphere.service.support;

import com.campussphere.entity.Role;
import com.campussphere.entity.RoleCode;
import com.campussphere.entity.User;
import com.campussphere.entity.UserRole;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CurrentUserContext {

    private final UserRepository userRepository;

    public CurrentUserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireCurrentUser(String email) {
        if (email == null || email.isBlank()) {
            throw new ResourceNotFoundException("Current user was not found.");
        }
        return userRepository.findWithRolesByEmailIgnoreCaseAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user was not found."));
    }

    public boolean hasRole(User user, RoleCode roleCode) {
        return roles(user).stream().anyMatch(role -> role.getCode() == roleCode);
    }

    public boolean isAdministrator(User user) {
        return hasRole(user, RoleCode.ADMINISTRATOR);
    }

    public boolean isFaculty(User user) {
        return hasRole(user, RoleCode.FACULTY_COORDINATOR);
    }

    public boolean isStudent(User user) {
        return hasRole(user, RoleCode.STUDENT);
    }

    public List<Role> roles(User user) {
        return user.getUserRoles().stream().map(UserRole::getRole).toList();
    }

    public String currentEmail(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }
}
