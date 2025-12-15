package com.cardgames.model.exception;

/**
 * Exception thrown when authentication fails due to invalid credentials.
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Constructs a new InvalidCredentialsException with the specified detail
     * message.
     *
     * @param message The detail message.
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
