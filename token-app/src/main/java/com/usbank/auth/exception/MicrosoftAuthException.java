package com.usbank.auth.exception;

/**
 * Custom exception class for Microsoft Authentication errors.
 */
public class MicrosoftAuthException extends Exception {
    public MicrosoftAuthException(String message) {
        super(message);
    }

    public MicrosoftAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}