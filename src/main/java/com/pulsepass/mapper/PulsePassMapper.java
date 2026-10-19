package com.pulsepass.mapper;

import com.pulsepass.domain.*;
import com.pulsepass.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PulsePassMapper {

    VenueResponse toResponse(Venue venue);

    ArtistResponse toResponse(Artist artist);

    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(
            target = "artists",
            expression = "java(event.getArtists().stream().map(artist -> artist.getStageName()).sorted().toList())"
    )
    EventResponse toResponse(Event event);

    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(
            target = "artists",
            expression = "java(event.getArtists().stream().map(artist -> artist.getStageName()).sorted().toList())"
    )
    EventSummaryResponse toSummary(Event event);

    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName", source = "profile.lastName")
    @Mapping(target = "phone", source = "profile.phone")
    @Mapping(target = "city", source = "profile.city")
    @Mapping(target = "birthDate", source = "profile.birthDate")
    UserResponse toResponse(User user);

    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "eventCode", source = "event.eventCode")
    @Mapping(target = "eventName", source = "event.name")
    TicketResponse toResponse(Ticket ticket);

    List<EventSummaryResponse> toSummaries(List<Event> events);
}