package com.gpomares.adventurebook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AdventureBookNotFoundException extends RuntimeException {
    public AdventureBookNotFoundException(Long id) {
        super(String.format("AdventureBook with id %d not found", id));
    }
}
