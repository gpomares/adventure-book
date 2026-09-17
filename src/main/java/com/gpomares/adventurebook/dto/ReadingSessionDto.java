package com.gpomares.adventurebook.dto;

public record ReadingSessionDto(Long sessionId,
                                int health,
                                String status,
                                ConsequenceDto consequence,
                                PlayableSectionDto section) {
}
