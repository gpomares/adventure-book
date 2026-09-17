package com.gpomares.adventurebook.domain;

public class ReadingSessionConflictException extends RuntimeException {

    public ReadingSessionConflictException(String message) {
        super(message);
    }
}
