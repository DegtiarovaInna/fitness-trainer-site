package com.fitness.exceptions;

public class StripeApiException extends RuntimeException {
    public StripeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

