package com.pulsepass.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterUserRequest(

        @NotBlank(message = "Username is required")
        @Size(max = 60)
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 180)
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @Size(max = 30)
        String phone,

        @Size(max = 100)
        String city,

        @NotNull(message = "Birth date is required")
        @PastOrPresent(message = "Birth date cannot be in the future")
        LocalDate birthDate
) {
}