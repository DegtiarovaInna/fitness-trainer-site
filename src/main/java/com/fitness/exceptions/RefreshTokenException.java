package com.fitness.exceptions;

public class RefreshTokenException extends RuntimeException{
    public RefreshTokenException(String message){
        super(message);
    }
}
