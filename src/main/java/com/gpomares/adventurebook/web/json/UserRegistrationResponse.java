package com.gpomares.adventurebook.web.json;

import java.time.Instant;

public record UserRegistrationResponse(Long id, String email, Instant createdAt) {
}
