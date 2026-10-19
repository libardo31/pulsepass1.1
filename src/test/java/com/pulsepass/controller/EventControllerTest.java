package com.pulsepass.controller;

import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class) class EventControllerTest {
    @Autowired MockMvc mvc; @MockitoBean EventService eventService;
    private EventResponse response(){return new EventResponse(1L,"CMF-2026","Caribbean Music Fest 2026","Festival",EventCategory.MUSIC,EventStatus.DRAFT,LocalDateTime.now().plusDays(30),18,null,"VEN-SMR-01","Marina Convention Center",List.of());}
    @Test void validCreateReturns201() throws Exception {
        when(eventService.create(any(CreateEventRequest.class))).thenReturn(response());
        mvc.perform(post("/api/events").contentType("application/json").content("""
          {"eventCode":"CMF-2026","name":"Caribbean Music Fest 2026","description":"Festival","category":"MUSIC","eventDate":"2030-12-01T18:00:00","minimumAge":18,"venueCode":"VEN-SMR-01"}
          """)).andExpect(status().isCreated()).andExpect(jsonPath("$.eventCode").value("CMF-2026"));
        verify(eventService).create(any(CreateEventRequest.class));
    }
    @Test void invalidCreateReturns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/events").contentType("application/json").content("""
          {"eventCode":"","name":"","category":null,"eventDate":null,"minimumAge":-1,"venueCode":""}
          """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.details").exists());
        verifyNoInteractions(eventService);
    }
    @Test void publishBusinessConflictReturns409() throws Exception {
        when(eventService.publish("CMF-2026")).thenThrow(new BusinessRuleException("Only DRAFT events can be published"));
        mvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }
    @Test void publishedListReturns200() throws Exception {
        when(eventService.findPublishedEvents()).thenReturn(List.<EventSummaryResponse>of());
        mvc.perform(get("/api/events/published")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void findExistingEventReturns200() throws Exception {when(eventService.findByCode("CMF-2026")).thenReturn(response()); mvc.perform(get("/api/events/CMF-2026")).andExpect(status().isOk()).andExpect(jsonPath("$.eventCode").value("CMF-2026"));}
    @Test void publishDraftReturns200() throws Exception {EventResponse published=new EventResponse(1L,"CMF-2026","Caribbean Music Fest 2026","Festival",EventCategory.MUSIC,EventStatus.PUBLISHED,LocalDateTime.now().plusDays(30),18,null,"VEN-SMR-01","Marina Convention Center",List.of()); when(eventService.publish("CMF-2026")).thenReturn(published); mvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));}
    @Test void addArtistReturns200() throws Exception {when(eventService.addArtist("CMF-2026",1L)).thenReturn(response()); mvc.perform(post("/api/events/CMF-2026/artists/1")).andExpect(status().isOk());}
    @Test void eventsByArtistReturns200() throws Exception {when(eventService.findByArtist("Solar Beat")).thenReturn(List.of()); mvc.perform(get("/api/events/by-artist").param("stageName","Solar Beat")).andExpect(status().isOk()).andExpect(content().json("[]"));}

}
