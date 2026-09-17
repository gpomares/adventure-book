package com.gpomares.adventurebook.security;

import java.util.Optional;

public interface AuthenticatedUserProvider {

    Optional<AuthenticatedUser> currentUser();

    default Long requireUserId() {
        return currentUser()
                .map(AuthenticatedUser::userId)
                .orElseThrow(() -> new IllegalStateException("No authenticated user is available"));
    }
}
