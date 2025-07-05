package com.fitness.exceptions;

public class TimeSlotOverlapException extends RuntimeException{
    public TimeSlotOverlapException(String msg) { super(msg); }
}
