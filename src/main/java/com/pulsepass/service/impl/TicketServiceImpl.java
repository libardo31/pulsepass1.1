package com.pulsepass.service.impl;

import com.pulsepass.domain.*;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.PulsePassMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository; private final UserRepository userRepository; private final EventRepository eventRepository; private final PulsePassMapper mapper;
    public TicketServiceImpl(TicketRepository ticketRepository, UserRepository userRepository, EventRepository eventRepository, PulsePassMapper mapper){this.ticketRepository=ticketRepository;this.userRepository=userRepository;this.eventRepository=eventRepository;this.mapper=mapper;}
    @Override @Transactional public TicketResponse purchase(PurchaseTicketRequest request){
        User user=userRepository.findByEmailIgnoreCase(request.userEmail()).orElseThrow(()->new ResourceNotFoundException("User not found with email: "+request.userEmail()));
        if(!user.isActive()) throw new BusinessRuleException("Inactive users cannot purchase tickets");
        Event event=eventRepository.findByEventCode(request.eventCode()).orElseThrow(()->new ResourceNotFoundException("Event not found: "+request.eventCode()));
        if(event.getStatus()!=EventStatus.PUBLISHED) throw new BusinessRuleException("Tickets can only be purchased for PUBLISHED events");
        if(!event.getEventDate().isAfter(LocalDateTime.now())) throw new BusinessRuleException("Cannot purchase a ticket for an event that has already started");
        if(event.getMinimumAge()>0){
            LocalDate birthDate=user.getProfile().getBirthDate();
            int ageAtEvent=Period.between(birthDate,event.getEventDate().toLocalDate()).getYears();
            if(ageAtEvent<event.getMinimumAge()) throw new BusinessRuleException("User does not meet the minimum age for this event");
        }
        long paid=ticketRepository.countByEventEventCodeAndStatus(event.getEventCode(), TicketStatus.PAID);
        if(paid>=event.getVenue().getCapacity()) throw new BusinessRuleException("Event capacity has been reached");
        Ticket ticket=new Ticket(); ticket.setTicketCode("TCK-"+UUID.randomUUID().toString().substring(0,8).toUpperCase()); ticket.setType(request.type()); ticket.setPrice(priceFor(request.type())); ticket.setStatus(TicketStatus.PAID); ticket.setPurchaseDate(LocalDateTime.now()); ticket.setUser(user); ticket.setEvent(event);
        Ticket saved=ticketRepository.save(ticket);
        if(paid+1>=event.getVenue().getCapacity()) event.setStatus(EventStatus.SOLD_OUT);
        return mapper.toResponse(saved);
    }
    @Override @Transactional(readOnly=true) public TicketResponse findByCode(String code){return mapper.toResponse(getTicket(code));}
    @Override @Transactional(readOnly=true) public List<TicketResponse> findByUserEmail(String email){
        if(!userRepository.findByEmailIgnoreCase(email).isPresent()) throw new ResourceNotFoundException("User not found with email: "+email);
        return ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(email).stream().map(mapper::toResponse).toList();
    }
    @Override @Transactional(readOnly=true) public List<TicketResponse> findPaidTicketsByEvent(String eventCode){
        if(!eventRepository.findByEventCode(eventCode).isPresent()) throw new ResourceNotFoundException("Event not found: "+eventCode);
        return ticketRepository.findByEventEventCodeAndStatusOrderByPurchaseDateAsc(eventCode, TicketStatus.PAID).stream().map(mapper::toResponse).toList();
    }
    @Override @Transactional public TicketResponse cancel(String code){
        Ticket ticket=getTicket(code);
        if(ticket.getStatus()!=TicketStatus.PAID) throw new BusinessRuleException("Only PAID tickets can be cancelled");
        if(!ticket.getEvent().getEventDate().isAfter(LocalDateTime.now())) throw new BusinessRuleException("A ticket cannot be cancelled after the event date");
        ticket.setStatus(TicketStatus.CANCELLED); return mapper.toResponse(ticketRepository.save(ticket));
    }
    @Override @Transactional public TicketResponse markAsUsed(String code){
        Ticket ticket=getTicket(code);
        if(ticket.getStatus()!=TicketStatus.PAID) throw new BusinessRuleException("Only PAID tickets can be marked as used");
        ticket.setStatus(TicketStatus.USED); return mapper.toResponse(ticketRepository.save(ticket));
    }
    private Ticket getTicket(String code){return ticketRepository.findByTicketCode(code).orElseThrow(()->new ResourceNotFoundException("Ticket not found: "+code));}
    private BigDecimal priceFor(TicketType type){return switch(type){case GENERAL -> new BigDecimal("120000.00"); case STUDENT -> new BigDecimal("90000.00"); case VIP -> new BigDecimal("250000.00"); case BACKSTAGE -> new BigDecimal("400000.00");};}
}
