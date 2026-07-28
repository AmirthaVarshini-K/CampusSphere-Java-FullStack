package com.campussphere.service.impl;

import com.campussphere.config.ApplicationProperties;
import com.campussphere.dto.auth.AuthResponse;
import com.campussphere.dto.auth.ChangePasswordRequest;
import com.campussphere.dto.auth.ForgotPasswordRequest;
import com.campussphere.dto.auth.LoginRequest;
import com.campussphere.dto.auth.LogoutRequest;
import com.campussphere.dto.auth.PasswordResetResponse;
import com.campussphere.dto.auth.RefreshTokenRequest;
import com.campussphere.dto.auth.ResetPasswordRequest;
import com.campussphere.dto.auth.StudentRegistrationRequest;
import com.campussphere.dto.user.UserProfileResponse;
import com.campussphere.entity.PasswordResetToken;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.RefreshToken;
import com.campussphere.entity.Role;
import com.campussphere.entity.RoleCode;
import com.campussphere.entity.User;
import com.campussphere.entity.UserRole;
import com.campussphere.exception.AccountLockedException;
import com.campussphere.exception.BadCredentialsException;
import com.campussphere.exception.DuplicateResourceException;
import com.campussphere.exception.InvalidTokenException;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.mapper.UserMapper;
import com.campussphere.repository.PasswordResetTokenRepository;
import com.campussphere.repository.RefreshTokenRepository;
import com.campussphere.repository.RoleRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.security.JwtTokenService;
import com.campussphere.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserMapper userMapper;
    private final ApplicationProperties applicationProperties;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            UserMapper userMapper,
            ApplicationProperties applicationProperties
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.userMapper = userMapper;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = findUserByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials."));

        verifyAccountState(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new BadCredentialsException("Invalid credentials.");
        }

        registerSuccessfulLogin(user, httpRequest);
        List<Role> roles = user.getUserRoles().stream().map(UserRole::getRole).toList();
        return buildAuthResponse(user, roles, request.isRememberMe(), httpRequest, null);
    }

    @Override
    public void logout(LogoutRequest request) {
        String hashed = jwtTokenService.hashToken(request.getRefreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndDeletedFalse(hashed)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid."));
        refreshToken.setRevokedAt(Instant.now());
        refreshToken.setStatus(RecordStatus.DELETED);
        refreshToken.setDeleted(true);
        refreshToken.setDeletedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
        log.info("Logout recorded for user {}", refreshToken.getUser().getEmail());
    }

    @Override
    public AuthResponse registerStudent(StudentRegistrationRequest request, HttpServletRequest httpRequest) {
        validatePasswords(request.getPassword(), request.getConfirmPassword());
        ensureUniqueStudentIdentity(request.getEmail(), request.getRegisterNumber());

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setRegisterNumber(request.getRegisterNumber().trim());
        user.setDepartment(request.getDepartment().trim());
        user.setAcademicYear(request.getAcademicYear().trim());
        user.setSection(request.getSection().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setProfilePictureUrl(trimToNull(request.getProfilePictureUrl()));
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setTermsAccepted(request.isTermsAccepted());
        user.setStatus(RecordStatus.ACTIVE);
        user.setPasswordChangedAt(Instant.now());
        User saved = userRepository.save(user);
        assignRole(saved, RoleCode.STUDENT);

        log.info("Student registered: {}", saved.getEmail());
        List<Role> roles = saved.getUserRoles().stream().map(UserRole::getRole).toList();
        return buildAuthResponse(saved, roles, false, httpRequest, "Student account created successfully.");
    }

    @Override
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findWithRolesByEmailIgnoreCaseAndDeletedFalse(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("No account found for the provided email."));
        String resetToken = jwtTokenService.generateRefreshToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(jwtTokenService.hashToken(resetToken));
        token.setExpiresAt(jwtTokenService.resetTokenExpiry());
        token.setRequestIp(extractIp(httpRequest));
        token.setRequestUserAgent(httpRequest.getHeader("User-Agent"));
        passwordResetTokenRepository.save(token);

        log.info("Password reset requested for {}", user.getEmail());
        PasswordResetResponse response = new PasswordResetResponse();
        response.setResetToken(resetToken);
        response.setExpiresAt(token.getExpiresAt());
        response.setMessage("Password reset token generated successfully.");
        return response;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        validatePasswords(request.getNewPassword(), request.getConfirmPassword());
        String hashedToken = jwtTokenService.hashToken(request.getToken().trim());
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndDeletedFalse(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Reset token is invalid."));
        if (token.getUsedAt() != null || Instant.now().isAfter(token.getExpiresAt())) {
            throw new InvalidTokenException("Reset token has expired.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setStatus(RecordStatus.ACTIVE);
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        token.setStatus(RecordStatus.DELETED);
        token.setDeleted(true);
        token.setDeletedAt(Instant.now());
        passwordResetTokenRepository.save(token);
        log.info("Password reset completed for {}", user.getEmail());
    }

    @Override
    public void changePassword(ChangePasswordRequest request, String currentUserEmail) {
        validatePasswords(request.getNewPassword(), request.getConfirmPassword());
        User user = userRepository.findWithRolesByEmailIgnoreCaseAndDeletedFalse(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user was not found."));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
        log.info("Password changed for {}", user.getEmail());
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String hashed = jwtTokenService.hashToken(request.getRefreshToken().trim());
        RefreshToken existing = refreshTokenRepository.findByTokenHashAndDeletedFalse(hashed)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid."));
        if (existing.getRevokedAt() != null || Instant.now().isAfter(existing.getExpiresAt())) {
            throw new InvalidTokenException("Refresh token has expired.");
        }
        existing.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(existing);
        existing.setRevokedAt(Instant.now());
        existing.setStatus(RecordStatus.DELETED);
        existing.setDeleted(true);
        existing.setDeletedAt(Instant.now());
        refreshTokenRepository.save(existing);
        User user = existing.getUser();
        List<Role> roles = user.getUserRoles().stream().map(UserRole::getRole).toList();
        return buildAuthResponse(user, roles, existing.isRememberMe(), httpRequest, "Token refreshed successfully.");
    }

    private AuthResponse buildAuthResponse(User user, List<Role> roles, boolean rememberMe, HttpServletRequest httpRequest, String message) {
        String accessToken = jwtTokenService.generateAccessToken(user, roles);
        String refreshTokenRaw = jwtTokenService.generateRefreshToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(jwtTokenService.hashToken(refreshTokenRaw));
        refreshToken.setExpiresAt(jwtTokenService.refreshTokenExpiry(rememberMe));
        refreshToken.setRememberMe(rememberMe);
        refreshToken.setDeviceName(resolveDeviceName(httpRequest));
        refreshToken.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        AuthResponse response = new AuthResponse();
        response.setTokenType("Bearer");
        response.setAccessToken(accessToken);
        response.setAccessTokenExpiresAt(jwtTokenService.accessTokenExpiry());
        response.setRefreshToken(refreshTokenRaw);
        response.setRefreshTokenExpiresAt(refreshToken.getExpiresAt());
        response.setUser(userMapper.toProfileResponse(user, roles));
        return response;
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        String normalized = identifier.trim();
        return userRepository.findWithRolesByEmailIgnoreCaseAndDeletedFalse(normalized.toLowerCase(Locale.ROOT))
                .or(() -> userRepository.findWithRolesByRegisterNumberIgnoreCaseAndDeletedFalse(normalized))
                .or(() -> userRepository.findWithRolesByEmployeeIdIgnoreCaseAndDeletedFalse(normalized));
    }

    private void verifyAccountState(User user) {
        if (user.getStatus() == RecordStatus.DELETED || user.isDeleted()) {
            throw new BadCredentialsException("Invalid credentials.");
        }
        if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
            throw new AccountLockedException("Your account is temporarily locked.");
        }
    }

    private void registerFailedAttempt(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= applicationProperties.getAuth().getMaxFailedAttempts()) {
            user.setLockedUntil(Instant.now().plus(applicationProperties.getAuth().getLockMinutes(), java.time.temporal.ChronoUnit.MINUTES));
            user.setStatus(RecordStatus.LOCKED);
        }
        userRepository.save(user);
    }

    private void registerSuccessfulLogin(User user, HttpServletRequest httpRequest) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        user.setLastLoginIp(extractIp(httpRequest));
        user.setStatus(RecordStatus.ACTIVE);
        userRepository.save(user);
        log.info("Successful login for {}", user.getEmail());
    }

    private void ensureUniqueStudentIdentity(String email, String registerNumber) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(email.trim().toLowerCase(Locale.ROOT))) {
            throw new DuplicateResourceException("Email already exists.");
        }
        if (userRepository.existsByRegisterNumberIgnoreCaseAndDeletedFalse(registerNumber.trim())) {
            throw new DuplicateResourceException("Register number already exists.");
        }
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

    private void validatePasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
    }

    private String resolveDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown device";
        }
        return userAgent.length() > 120 ? userAgent.substring(0, 120) : userAgent;
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
