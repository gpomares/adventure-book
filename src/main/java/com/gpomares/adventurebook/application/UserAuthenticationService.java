package com.gpomares.adventurebook.application;

import com.gpomares.adventurebook.dto.AccessTokenDto;

public interface UserAuthenticationService {

    AccessTokenDto login(String email, String password);
}
