package com.campussphere.service.impl;

import com.campussphere.dto.user.UserCreateRequest;
import com.campussphere.dto.user.UserProfileResponse;
import com.campussphere.dto.user.UserSummaryResponse;
import com.campussphere.dto.user.UserUpdateRequest;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.Role;
import com.campussphere.entity.RoleCode;
import com.campussphere.entity.User;
import com.campussphere.entity.UserRole;
import com.campussphere.exception.DuplicateResourceException;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.mapper.UserMapper;
import com.campussphere.repository.RoleRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserProfileResponse getCurrentUser(String currentUserEmail) {
        User user = findByEmail(currentUserEmail);
        return userMapper.toProfileResponse(user, getRoles(user));
    }

    @Override
    public UserProfileResponse updateCurrentUser(String currentUserEmail, UserUpdateRequest request) {
        User user = findByEmail(currentUserEmail);
        return updateUserInternal(user, request);
    }

    @Override
    public List<UserSummaryResponse> listUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .map(user -> userMapper.toSummaryResponse(user, getRoles(user)))
                .toList();
    }

    @Override
    public UserProfileResponse getUser(Long id) {
        User user = userRepository.findWithRolesByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return userMapper.toProfileResponse(user, getRoles(user));
    }

    @Override
    public UserProfileResponse createUser(UserCreateRequest request) {
        validatePassword(request.getPassword(), request.getConfirmPassword());
        validateRoleSpecificFields(request);
        ensureUniqueIdentity(request.getEmail(), request.getRegisterNumber(), request.getEmployeeId());

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setRegisterNumber(trimToNull(request.getRegisterNumber()));
        user.setEmployeeId(trimToNull(request.getEmployeeId()));
        user.setDepartment(trimToNull(request.getDepartment()));
        user.setAcademicYear(trimToNull(request.getAcademicYear()));
        user.setSection(trimToNull(request.getSection()));
        user.setPhoneNumber(trimToNull(request.getPhoneNumber()));
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.isActive() ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
        assignRole(user, request.getRoleCode());
        return userMapper.toProfileResponse(user, getRoles(user));
    }

    @Override
    public UserProfileResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findWithRolesByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return updateUserInternal(user, request);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());
        user.setStatus(RecordStatus.DELETED);
        userRepository.save(user);
    }

    private UserProfileResponse updateUserInternal(User user, UserUpdateRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getEmail() != null) {
            String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCaseAndDeletedFalse(email)) {
                throw new DuplicateResourceException("Email already exists.");
            }
            user.setEmail(email);
        }
        if (request.getRegisterNumber() != null) {
            String registerNumber = request.getRegisterNumber().trim();
            String existingRegisterNumber = user.getRegisterNumber();
            if ((existingRegisterNumber == null || !registerNumber.equalsIgnoreCase(existingRegisterNumber))
                    && userRepository.existsByRegisterNumberIgnoreCaseAndDeletedFalse(registerNumber)) {
                throw new DuplicateResourceException("Register number already exists.");
            }
            user.setRegisterNumber(registerNumber);
        }
        if (request.getEmployeeId() != null) {
            String employeeId = request.getEmployeeId().trim();
            String existingEmployeeId = user.getEmployeeId();
            if ((existingEmployeeId == null || !employeeId.equalsIgnoreCase(existingEmployeeId))
                    && userRepository.existsByEmployeeIdIgnoreCaseAndDeletedFalse(employeeId)) {
                throw new DuplicateResourceException("Employee ID already exists.");
            }
            user.setEmployeeId(employeeId);
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment().trim());
        }
        if (request.getAcademicYear() != null) {
            user.setAcademicYear(request.getAcademicYear().trim());
        }
        if (request.getSection() != null) {
            user.setSection(request.getSection().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(trimToNull(request.getProfilePictureUrl()));
        }
        if (request.getTermsAccepted() != null) {
            user.setTermsAccepted(request.getTermsAccepted());
        }
        userRepository.save(user);
        return userMapper.toProfileResponse(user, getRoles(user));
    }

    private User findByEmail(String email) {
        return userRepository.findWithRolesByEmailIgnoreCaseAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private List<Role> getRoles(User user) {
        return user.getUserRoles().stream().map(UserRole::getRole).toList();
    }

    private void assignRole(User user, RoleCode roleCode) {
        Role role = roleRepository.findByCodeAndDeletedFalse(roleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleCode));
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        user.getUserRoles().add(userRole);
        userRepository.save(user);
    }

    private void ensureUniqueIdentity(String email, String registerNumber, String employeeId) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(email.trim().toLowerCase(Locale.ROOT))) {
            throw new DuplicateResourceException("Email already exists.");
        }
        if (registerNumber != null && !registerNumber.isBlank() && userRepository.existsByRegisterNumberIgnoreCaseAndDeletedFalse(registerNumber.trim())) {
            throw new DuplicateResourceException("Register number already exists.");
        }
        if (employeeId != null && !employeeId.isBlank() && userRepository.existsByEmployeeIdIgnoreCaseAndDeletedFalse(employeeId.trim())) {
            throw new DuplicateResourceException("Employee ID already exists.");
        }
    }

    private void validateRoleSpecificFields(UserCreateRequest request) {
        if (request.getRoleCode() == RoleCode.STUDENT && (request.getRegisterNumber() == null || request.getRegisterNumber().isBlank())) {
            throw new IllegalArgumentException("Register number is required for student accounts.");
        }
        if (request.getRoleCode() == RoleCode.FACULTY_COORDINATOR && (request.getEmployeeId() == null || request.getEmployeeId().isBlank())) {
            throw new IllegalArgumentException("Employee ID is required for faculty accounts.");
        }
    }

    private void validatePassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
