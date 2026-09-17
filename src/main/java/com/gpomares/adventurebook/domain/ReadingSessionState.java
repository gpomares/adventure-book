package com.gpomares.adventurebook.domain;

import java.util.Objects;

public record ReadingSessionState(ReadingSession session, Section section, Consequence consequence) {

    public ReadingSessionState {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(section, "section must not be null");
    }
}
