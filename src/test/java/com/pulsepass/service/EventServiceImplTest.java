package com.pulsepass.service;

import com.pulsepass.domain.*;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.mapper.PulsePassMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventServiceImplTest {
    private EventRepository events; private VenueRepository venues; private ArtistRepository artists; private PulsePassMapper mapper; private EventServiceImpl service;
    @BeforeEach void setUp(){events=mock(EventRepository.class);venues=mock(VenueRepository.class);artists=mock(ArtistRepository.class);mapper=mock(PulsePassMapper.class);service=new EventServiceImpl(events,venues,artists,mapper);}
    @Test void duplicateEventCodeIsRejected(){
        when(events.existsByEventCode("CMF-2026")).thenReturn(true);
        CreateEventRequest request=new CreateEventRequest("CMF-2026","Festival",null,EventCategory.MUSIC,LocalDateTime.now().plusDays(20),18,"VEN-SMR-01");
        assertThrows(DuplicateResourceException.class,()->service.create(request)); verifyNoInteractions(venues); verify(events,never()).save(any());
    }
    @Test void inactiveVenueCannotBeUsed(){
        when(events.existsByEventCode("CMF-2026")).thenReturn(false);
        Venue venue=new Venue();venue.setCode("VEN-SMR-01");venue.setActive(false);
        when(venues.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));
        CreateEventRequest request=new CreateEventRequest("CMF-2026","Festival",null,EventCategory.MUSIC,LocalDateTime.now().plusDays(20),18,"VEN-SMR-01");
        assertThrows(BusinessRuleException.class,()->service.create(request)); verify(events,never()).save(any());
    }

    @Test void validEventIsSavedInDraft(){
        when(events.existsByEventCode("CMF-2026")).thenReturn(false); Venue venue=new Venue();venue.setCode("VEN-SMR-01");venue.setName("Marina");venue.setActive(true);when(venues.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));
        when(events.save(any(Event.class))).thenAnswer(call->call.getArgument(0));
        com.pulsepass.dto.response.EventResponse expected=new com.pulsepass.dto.response.EventResponse(1L,"CMF-2026","Festival",null,EventCategory.MUSIC,EventStatus.DRAFT,LocalDateTime.now().plusDays(20),18,null,"VEN-SMR-01","Marina",java.util.List.of());
        when(mapper.toResponse(any(Event.class))).thenReturn(expected);
        var result=service.create(new CreateEventRequest("CMF-2026","Festival",null,EventCategory.MUSIC,LocalDateTime.now().plusDays(20),18,"VEN-SMR-01"));
        assertEquals(EventStatus.DRAFT,result.status());verify(events).save(argThat(event->event.getStatus()==EventStatus.DRAFT && event.getVenue()==venue));
    }
    @Test void missingVenuePreventsSaving(){when(events.existsByEventCode("CMF-2026")).thenReturn(false);when(venues.findByCode("VEN-SMR-01")).thenReturn(Optional.empty());CreateEventRequest request=new CreateEventRequest("CMF-2026","Festival",null,EventCategory.MUSIC,LocalDateTime.now().plusDays(20),18,"VEN-SMR-01");assertThrows(com.pulsepass.exception.ResourceNotFoundException.class,()->service.create(request));verify(events,never()).save(any());}
    @Test void pastEventDateIsRejected(){when(events.existsByEventCode("CMF-2026")).thenReturn(false);Venue venue=new Venue();venue.setActive(true);when(venues.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));CreateEventRequest request=new CreateEventRequest("CMF-2026","Festival",null,EventCategory.MUSIC,LocalDateTime.now().minusDays(1),18,"VEN-SMR-01");assertThrows(BusinessRuleException.class,()->service.create(request));verify(events,never()).save(any());}
    @Test void validDraftCanBePublished(){Event event=new Event();event.setEventCode("CMF-2026");event.setStatus(EventStatus.DRAFT);event.setEventDate(LocalDateTime.now().plusDays(20));Venue venue=new Venue();venue.setActive(true);event.setVenue(venue);when(events.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));when(events.save(event)).thenReturn(event);when(mapper.toResponse(event)).thenReturn(new com.pulsepass.dto.response.EventResponse(1L,"CMF-2026","Festival",null,EventCategory.MUSIC,EventStatus.PUBLISHED,event.getEventDate(),18,null,"VEN-SMR-01","Marina",java.util.List.of()));assertEquals(EventStatus.PUBLISHED,service.publish("CMF-2026").status());assertEquals(EventStatus.PUBLISHED,event.getStatus());}
    @Test void cancelledEventCannotBePublished(){Event event=new Event();event.setEventCode("CMF-2026");event.setStatus(EventStatus.CANCELLED);event.setEventDate(LocalDateTime.now().plusDays(20));when(events.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));assertThrows(BusinessRuleException.class,()->service.publish("CMF-2026"));verify(events,never()).save(any());}
    @Test void missingEventThrowsNotFound(){when(events.findByEventCode("MISSING")).thenReturn(Optional.empty());assertThrows(com.pulsepass.exception.ResourceNotFoundException.class,()->service.findByCode("MISSING"));}

}
