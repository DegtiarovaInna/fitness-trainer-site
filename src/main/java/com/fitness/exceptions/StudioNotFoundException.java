package com.fitness.exceptions;

public class StudioNotFoundException extends RuntimeException{
    public StudioNotFoundException(String message) {
        super(message);
    }
}
