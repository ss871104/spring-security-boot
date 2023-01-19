package com.ss871104.springsecurityboot.authentication.service;

import com.ss871104.springsecurityboot.authentication.dto.AuthenticationResponse;
import com.ss871104.springsecurityboot.authentication.dto.LoginRequest;
import com.ss871104.springsecurityboot.authentication.dto.RegisterRequest;

public interface AuthenticationService {
    public AuthenticationResponse register(RegisterRequest registerRequest);
    public AuthenticationResponse login(LoginRequest loginRequest);
}
