package com.fitness.exceptions;

public class TrainerNotAvailableException extends RuntimeException{
    public TrainerNotAvailableException(String message){
        super(message);
    }
}
