package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.dto.UserDto;

public interface UserRegistrationService {

    UserDto register(String email, String password);
}
