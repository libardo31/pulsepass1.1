package com.pulsepass.controller;
import com.pulsepass.domain.*;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(TicketController.class) class TicketControllerTest {
    @Autowired MockMvc mvc; @MockitoBean TicketService ticketService;
    private TicketResponse ticket(){return new TicketResponse(1L,"TCK-12345678",TicketType.GENERAL,new BigDecimal("120000.00"),TicketStatus.PAID,LocalDateTime.now(),"andrea@example.com","CMF-2026","Caribbean Music Fest 2026");}
    @Test void validPurchaseReturns201() throws Exception {when(ticketService.purchase(any(PurchaseTicketRequest.class))).thenReturn(ticket()); mvc.perform(post("/api/tickets").contentType("application/json").content("""
      {"userEmail":"andrea@example.com","eventCode":"CMF-2026","type":"GENERAL"}
      """)).andExpect(status().isCreated()).andExpect(jsonPath("$.ticketCode").value("TCK-12345678"));}
    @Test void invalidPurchaseReturns400WithoutCallingService() throws Exception {mvc.perform(post("/api/tickets").contentType("application/json").content("{}")).andExpect(status().isBadRequest()); verifyNoInteractions(ticketService);}
    @Test void missingUserReturns404() throws Exception {when(ticketService.purchase(any(PurchaseTicketRequest.class))).thenThrow(new ResourceNotFoundException("User not found")); mvc.perform(post("/api/tickets").contentType("application/json").content("""
      {"userEmail":"andrea@example.com","eventCode":"CMF-2026","type":"GENERAL"}
      """)).andExpect(status().isNotFound());}
    @Test void invalidCancellationReturns409() throws Exception {when(ticketService.cancel("TCK-1")).thenThrow(new BusinessRuleException("Only PAID tickets can be cancelled")); mvc.perform(patch("/api/tickets/TCK-1/cancel")).andExpect(status().isConflict());}
    @Test void ticketByCodeReturns200() throws Exception {when(ticketService.findByCode("TCK-12345678")).thenReturn(ticket()); mvc.perform(get("/api/tickets/TCK-12345678")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"));}
    @Test void paidTicketsForEventReturns200() throws Exception {when(ticketService.findPaidTicketsByEvent("CMF-2026")).thenReturn(List.of(ticket())); mvc.perform(get("/api/events/CMF-2026/tickets/paid")).andExpect(status().isOk()).andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));}

    @Test void ticketsByUserReturns200() throws Exception {when(ticketService.findByUserEmail("andrea@example.com")).thenReturn(List.of(ticket())); mvc.perform(get("/api/tickets/by-user").param("email","andrea@example.com")).andExpect(status().isOk()).andExpect(jsonPath("$[0].userEmail").value("andrea@example.com"));}
    @Test void validCancellationReturns200() throws Exception {TicketResponse cancelled=new TicketResponse(1L,"TCK-12345678",TicketType.GENERAL,new BigDecimal("120000.00"),TicketStatus.CANCELLED,LocalDateTime.now(),"andrea@example.com","CMF-2026","Caribbean Music Fest 2026"); when(ticketService.cancel("TCK-12345678")).thenReturn(cancelled); mvc.perform(patch("/api/tickets/TCK-12345678/cancel")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));}
    @Test void validUseReturns200() throws Exception {TicketResponse used=new TicketResponse(1L,"TCK-12345678",TicketType.GENERAL,new BigDecimal("120000.00"),TicketStatus.USED,LocalDateTime.now(),"andrea@example.com","CMF-2026","Caribbean Music Fest 2026"); when(ticketService.markAsUsed("TCK-12345678")).thenReturn(used); mvc.perform(patch("/api/tickets/TCK-12345678/use")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("USED"));}
    @Test void invalidUseReturns409() throws Exception {when(ticketService.markAsUsed("TCK-CANCELLED")).thenThrow(new BusinessRuleException("Only PAID tickets can be marked as used")); mvc.perform(patch("/api/tickets/TCK-CANCELLED/use")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));}

}
