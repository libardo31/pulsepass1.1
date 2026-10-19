package com.pulsepass.dto.response;

import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EventSummaryResponse(
        Long id,
        String eventCode,
        String name,
        EventCategory category,
        EventStatus status,
        LocalDateTime eventDate,
        String venueCode,
        String venueName,
        List<String> artists
) {
}