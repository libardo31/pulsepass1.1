package com.pulsepass.security.service;

import com.pulsepass.domain.User;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.security.auth.UserPrincipal;
import com.pulsepass.security.repository.UserCredentialRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PulsePassUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    private final UserCredentialRepository credentials;
    private final JdbcTemplate jdbc;

    public PulsePassUserDetailsService(UserRepository users, UserCredentialRepository credentials, JdbcTemplate jdbc) {
        this.users = users;
        this.credentials = credentials;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = users.findByUsername(login)
                .or(() -> users.findByEmailIgnoreCase(login))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return buildPrincipal(user);
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadById(Long id) {
        User user = users.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return buildPrincipal(user);
    }

    private UserPrincipal buildPrincipal(User user) {
        var credential = credentials.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        List<String> roles = jdbc.queryForList(
                "select role from user_roles where user_id = ? order by role", String.class, user.getId());
        return new UserPrincipal(user.getId(), user.getUsername(), user.getEmail(),
                credential.getPasswordHash(), user.isActive(), roles);
    }
}
