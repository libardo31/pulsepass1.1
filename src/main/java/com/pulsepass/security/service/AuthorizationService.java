package com.pulsepass.security.service;

import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.security.auth.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("authorizationService")
public class AuthorizationService {
    private final TicketRepository tickets;
    private final UserRepository users;

    public AuthorizationService(TicketRepository tickets, UserRepository users) {
        this.tickets = tickets;
        this.users = users;
    }

    public boolean isSelfOrAdmin(Authentication authentication, String email) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) return false;
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) return true;
        return principal.getEmail().equalsIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public boolean ownsTicketOrIsAdmin(Authentication authentication, String ticketCode) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) return false;
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) return true;
        return tickets.findByTicketCode(ticketCode).map(t -> t.getUser().getId().equals(principal.getId())).orElse(false);
    }

    public boolean isSelfOrAdminByUsername(Authentication authentication, String username) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) return false;
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) return true;
        return principal.getUsername().equals(username);
    }
}
