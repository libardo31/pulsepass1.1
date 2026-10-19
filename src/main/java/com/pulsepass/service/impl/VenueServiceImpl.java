package com.pulsepass.service.impl;
import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.PulsePassMapper;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.VenueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class VenueServiceImpl implements VenueService {
    private final VenueRepository venueRepository;
    private final PulsePassMapper mapper;
    public VenueServiceImpl(VenueRepository venueRepository, PulsePassMapper mapper){this.venueRepository=venueRepository;this.mapper=mapper;}
    @Override @Transactional(readOnly=true) public VenueResponse findByCode(String code){return venueRepository.findByCode(code).map(mapper::toResponse).orElseThrow(()->new ResourceNotFoundException("Venue not found: "+code));}
    @Override @Transactional(readOnly=true) public List<VenueResponse> findActiveVenues(){return venueRepository.findByActiveTrueOrderByNameAsc().stream().map(mapper::toResponse).toList();}
}
