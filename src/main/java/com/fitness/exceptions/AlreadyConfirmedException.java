package com.fitness.exceptions;

public class AlreadyConfirmedException extends RuntimeException{
    public AlreadyConfirmedException(String message){
        super(message);
    }
}
