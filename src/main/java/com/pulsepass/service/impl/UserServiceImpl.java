package com.pulsepass.service.impl;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.PulsePassMapper;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository; private final PulsePassMapper mapper;
    public UserServiceImpl(UserRepository userRepository, PulsePassMapper mapper){this.userRepository=userRepository;this.mapper=mapper;}
    @Override @Transactional public UserResponse register(RegisterUserRequest request){
        if(userRepository.existsByUsername(request.username())) throw new DuplicateResourceException("Username already exists");
        if(userRepository.existsByEmailIgnoreCase(request.email())) throw new DuplicateResourceException("Email already exists");
        if(request.birthDate().isAfter(LocalDate.now())) throw new BusinessRuleException("Birth date cannot be in the future");
        User user=new User(); user.setUsername(request.username().trim()); user.setEmail(request.email().trim().toLowerCase()); user.setActive(true);
        UserProfile profile=new UserProfile(); profile.setFirstName(request.firstName().trim()); profile.setLastName(request.lastName().trim()); profile.setPhone(request.phone()); profile.setCity(request.city()); profile.setBirthDate(request.birthDate()); user.setProfile(profile);
        return mapper.toResponse(userRepository.save(user));
    }
    @Override @Transactional(readOnly=true) public UserResponse findByEmail(String email){return userRepository.findByEmailIgnoreCase(email).map(mapper::toResponse).orElseThrow(()->new ResourceNotFoundException("User not found with email: "+email));}
    @Override @Transactional(readOnly=true) public UserResponse findByUsername(String username){return userRepository.findByUsername(username).map(mapper::toResponse).orElseThrow(()->new ResourceNotFoundException("User not found with username: "+username));}
}
