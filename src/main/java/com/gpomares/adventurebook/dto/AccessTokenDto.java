package com.gpomares.adventurebook.dto;

public record AccessTokenDto(String accessToken, String tokenType, long expiresIn) {
}
