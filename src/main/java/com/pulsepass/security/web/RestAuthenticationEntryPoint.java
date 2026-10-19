package com.pulsepass.security.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override public void commence(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required\",\"details\":{}}");
    }
}
