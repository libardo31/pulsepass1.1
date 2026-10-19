package com.pulsepass.security.web;

import com.pulsepass.security.service.JwtService;
import com.pulsepass.security.service.PulsePassUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final PulsePassUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, PulsePassUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parseAndValidate(header.substring(7).trim());
                Long userId = Long.valueOf(claims.getSubject());
                UserDetails details = userDetailsService.loadById(userId);
                if (!details.isEnabled()) {
                    SecurityContextHolder.clearContext();
                } else {
                    var authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException | org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"timestamp\":\"" + java.time.LocalDateTime.now() + "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or expired token\",\"details\":{}}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
