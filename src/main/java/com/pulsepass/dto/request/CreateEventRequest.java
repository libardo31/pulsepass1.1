package com.pulsepass.dto.request;

import com.pulsepass.domain.EventCategory;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateEventRequest(

        @NotBlank(message = "Event code is required")
        @Size(max = 40)
        String eventCode,

        @NotBlank(message = "Event name is required")
        @Size(max = 160)
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Category is required")
        EventCategory category,

        @NotNull(message = "Event date is required")
        @Future(message = "Event date must be in the future")
        LocalDateTime eventDate,

        @NotNull(message = "Minimum age is required")
        @Min(value = 0, message = "Minimum age cannot be negative")
        Integer minimumAge,

        @NotBlank(message = "Venue code is required")
        @Size(max = 40)
        String venueCode
) {
}