package com.gpomares.adventurebook.web.json;

import com.gpomares.adventurebook.dto.ConsequenceDto;

public record ReadingSession(Long sessionId,
                             int health,
                             String status,
                             ConsequenceDto consequence,
                             PlayableSection section) {
}
