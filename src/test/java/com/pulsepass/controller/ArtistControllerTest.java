package com.pulsepass.controller;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.ArtistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(ArtistController.class) class ArtistControllerTest {
    @Autowired MockMvc mvc; @MockitoBean ArtistService artistService;
    @Test void findArtistByIdReturns200() throws Exception {when(artistService.findById(1L)).thenReturn(new ArtistResponse(1L,"Solar Beat","Colombia","Electronic",true)); mvc.perform(get("/api/artists/1")).andExpect(status().isOk()).andExpect(jsonPath("$.stageName").value("Solar Beat"));}
    @Test void missingArtistReturns404() throws Exception {when(artistService.findById(99L)).thenThrow(new ResourceNotFoundException("Artist not found: 99")); mvc.perform(get("/api/artists/99")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));}
    @Test void activeArtistsReturns200() throws Exception {when(artistService.findActiveArtists()).thenReturn(List.of()); mvc.perform(get("/api/artists/active")).andExpect(status().isOk()).andExpect(content().json("[]"));}

    @Test void findByStageNameReturns200() throws Exception {when(artistService.findByStageName("Solar Beat")).thenReturn(new ArtistResponse(1L,"Solar Beat","Colombia","Electronic",true)); mvc.perform(get("/api/artists/by-stage-name").param("stageName","Solar Beat")).andExpect(status().isOk()).andExpect(jsonPath("$.stageName").value("Solar Beat"));}

}
