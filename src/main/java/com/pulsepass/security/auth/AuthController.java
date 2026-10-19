package com.pulsepass.security.auth;

import com.pulsepass.security.service.JwtService;
import com.pulsepass.security.service.PulsePassUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PulsePassUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          PulsePassUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRequests.TokenResponse> login(@Valid @RequestBody AuthRequests.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password()));
        UserDetails details = (UserDetails) authentication.getPrincipal();
        if (!(details instanceof UserPrincipal principal) || !principal.isEnabled()) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return ResponseEntity.ok(new AuthRequests.TokenResponse("Bearer", jwtService.generateToken(principal), jwtService.getExpirationSeconds()));
    }
}
