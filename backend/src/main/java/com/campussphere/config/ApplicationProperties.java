package com.campussphere.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campus-sphere")
public class ApplicationProperties {

    private String appName;
    private final Cors cors = new Cors();
    private final Security security = new Security();
    private final Auth auth = new Auth();
    private final Seed seed = new Seed();
    private String frontendBaseUrl;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Cors getCors() {
        return cors;
    }

    public Security getSecurity() {
        return security;
    }

    public Auth getAuth() {
        return auth;
    }

    public Seed getSeed() {
        return seed;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public static class Cors {
        private String allowedOrigins;

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Security {
        private final Jwt jwt = new Jwt();

        public Jwt getJwt() {
            return jwt;
        }
    }

    public static class Jwt {
        private String issuer;
        private String secret;
        private long accessTokenMinutes = 30;
        private long refreshTokenDays = 14;
        private long resetTokenMinutes = 20;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenMinutes() {
            return accessTokenMinutes;
        }

        public void setAccessTokenMinutes(long accessTokenMinutes) {
            this.accessTokenMinutes = accessTokenMinutes;
        }

        public long getRefreshTokenDays() {
            return refreshTokenDays;
        }

        public void setRefreshTokenDays(long refreshTokenDays) {
            this.refreshTokenDays = refreshTokenDays;
        }

        public long getResetTokenMinutes() {
            return resetTokenMinutes;
        }

        public void setResetTokenMinutes(long resetTokenMinutes) {
            this.resetTokenMinutes = resetTokenMinutes;
        }
    }

    public static class Auth {
        private int maxFailedAttempts = 5;
        private long lockMinutes = 15;

        public int getMaxFailedAttempts() {
            return maxFailedAttempts;
        }

        public void setMaxFailedAttempts(int maxFailedAttempts) {
            this.maxFailedAttempts = maxFailedAttempts;
        }

        public long getLockMinutes() {
            return lockMinutes;
        }

        public void setLockMinutes(long lockMinutes) {
            this.lockMinutes = lockMinutes;
        }
    }

    public static class Seed {
        private String adminEmail;
        private String adminPassword;
        private String facultyEmail;
        private String facultyPassword;

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public String getFacultyEmail() {
            return facultyEmail;
        }

        public void setFacultyEmail(String facultyEmail) {
            this.facultyEmail = facultyEmail;
        }

        public String getFacultyPassword() {
            return facultyPassword;
        }

        public void setFacultyPassword(String facultyPassword) {
            this.facultyPassword = facultyPassword;
        }
    }
}
