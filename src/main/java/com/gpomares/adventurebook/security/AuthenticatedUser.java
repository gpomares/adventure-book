package com.gpomares.adventurebook.security;

public record AuthenticatedUser(Long userId, String email) {
}
