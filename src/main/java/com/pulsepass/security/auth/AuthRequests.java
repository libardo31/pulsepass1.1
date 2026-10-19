package com.pulsepass.security.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthRequests {
    private AuthRequests() { }
    public record LoginRequest(@NotBlank @Size(max = 180) String login, @NotBlank @Size(max = 72) String password) { }
    public record TokenResponse(String tokenType, String accessToken, long expiresIn) { }
}
