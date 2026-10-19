package com.pulsepass;

import com.pulsepass.domain.*;
import com.pulsepass.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class PulsePassPersistenceTest {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine").withDatabaseName("pulsepass").withUsername("pulsepass").withPassword("pulsepass");
    @DynamicPropertySource static void databaseProperties(DynamicPropertyRegistry registry){registry.add("spring.datasource.url",POSTGRES::getJdbcUrl);registry.add("spring.datasource.username",POSTGRES::getUsername);registry.add("spring.datasource.password",POSTGRES::getPassword);}
    @Autowired VenueRepository venues; @Autowired ArtistRepository artists; @Autowired EventRepository events; @Autowired UserRepository users; @Autowired UserProfileRepository profiles; @Autowired TicketRepository tickets;

    @Test void flywayCreatesReferenceVenueAndArtistCatalog(){
        assertTrue(venues.findByCode("VEN-SMR-01").isPresent());
        assertTrue(artists.findByStageNameIgnoreCase("Solar Beat").isPresent());
    }
    @Test @Transactional void eventVenueAndArtistRelationshipsPersistAndQueryWorks(){
        Venue venue=venues.findByCode("VEN-SMR-01").orElseThrow(); Artist artist=artists.findByStageNameIgnoreCase("Solar Beat").orElseThrow();
        Event event=new Event();event.setEventCode("TEST-EVENT-01");event.setName("Test Event");event.setCategory(EventCategory.MUSIC);event.setStatus(EventStatus.PUBLISHED);event.setEventDate(LocalDateTime.now().plusDays(30));event.setMinimumAge(0);event.setVenue(venue);event.getArtists().add(artist);
        events.saveAndFlush(event);
        assertEquals(1,events.findByArtistStageName("Solar Beat").stream().filter(e->e.getEventCode().equals("TEST-EVENT-01")).count());
        assertEquals(1,events.findByVenueCodeOrderByEventDateAsc("VEN-SMR-01").stream().filter(e->e.getEventCode().equals("TEST-EVENT-01")).count());
        assertEquals(1,events.findByCityAndArtist("Santa Marta","Solar Beat").stream().filter(e->e.getEventCode().equals("TEST-EVENT-01")).count());
        assertEquals(1,events.findRecommended(LocalDateTime.now(),"Santa Marta","solar").stream().filter(e->e.getEventCode().equals("TEST-EVENT-01")).count());
    }
    @Test @Transactional void userProfileAndTicketRelationshipsAndPaidCountWork(){
        Venue venue=venues.findByCode("VEN-SMR-01").orElseThrow();
        User user=new User(); user.setUsername("persistence-user"); user.setEmail("persistence-user@example.com"); user.setActive(true);
        UserProfile profile=new UserProfile(); profile.setFirstName("Test"); profile.setLastName("User"); profile.setBirthDate(java.time.LocalDate.of(1995,1,1)); user.setProfile(profile);
        users.saveAndFlush(user);
        assertNotNull(user.getProfile().getId());

        Event event=new Event(); event.setEventCode("PERSIST-TICKET-EVENT"); event.setName("Persistence ticket event"); event.setCategory(EventCategory.MUSIC); event.setStatus(EventStatus.PUBLISHED); event.setEventDate(LocalDateTime.now().plusDays(45)); event.setMinimumAge(0); event.setVenue(venue); events.saveAndFlush(event);
        Ticket paid=new Ticket(); paid.setTicketCode("PERSIST-PAID-1"); paid.setType(TicketType.GENERAL); paid.setPrice(new java.math.BigDecimal("120000.00")); paid.setStatus(TicketStatus.PAID); paid.setPurchaseDate(LocalDateTime.now()); paid.setUser(user); paid.setEvent(event); tickets.saveAndFlush(paid);
        Ticket reserved=new Ticket(); reserved.setTicketCode("PERSIST-RES-1"); reserved.setType(TicketType.GENERAL); reserved.setPrice(new java.math.BigDecimal("120000.00")); reserved.setStatus(TicketStatus.RESERVED); reserved.setPurchaseDate(LocalDateTime.now()); reserved.setUser(user); reserved.setEvent(event); tickets.saveAndFlush(reserved);

        assertEquals(1, tickets.countByEventEventCodeAndStatus("PERSIST-TICKET-EVENT", TicketStatus.PAID));
        assertEquals(2, tickets.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc("persistence-user@example.com").size());
        assertEquals(1, tickets.findByEventEventCodeAndStatusOrderByPurchaseDateAsc("PERSIST-TICKET-EVENT", TicketStatus.PAID).size());
    }

    @Test @Transactional void userProfileIsOneToOne(){
        User user=new User();user.setUsername("profile-owner");user.setEmail("profile-owner@example.com");user.setActive(true);
        UserProfile first=new UserProfile();first.setFirstName("Profile");first.setLastName("Owner");first.setBirthDate(java.time.LocalDate.of(1990,1,1));user.setProfile(first);users.saveAndFlush(user);
        UserProfile duplicate=new UserProfile();duplicate.setFirstName("Second");duplicate.setLastName("Profile");duplicate.setBirthDate(java.time.LocalDate.of(1991,1,1));duplicate.setUser(user);
        assertThrows(DataIntegrityViolationException.class,()->profiles.saveAndFlush(duplicate));
    }

    @Test void uniqueVenueCodeIsEnforcedByPostgres(){
        Venue duplicate=new Venue();duplicate.setCode("VEN-SMR-01");duplicate.setName("Duplicate");duplicate.setCity("Santa Marta");duplicate.setAddress("Address");duplicate.setCapacity(10);duplicate.setActive(true);
        assertThrows(DataIntegrityViolationException.class,()->venues.saveAndFlush(duplicate));
    }
}
