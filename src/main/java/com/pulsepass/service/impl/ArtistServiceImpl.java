package com.pulsepass.service.impl;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.PulsePassMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.service.ArtistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class ArtistServiceImpl implements ArtistService {
    private final ArtistRepository artistRepository; private final PulsePassMapper mapper;
    public ArtistServiceImpl(ArtistRepository artistRepository, PulsePassMapper mapper){this.artistRepository=artistRepository;this.mapper=mapper;}
    @Override @Transactional(readOnly=true) public ArtistResponse findById(Long id){return artistRepository.findById(id).map(mapper::toResponse).orElseThrow(()->new ResourceNotFoundException("Artist not found: "+id));}
    @Override @Transactional(readOnly=true) public ArtistResponse findByStageName(String name){return artistRepository.findByStageNameIgnoreCase(name).map(mapper::toResponse).orElseThrow(()->new ResourceNotFoundException("Artist not found: "+name));}
    @Override @Transactional(readOnly=true) public List<ArtistResponse> findActiveArtists(){return artistRepository.findByActiveTrueOrderByStageNameAsc().stream().map(mapper::toResponse).toList();}
}
