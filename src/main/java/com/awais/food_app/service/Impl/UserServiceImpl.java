package com.awais.food_app.service.Impl;

import com.awais.food_app.entity.UserEntity;
import com.awais.food_app.io.UserRequest;
import com.awais.food_app.io.UserResponse;
import com.awais.food_app.repository.UserRepository;
import com.awais.food_app.service.AuthenticationFacade;
import com.awais.food_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;
    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        UserEntity newUser = convertToEntity(userRequest);
        UserEntity savedUser = userRepository.save(newUser);
        return convertToResponse(savedUser);

    }

    @Override
    public String findByUserId(String email) {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity logginUser = userRepository.findByEmail(loggedInUserEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return logginUser.getId();
    }

    private UserEntity convertToEntity(UserRequest userRequest) {
        return UserEntity.builder()
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .name(userRequest.getName())
                .build();


    }
    private UserResponse convertToResponse(UserEntity userEntity) {
        return UserResponse.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .build();

    }
}
