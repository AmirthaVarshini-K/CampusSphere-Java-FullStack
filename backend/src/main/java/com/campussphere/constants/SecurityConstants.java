package com.campussphere.constants;

public final class SecurityConstants {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/error",
            "/actuator/health",
            "/v1/system/**",
            "/auth/login",
            "/api/auth/login",
            "/auth/register/student",
            "/api/auth/register/student",
            "/auth/forgot-password",
            "/api/auth/forgot-password",
            "/auth/reset-password",
            "/api/auth/reset-password",
            "/auth/refresh-token",
            "/api/auth/refresh-token"
    };

    private SecurityConstants() {
    }
}
