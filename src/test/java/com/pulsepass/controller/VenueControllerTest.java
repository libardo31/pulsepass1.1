package com.pulsepass.controller;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
class VenueControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean VenueService venueService;
    @Test void existingVenueReturns200() throws Exception {
        when(venueService.findByCode("VEN-SMR-01")).thenReturn(new VenueResponse(1L,"VEN-SMR-01","Marina Convention Center","Santa Marta","Avenida del Puerto",3,true));
        mvc.perform(get("/api/venues/VEN-SMR-01")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.code").value("VEN-SMR-01"));
        verify(venueService).findByCode("VEN-SMR-01");
    }
    @Test void missingVenueReturns404WithStandardError() throws Exception {
        when(venueService.findByCode("missing")).thenThrow(new ResourceNotFoundException("Venue not found: missing"));
        mvc.perform(get("/api/venues/missing")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.message").value("Venue not found: missing"));
    }
    @Test void activeVenuesReturns200() throws Exception {
        when(venueService.findActiveVenues()).thenReturn(List.of());
        mvc.perform(get("/api/venues/active")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
}
