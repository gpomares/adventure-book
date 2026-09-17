package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.UserAuthenticationService;
import com.gpomares.adventurebook.dto.AccessTokenDto;
import com.gpomares.adventurebook.exception.InvalidCredentialsException;
import com.gpomares.adventurebook.web.json.LoginRequest;
import com.gpomares.adventurebook.web.json.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final UserAuthenticationService authenticationService;

    public AuthenticationController(UserAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody(required = true) LoginRequest request) {
        if (request == null) {
            throw new InvalidCredentialsException();
        }
        AccessTokenDto token = authenticationService.login(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(token.accessToken(), token.tokenType(), token.expiresIn()));
    }
}
