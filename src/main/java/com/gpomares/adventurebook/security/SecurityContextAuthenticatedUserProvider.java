package com.gpomares.adventurebook.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SecurityContextAuthenticatedUserProvider implements AuthenticatedUserProvider {

    @Override
    public Optional<AuthenticatedUser> currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof AuthenticatedUser user ? Optional.of(user) : Optional.empty();
    }
}
