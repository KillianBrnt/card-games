package com.cardgames.model.exception;

/**
 * Exception thrown when a user attempts to access a resource or perform an
 * action
 * for which they do not have the necessary permissions.
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Constructs a new AccessDeniedException with the specified detail message.
     *
     * @param message The detail message.
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
