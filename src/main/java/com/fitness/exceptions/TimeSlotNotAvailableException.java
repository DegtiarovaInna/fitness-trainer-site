package com.fitness.exceptions;

public class TimeSlotNotAvailableException extends RuntimeException{
    public TimeSlotNotAvailableException(String message){
        super(message);
    }
}
