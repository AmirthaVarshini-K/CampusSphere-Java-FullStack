package com.campussphere.controller;

import com.campussphere.dto.ApiResponse;
import com.campussphere.dto.auth.AuthResponse;
import com.campussphere.dto.auth.ChangePasswordRequest;
import com.campussphere.dto.auth.ForgotPasswordRequest;
import com.campussphere.dto.auth.LoginRequest;
import com.campussphere.dto.auth.LogoutRequest;
import com.campussphere.dto.auth.PasswordResetResponse;
import com.campussphere.dto.auth.RefreshTokenRequest;
import com.campussphere.dto.auth.ResetPasswordRequest;
import com.campussphere.dto.auth.StudentRegistrationRequest;
import com.campussphere.service.AuthService;
import com.campussphere.util.ApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponseFactory.success("Login successful.", authService.login(request, httpRequest));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponseFactory.success("Logout successful.", "Session ended successfully.");
    }

    @PostMapping("/register/student")
    public ApiResponse<AuthResponse> registerStudent(@Valid @RequestBody StudentRegistrationRequest request, HttpServletRequest httpRequest) {
        return ApiResponseFactory.success("Student registration successful.", authService.registerStudent(request, httpRequest));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        return ApiResponseFactory.success("Password reset instructions generated.", authService.forgotPassword(request, httpRequest));
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponseFactory.success("Password reset successful.", "Password has been updated.");
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request, currentUserEmail());
        return ApiResponseFactory.success("Password changed successfully.", "Password has been updated.");
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        return ApiResponseFactory.success("Token refreshed successfully.", authService.refreshToken(request, httpRequest));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
