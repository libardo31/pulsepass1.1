package com.pulsepass.controller;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(UserController.class) class UserControllerTest {
    @Autowired MockMvc mvc; @MockitoBean UserService userService;
    @Test void validRegistrationReturns201() throws Exception {
        when(userService.register(any(RegisterUserRequest.class))).thenReturn(new UserResponse(1L,"andrea","andrea@example.com",true,"Andrea","Lopez",null,"Santa Marta",LocalDate.of(1998,1,1)));
        mvc.perform(post("/api/users").contentType("application/json").content("""
          {"username":"andrea","email":"andrea@example.com","firstName":"Andrea","lastName":"Lopez","birthDate":"1998-01-01"}
          """)).andExpect(status().isCreated()).andExpect(jsonPath("$.active").value(true));
    }
    @Test void invalidEmailReturns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/users").contentType("application/json").content("""
          {"username":"andrea","email":"not-an-email","firstName":"Andrea","lastName":"Lopez","birthDate":"1998-01-01"}
          """)).andExpect(status().isBadRequest()); verifyNoInteractions(userService);
    }
    @Test void duplicateUsernameReturns409() throws Exception {when(userService.register(any(RegisterUserRequest.class))).thenThrow(new DuplicateResourceException("Username already exists")); mvc.perform(post("/api/users").contentType("application/json").content("""
          {"username":"andrea","email":"andrea@example.com","firstName":"Andrea","lastName":"Lopez","birthDate":"1998-01-01"}
          """)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));}

    @Test void findByEmailReturns200() throws Exception {when(userService.findByEmail("andrea@example.com")).thenReturn(new UserResponse(1L,"andrea","andrea@example.com",true,"Andrea","Lopez",null,"Santa Marta",LocalDate.of(1998,1,1))); mvc.perform(get("/api/users/by-email").param("email","andrea@example.com")).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("andrea@example.com"));}
    @Test void findByUsernameReturns200() throws Exception {when(userService.findByUsername("andrea")).thenReturn(new UserResponse(1L,"andrea","andrea@example.com",true,"Andrea","Lopez",null,"Santa Marta",LocalDate.of(1998,1,1))); mvc.perform(get("/api/users/by-username").param("username","andrea")).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("andrea"));}

}
