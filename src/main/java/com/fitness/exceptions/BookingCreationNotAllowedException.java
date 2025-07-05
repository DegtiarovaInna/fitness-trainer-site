package com.fitness.exceptions;

public class BookingCreationNotAllowedException extends RuntimeException{
    public BookingCreationNotAllowedException(String message) {
        super(message);
    }
}
