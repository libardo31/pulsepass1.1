package com.pulsepass.security.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override public void handle(HttpServletRequest request, HttpServletResponse response,
                                 AccessDeniedException exception) throws IOException, ServletException {
        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to perform this action\",\"details\":{}}");
    }
}
