package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.service.ArtistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping("/active")
    public ResponseEntity<List<ArtistResponse>> active() {
        return ResponseEntity.ok(artistService.findActiveArtists());
    }

    @GetMapping("/by-stage-name")
    public ResponseEntity<ArtistResponse> byStageName(@RequestParam String stageName) {
        return ResponseEntity.ok(artistService.findByStageName(stageName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.findById(id));
    }
}
