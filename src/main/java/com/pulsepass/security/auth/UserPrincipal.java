package com.pulsepass.security.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public final class UserPrincipal implements UserDetails {
    private final Long id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String passwordHash,
                         boolean active, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .map(GrantedAuthority.class::cast).toList();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
