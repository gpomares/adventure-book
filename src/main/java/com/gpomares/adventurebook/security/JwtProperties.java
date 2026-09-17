package com.gpomares.adventurebook.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String signingSecret, Duration accessTokenExpiry) {
}
