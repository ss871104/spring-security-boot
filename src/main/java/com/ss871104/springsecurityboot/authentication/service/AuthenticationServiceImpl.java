package com.ss871104.springsecurityboot.authentication.service;

import com.ss871104.springsecurityboot.authentication.dto.AuthenticationResponse;
import com.ss871104.springsecurityboot.security.jwt.JwtTokenService;
import com.ss871104.springsecurityboot.user.domain.Role;
import com.ss871104.springsecurityboot.user.domain.User;
import com.ss871104.springsecurityboot.authentication.dto.LoginRequest;
import com.ss871104.springsecurityboot.authentication.dto.RegisterRequest;
import com.ss871104.springsecurityboot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    @Override
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .name(registerRequest.getName())
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .build();

        repository.save(user);

        String jwtToken = jwtTokenService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public AuthenticationResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = repository.findByUsernameOrEmail(loginRequest.getUsername())
                .orElseThrow();

        String jwtToken = jwtTokenService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
