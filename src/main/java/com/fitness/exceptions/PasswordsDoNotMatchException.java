package com.fitness.exceptions;

public class PasswordsDoNotMatchException extends RuntimeException{
    public PasswordsDoNotMatchException(String message){
        super(message);
    }
}
