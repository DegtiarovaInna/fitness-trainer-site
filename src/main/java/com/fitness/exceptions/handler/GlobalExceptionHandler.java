package com.fitness.exceptions.handler;

import com.fitness.exceptions.*;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, String>> buildResponse(String errorCode, String message, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(Map.of("error", errorCode, "message", message));
    }

    // 429
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<?> rateLimit(RequestNotPermitted ex) {
        return buildResponse("RATE_LIMIT", "Too many requests", HttpStatus.TOO_MANY_REQUESTS);
    }


    // 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    // 401
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidTokenException ex) {
        return buildResponse("INVALID_TOKEN", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<Map<String, String>> handleRefreshError(RefreshTokenException ex) {
        return buildResponse("INVALID_REFRESH_TOKEN", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // 401
    @ExceptionHandler(CurrentPasswordInvalidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCurrentPassword(CurrentPasswordInvalidException ex) {
        return buildResponse("INVALID_CURRENT_PASSWORD", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // 401
    @ExceptionHandler(EmailNotConfirmedException.class)
    public ResponseEntity<Map<String,String>> handleEmailNotConfirmed(EmailNotConfirmedException ex) {
        return buildResponse(
                "EMAIL_NOT_CONFIRMED",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED
        );
    }
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String,String>> handleDisabled(DisabledException ex) {
        return buildResponse(
                "EMAIL_NOT_CONFIRMED",
                ErrorMessage.EMAIL_NOT_CONFIRMED,
                HttpStatus.UNAUTHORIZED
        );
    }
    // 403
    @ExceptionHandler({AccessDeniedException.class, BookingCreationNotAllowedException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(RuntimeException ex) {
        return buildResponse("FORBIDDEN", ex.getMessage(), HttpStatus.FORBIDDEN);
    }


    // 404
    @ExceptionHandler({
            UserNotFoundException.class,
            StudioNotFoundException.class,
            BookingNotFoundException.class,
            TimeSlotNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return buildResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 409
    @ExceptionHandler({
            StudioAlreadyExistsException.class,
            TimeSlotOverlapException.class
    })
    public ResponseEntity<Map<String, String>> handleConflict(RuntimeException ex) {
        return buildResponse("CONFLICT", ex.getMessage(), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(UserHasActiveBookings.class)
    public ResponseEntity<Map<String, String>> handleActiveBookings(UserHasActiveBookings ex) {
        return buildResponse("USER_HAS_ACTIVE_BOOKINGS",
                ex.getMessage(),
                HttpStatus.CONFLICT);
    }
    // 400
    @ExceptionHandler({
            AlreadyConfirmedException.class,
            PasswordsDoNotMatchException.class,
            BookingAlreadyCancelledException.class,
            TrialSessionLimitExceededException.class,
            TimeSlotInvalidTimeException.class,
            TimeSlotNotAvailableException.class,
            TrainerNotAvailableException.class
    })
    public ResponseEntity<Map<String, String>> handleBusinessErrors(RuntimeException ex) {
        return buildResponse("BUSINESS_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    // 400
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return buildResponse("EMAIL_ALREADY_EXISTS", ErrorMessage.USER_EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllUncaughtException(Exception ex) {
        ex.printStackTrace();
        return buildResponse("INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
