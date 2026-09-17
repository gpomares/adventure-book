package com.gpomares.adventurebook.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("An account with that email already exists");
    }
}
