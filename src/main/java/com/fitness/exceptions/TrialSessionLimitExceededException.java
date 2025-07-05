package com.fitness.exceptions;

public class TrialSessionLimitExceededException extends RuntimeException{
    public TrialSessionLimitExceededException(String message){
        super(message);
    }
}
