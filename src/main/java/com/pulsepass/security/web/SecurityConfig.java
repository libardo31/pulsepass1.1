package com.pulsepass.security.web;

import com.pulsepass.security.service.PulsePassUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean public DaoAuthenticationProvider daoAuthenticationProvider(
            PulsePassUserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean public JwtAuthenticationFilter jwtAuthenticationFilter(
            com.pulsepass.security.service.JwtService jwtService,
            PulsePassUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtFilter, RestAuthenticationEntryPoint entryPoint,
            RestAccessDeniedHandler deniedHandler, DaoAuthenticationProvider provider) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(provider)
            .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/users").permitAll()
                .requestMatchers("/api/events/published", "/api/events/by-artist", "/api/venues/**", "/api/artists/**").permitAll()
                .requestMatchers("/api/events/*/tickets/paid").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/events/*/publish", "/api/events/*/artists/*", "/api/events").hasRole("ADMIN")
                .requestMatchers("/api/tickets/*/use").hasAnyRole("STAFF", "ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
