package com.pulsepass.service.impl;

import com.pulsepass.domain.*;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.PulsePassMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository; private final VenueRepository venueRepository; private final ArtistRepository artistRepository; private final PulsePassMapper mapper;
    public EventServiceImpl(EventRepository eventRepository, VenueRepository venueRepository, ArtistRepository artistRepository, PulsePassMapper mapper){this.eventRepository=eventRepository;this.venueRepository=venueRepository;this.artistRepository=artistRepository;this.mapper=mapper;}
    @Override @Transactional public EventResponse create(CreateEventRequest request){
        if(eventRepository.existsByEventCode(request.eventCode())) throw new DuplicateResourceException("Event code already exists: "+request.eventCode());
        Venue venue=venueRepository.findByCode(request.venueCode()).orElseThrow(()->new ResourceNotFoundException("Venue not found: "+request.venueCode()));
        if(!venue.isActive()) throw new BusinessRuleException("Cannot create an event at an inactive venue");
        if(!request.eventDate().isAfter(LocalDateTime.now())) throw new BusinessRuleException("Event date must be in the future");
        if(request.minimumAge()<0) throw new BusinessRuleException("Minimum age cannot be negative");
        Event event=new Event(); event.setEventCode(request.eventCode().trim()); event.setName(request.name().trim()); event.setDescription(request.description()); event.setCategory(request.category()); event.setStatus(EventStatus.DRAFT); event.setEventDate(request.eventDate()); event.setMinimumAge(request.minimumAge()); event.setVenue(venue);
        return mapper.toResponse(eventRepository.save(event));
    }
    @Override @Transactional(readOnly=true) public EventResponse findByCode(String code){return mapper.toResponse(getEvent(code));}
    @Override @Transactional(readOnly=true) public List<EventSummaryResponse> findPublishedEvents(){return mapper.toSummaries(eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED));}
    @Override @Transactional public EventResponse publish(String code){
        Event event=getEvent(code);
        if(event.getStatus()!=EventStatus.DRAFT) throw new BusinessRuleException("Only DRAFT events can be published");
        if(!event.getEventDate().isAfter(LocalDateTime.now())) throw new BusinessRuleException("Only future events can be published");
        if(!event.getVenue().isActive()) throw new BusinessRuleException("Cannot publish an event at an inactive venue");
        event.setStatus(EventStatus.PUBLISHED); return mapper.toResponse(eventRepository.save(event));
    }
    @Override @Transactional public EventResponse addArtist(String code, Long artistId){
        Event event=getEvent(code); Artist artist=artistRepository.findById(artistId).orElseThrow(()->new ResourceNotFoundException("Artist not found: "+artistId));
        if(event.getStatus()==EventStatus.CANCELLED || event.getStatus()==EventStatus.FINISHED) throw new BusinessRuleException("Cannot add artists to a cancelled or finished event");
        if(event.getArtists().stream().anyMatch(existing->existing.getId().equals(artist.getId()))) throw new BusinessRuleException("Artist is already associated with this event");
        event.getArtists().add(artist); return mapper.toResponse(eventRepository.save(event));
    }
    @Override @Transactional(readOnly=true) public List<EventSummaryResponse> findByArtist(String stageName){
        artistRepository.findByStageNameIgnoreCase(stageName).orElseThrow(()->new ResourceNotFoundException("Artist not found: "+stageName));
        return mapper.toSummaries(eventRepository.findByArtistStageName(stageName));
    }
    private Event getEvent(String code){return eventRepository.findByEventCode(code).orElseThrow(()->new ResourceNotFoundException("Event not found: "+code));}
}
