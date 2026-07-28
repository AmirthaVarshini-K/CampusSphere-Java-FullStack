package com.campussphere.service;

import com.campussphere.dto.auth.AuthResponse;
import com.campussphere.dto.auth.ChangePasswordRequest;
import com.campussphere.dto.auth.ForgotPasswordRequest;
import com.campussphere.dto.auth.LoginRequest;
import com.campussphere.dto.auth.LogoutRequest;
import com.campussphere.dto.auth.PasswordResetResponse;
import com.campussphere.dto.auth.RefreshTokenRequest;
import com.campussphere.dto.auth.ResetPasswordRequest;
import com.campussphere.dto.auth.StudentRegistrationRequest;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    void logout(LogoutRequest request);

    AuthResponse registerStudent(StudentRegistrationRequest request, HttpServletRequest httpRequest);

    PasswordResetResponse forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request, String currentUserEmail);

    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);
}
