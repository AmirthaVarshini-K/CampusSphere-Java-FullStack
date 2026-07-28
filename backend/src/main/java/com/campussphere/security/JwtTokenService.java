package com.campussphere.security;

import com.campussphere.config.ApplicationProperties;
import com.campussphere.entity.Role;
import com.campussphere.entity.User;
import com.campussphere.exception.ExpiredTokenException;
import com.campussphere.exception.InvalidTokenException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;

    public JwtTokenService(ApplicationProperties applicationProperties, ObjectMapper objectMapper) {
        this.applicationProperties = applicationProperties;
        this.objectMapper = objectMapper;
    }

    public String generateAccessToken(User user, List<Role> roles) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(applicationProperties.getSecurity().getJwt().getAccessTokenMinutes(), ChronoUnit.MINUTES);
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", user.getEmail());
        claims.put("uid", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", roles.stream().map(role -> role.getCode().name()).toList());
        claims.put("typ", "access");
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", expiresAt.getEpochSecond());
        claims.put("iss", applicationProperties.getSecurity().getJwt().getIssuer());
        return sign(claims);
    }

    public JwtClaims parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidTokenException("Token format is invalid.");
        }

        String header = parts[0];
        String payload = parts[1];
        String signature = parts[2];
        String signedContent = header + "." + payload;
        String expectedSignature = signBytes(signedContent.getBytes(StandardCharsets.UTF_8));
        if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidTokenException("Token signature is invalid.");
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            Map<String, Object> claims = objectMapper.readValue(decoded, new TypeReference<>() {
            });
            long expiresAt = ((Number) claims.get("exp")).longValue();
            if (Instant.now().isAfter(Instant.ofEpochSecond(expiresAt))) {
                throw new ExpiredTokenException("Token has expired.");
            }
            String issuer = String.valueOf(claims.get("iss"));
            if (!applicationProperties.getSecurity().getJwt().getIssuer().equals(issuer)) {
                throw new InvalidTokenException("Token issuer is invalid.");
            }

            JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setUserId(Long.valueOf(String.valueOf(claims.get("uid"))));
            jwtClaims.setEmail(String.valueOf(claims.get("email")));
            jwtClaims.setTokenType(String.valueOf(claims.get("typ")));
            jwtClaims.setIssuedAt(Instant.ofEpochSecond(((Number) claims.get("iat")).longValue()));
            jwtClaims.setExpiresAt(Instant.ofEpochSecond(expiresAt));
            Object roles = claims.get("roles");
            if (roles instanceof List<?> roleList) {
                jwtClaims.setRoles(roleList.stream().map(String::valueOf).toList());
            }
            return jwtClaims;
        } catch (ExpiredTokenException | InvalidTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidTokenException("Token payload is invalid.");
        }
    }

    public String generateRefreshToken() {
        byte[] random = new byte[48];
        SECURE_RANDOM.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random) + "." + UUID.randomUUID();
    }

    public Instant accessTokenExpiry() {
        return Instant.now().plus(applicationProperties.getSecurity().getJwt().getAccessTokenMinutes(), ChronoUnit.MINUTES);
    }

    public Instant refreshTokenExpiry(boolean rememberMe) {
        long days = rememberMe ? applicationProperties.getSecurity().getJwt().getRefreshTokenDays() * 2 : applicationProperties.getSecurity().getJwt().getRefreshTokenDays();
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }

    public Instant resetTokenExpiry() {
        return Instant.now().plus(applicationProperties.getSecurity().getJwt().getResetTokenMinutes(), ChronoUnit.MINUTES);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : hashed) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash token.", exception);
        }
    }

    private String sign(Map<String, Object> claims) {
        try {
            String headerJson = objectMapper.writeValueAsString(Map.of("alg", "HS256", "typ", "JWT"));
            String payloadJson = objectMapper.writeValueAsString(claims);
            String header = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signature = signBytes((header + "." + payload).getBytes(StandardCharsets.UTF_8));
            return header + "." + payload + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate token.", exception);
        }
    }

    private String signBytes(byte[] value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    applicationProperties.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(secretKey);
            byte[] signature = mac.doFinal(value);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign token.", exception);
        }
    }
}
