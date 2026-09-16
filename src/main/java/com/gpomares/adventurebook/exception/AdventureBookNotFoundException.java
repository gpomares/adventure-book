package com.gpomares.adventurebook.exception;

public class AdventureBookNotFoundException extends RuntimeException {
    public AdventureBookNotFoundException(Long id) {
        super(String.format("AdventureBook with id %d not found", id));
    }
}
