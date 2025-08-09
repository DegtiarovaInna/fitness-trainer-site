package com.fitness.exceptions;

import java.util.Map;

public record ErrorResponse(String error, String message, Map<String, String> fields) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }
    public static ErrorResponse ofValidation(Map<String, String> fields) {
        return new ErrorResponse("VALIDATION_ERROR", "Validation failed", fields);
    }
}