package com.awais.food_app.controller;

import com.awais.food_app.io.AuthenticationRequest;
import com.awais.food_app.io.AuthenticationResponse;
import com.awais.food_app.service.Impl.AppUserDetailsService;
import com.awais.food_app.utils.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody AuthenticationRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String jwtToken = jwtUtils.generateToken(userDetails);
        return new AuthenticationResponse(authRequest.getEmail(), jwtToken);
    }
}
