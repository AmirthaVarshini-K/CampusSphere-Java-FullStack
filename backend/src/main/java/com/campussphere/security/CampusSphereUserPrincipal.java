package com.campussphere.security;

import com.campussphere.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CampusSphereUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final List<GrantedAuthority> authorities;

    public CampusSphereUserPrincipal(Long id, String email, String passwordHash, boolean enabled, boolean accountNonLocked,
                                     List<GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
    }

    public static CampusSphereUserPrincipal fromUser(User user, List<GrantedAuthority> authorities) {
        return new CampusSphereUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                !user.isDeleted() && user.getStatus() != null && user.getStatus() != com.campussphere.entity.RecordStatus.DELETED,
                user.getLockedUntil() == null || user.getLockedUntil().isBefore(java.time.Instant.now()),
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmailAddress() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public static List<GrantedAuthority> authoritiesFromRoles(Collection<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.stream()
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);
        return authorities;
    }
}
