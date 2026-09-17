package com.gpomares.adventurebook.web.controller;

import com.gpomares.adventurebook.application.UserRegistrationService;
import com.gpomares.adventurebook.dto.UserDto;
import com.gpomares.adventurebook.exception.InvalidUserRegistrationException;
import com.gpomares.adventurebook.web.json.UserRegistrationRequest;
import com.gpomares.adventurebook.web.json.UserRegistrationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class UserRegistrationController {

    private final UserRegistrationService registrationService;

    public UserRegistrationController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/users")
    public ResponseEntity<UserRegistrationResponse> register(@RequestBody(required = true) UserRegistrationRequest request) {
        if (request == null) {
            throw new InvalidUserRegistrationException("Request body is required");
        }
        UserDto user = registrationService.register(request.email(), request.password());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().pathSegment(user.id().toString()).build().toUri();
        return ResponseEntity.created(location)
                .body(new UserRegistrationResponse(user.id(), user.email(), user.createdAt()));
    }
}
