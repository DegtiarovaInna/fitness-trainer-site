package com.fitness.exceptions;

public class BookingAlreadyCancelledException extends RuntimeException{
    public BookingAlreadyCancelledException(String message){
        super(message);
    }
}
