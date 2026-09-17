package com.gpomares.adventurebook.domain;

public class ReadingSessionNotFoundException extends RuntimeException {

    public ReadingSessionNotFoundException(Long id) {
        super("Reading session with id %d was not found".formatted(id));
    }
}
