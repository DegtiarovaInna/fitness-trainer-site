package com.fitness.exceptions;

public class UserHasActiveBookings extends RuntimeException{
    public UserHasActiveBookings(String message) {
        super(message);
    }
}
