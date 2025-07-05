package com.fitness.exceptions;

public class StudioAlreadyExistsException extends RuntimeException{
    public StudioAlreadyExistsException(String message){
        super(message);
    }
}
