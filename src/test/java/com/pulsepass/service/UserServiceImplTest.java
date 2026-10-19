package com.pulsepass.service;

import com.pulsepass.domain.User;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.mapper.PulsePassMapper;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private UserRepository users; private PulsePassMapper mapper; private UserServiceImpl service;
    @BeforeEach void setUp(){users=mock(UserRepository.class);mapper=mock(PulsePassMapper.class);service=new UserServiceImpl(users,mapper);}
    private RegisterUserRequest request(LocalDate birthDate){return new RegisterUserRequest("andrea","andrea@example.com","Andrea","Gomez",null,"Santa Marta",birthDate);}
    @Test void validRegistrationSavesUserAndProfile(){
        when(users.existsByUsername("andrea")).thenReturn(false); when(users.existsByEmailIgnoreCase("andrea@example.com")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(call->call.getArgument(0));
        when(mapper.toResponse(any(User.class))).thenReturn(new UserResponse(1L,"andrea","andrea@example.com",true,"Andrea","Gomez",null,"Santa Marta",LocalDate.of(1998,1,1)));
        UserResponse result=service.register(request(LocalDate.of(1998,1,1)));
        assertTrue(result.active()); verify(users).save(argThat(user->user.isActive() && user.getProfile()!=null && user.getProfile().getBirthDate().equals(LocalDate.of(1998,1,1))));
    }
    @Test void duplicateUsernameIsRejected(){when(users.existsByUsername("andrea")).thenReturn(true); assertThrows(DuplicateResourceException.class,()->service.register(request(LocalDate.of(1998,1,1)))); verify(users,never()).save(any());}
    @Test void duplicateEmailIsRejectedIgnoringCase(){when(users.existsByUsername("andrea")).thenReturn(false);when(users.existsByEmailIgnoreCase("andrea@example.com")).thenReturn(true);assertThrows(DuplicateResourceException.class,()->service.register(request(LocalDate.of(1998,1,1))));verify(users,never()).save(any());}
    @Test void futureBirthDateIsRejected(){when(users.existsByUsername("andrea")).thenReturn(false);when(users.existsByEmailIgnoreCase("andrea@example.com")).thenReturn(false);assertThrows(BusinessRuleException.class,()->service.register(request(LocalDate.now().plusDays(1))));verify(users,never()).save(any());}
}
