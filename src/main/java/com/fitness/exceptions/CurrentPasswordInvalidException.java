package com.fitness.exceptions;

public class CurrentPasswordInvalidException extends RuntimeException{
    public CurrentPasswordInvalidException(String message) {
        super(message);
    }
}
