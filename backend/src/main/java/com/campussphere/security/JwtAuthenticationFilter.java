package com.campussphere.security;

import com.campussphere.dto.ApiResponse;
import com.campussphere.entity.Role;
import com.campussphere.entity.User;
import com.campussphere.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7).trim();
        try {
            JwtClaims claims = jwtTokenService.parseAndValidate(token);
            if (!"access".equals(claims.getTokenType())) {
                writeUnauthorized(response, "Token type is invalid.");
                return;
            }

            User user = userRepository.findWithRolesByIdAndDeletedFalse(claims.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found."));
            List<Role> roles = user.getUserRoles().stream().map(userRole -> userRole.getRole()).toList();
            CampusSphereUserPrincipal principal = CampusSphereUserPrincipal.fromUser(
                    user,
                    CampusSphereUserPrincipal.authoritiesFromRoles(roles.stream().map(role -> role.getCode().name()).toList())
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, exception.getMessage());
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(),
                ApiResponse.error(message == null ? "Authentication is required." : message,
                        Collections.singletonMap("authorization", Collections.singletonList("Access token is missing or invalid."))));
    }
}
