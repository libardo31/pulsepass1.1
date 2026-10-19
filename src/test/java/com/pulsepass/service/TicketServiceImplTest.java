package com.pulsepass.service;

import com.pulsepass.domain.*;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.PulsePassMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {
    private TicketRepository tickets; private UserRepository users; private EventRepository events; private PulsePassMapper mapper; private TicketServiceImpl service;
    @BeforeEach void setUp(){tickets=mock(TicketRepository.class);users=mock(UserRepository.class);events=mock(EventRepository.class);mapper=mock(PulsePassMapper.class);service=new TicketServiceImpl(tickets,users,events,mapper);}
    private User activeAdult(){User user=new User();user.setEmail("andrea@example.com");user.setActive(true);UserProfile profile=new UserProfile();profile.setBirthDate(LocalDate.now().minusYears(25));user.setProfile(profile);return user;}
    private Event event(EventStatus status,int capacity,int minAge){Venue venue=new Venue();venue.setCapacity(capacity);venue.setActive(true);Event event=new Event();event.setEventCode("CMF-2026");event.setName("Festival");event.setStatus(status);event.setEventDate(LocalDateTime.now().plusDays(20));event.setMinimumAge(minAge);event.setVenue(venue);return event;}
    private TicketResponse response(TicketStatus status){return new TicketResponse(1L,"TCK-12345678",TicketType.GENERAL,new BigDecimal("120000.00"),status,LocalDateTime.now(),"andrea@example.com","CMF-2026","Festival");}
    @Test void userMustExist(){when(users.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());assertThrows(ResourceNotFoundException.class,()->service.purchase(new PurchaseTicketRequest("missing@example.com","CMF-2026",TicketType.GENERAL)));verifyNoInteractions(events);}
    @Test void inactiveUserCannotPurchase(){User user=new User();user.setEmail("miguel@example.com");user.setActive(false);when(users.findByEmailIgnoreCase("miguel@example.com")).thenReturn(Optional.of(user));assertThrows(BusinessRuleException.class,()->service.purchase(new PurchaseTicketRequest("miguel@example.com","CMF-2026",TicketType.GENERAL)));verifyNoInteractions(events);verify(tickets,never()).save(any());}
    @Test void eventMustBePublished(){when(users.findByEmailIgnoreCase("andrea@example.com")).thenReturn(Optional.of(activeAdult()));when(events.findByEventCode("CMF-2026")).thenReturn(Optional.of(event(EventStatus.DRAFT,3,0)));assertThrows(BusinessRuleException.class,()->service.purchase(new PurchaseTicketRequest("andrea@example.com","CMF-2026",TicketType.GENERAL)));verify(tickets,never()).save(any());}
    @Test void cancelledEventCannotBePurchased(){when(users.findByEmailIgnoreCase("andrea@example.com")).thenReturn(Optional.of(activeAdult()));when(events.findByEventCode("CMF-2026")).thenReturn(Optional.of(event(EventStatus.CANCELLED,3,0)));assertThrows(BusinessRuleException.class,()->service.purchase(new PurchaseTicketRequest("andrea@example.com","CMF-2026",TicketType.GENERAL)));}
    @Test void underageUserCannotPurchase(){User user=activeAdult();user.getProfile().setBirthDate(LocalDate.now().minusYears(17));when(users.findByEmailIgnoreCase("andrea@example.com")).thenReturn(Optional.of(user));when(events.findByEventCode("CMF-2026")).thenReturn(Optional.of(event(EventStatus.PUBLISHED,3,18)));assertThrows(BusinessRuleException.class,()->service.purchase(new PurchaseTicketRequest("andrea@example.com","CMF-2026",TicketType.GENERAL)));verify(tickets,never()).save(any());}
    @Test void fullEventCannotBePurchased(){when(users.findByEmailIgnoreCase("andrea@example.com")).thenReturn(Optional.of(activeAdult()));when(events.findByEventCode("CMF-2026")).thenReturn(Optional.of(event(EventStatus.PUBLISHED,3,0)));when(tickets.countByEventEventCodeAndStatus("CMF-2026",TicketStatus.PAID)).thenReturn(3L);assertThrows(BusinessRuleException.class,()->service.purchase(new PurchaseTicketRequest("andrea@example.com","CMF-2026",TicketType.GENERAL)));verify(tickets,never()).save(any());}
    @Test void lastTicketMarksEventSoldOut(){
        User user=activeAdult();Event event=event(EventStatus.PUBLISHED,3,0);when(users.findByEmailIgnoreCase("andrea@example.com")).thenReturn(Optional.of(user));when(events.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));when(tickets.countByEventEventCodeAndStatus("CMF-2026",TicketStatus.PAID)).thenReturn(2L);when(tickets.save(any(Ticket.class))).thenAnswer(call->call.getArgument(0));when(mapper.toResponse(any(Ticket.class))).thenReturn(response(TicketStatus.PAID));
        TicketResponse result=service.purchase(new PurchaseTicketRequest("andrea@example.com","CMF-2026",TicketType.GENERAL));
        assertEquals(TicketStatus.PAID,result.status());assertEquals(EventStatus.SOLD_OUT,event.getStatus());verify(tickets).save(argThat(ticket->ticket.getStatus()==TicketStatus.PAID && ticket.getPrice().compareTo(new BigDecimal("120000.00"))==0));
    }
    @Test void paidTicketCanBeCancelled(){Ticket ticket=new Ticket();ticket.setStatus(TicketStatus.PAID);ticket.setTicketCode("TCK-1");Event event=event(EventStatus.PUBLISHED,3,0);ticket.setEvent(event);when(tickets.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));when(tickets.save(ticket)).thenReturn(ticket);when(mapper.toResponse(ticket)).thenReturn(response(TicketStatus.CANCELLED));assertEquals(TicketStatus.CANCELLED,service.cancel("TCK-1").status());}
    @Test void usedTicketCannotBeCancelled(){Ticket ticket=new Ticket();ticket.setStatus(TicketStatus.USED);ticket.setEvent(event(EventStatus.PUBLISHED,3,0));when(tickets.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));assertThrows(BusinessRuleException.class,()->service.cancel("TCK-1"));verify(tickets,never()).save(any());}
    @Test void paidTicketCanBeMarkedUsed(){Ticket ticket=new Ticket();ticket.setStatus(TicketStatus.PAID);ticket.setTicketCode("TCK-1");when(tickets.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));when(tickets.save(ticket)).thenReturn(ticket);when(mapper.toResponse(ticket)).thenReturn(response(TicketStatus.USED));assertEquals(TicketStatus.USED,service.markAsUsed("TCK-1").status());}
    @Test void cancelledTicketCannotBeUsed(){Ticket ticket=new Ticket();ticket.setStatus(TicketStatus.CANCELLED);when(tickets.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));assertThrows(BusinessRuleException.class,()->service.markAsUsed("TCK-1"));verify(tickets,never()).save(any());}
}
