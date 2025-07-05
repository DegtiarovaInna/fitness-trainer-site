package com.fitness.exceptions.handler;

import com.fitness.exceptions.*;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<String> handleRateLimit(RequestNotPermitted ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Слишком много попыток входа, попробуйте позже");
    }

    // Универсальный формат ответа с ошибкой
    private ResponseEntity<Map<String, String>> buildResponse(String errorCode, String message, HttpStatus status) {
        Map<String, String> body = new HashMap<>();
        body.put("error", errorCode);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
// --- Пользовательские исключения ---
@ExceptionHandler(TimeSlotOverlapException.class)
public ResponseEntity<Map<String,String>> handleOverlap(TimeSlotOverlapException ex) {
    return buildResponse("TIME_SLOT_OVERLAP", ex.getMessage(), HttpStatus.CONFLICT);
}
    @ExceptionHandler(TimeSlotInvalidTimeException.class)
    public ResponseEntity<Map<String,String>> handleBadTime(TimeSlotInvalidTimeException ex) {
        return buildResponse("INVALID_TIME_RANGE", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return buildResponse("USER_NOT_FOUND", ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<Map<String, String>> handleUserNotAuthenticated(UserNotAuthenticatedException ex) {
        return buildResponse("USER_NOT_AUTHENTICATED", ErrorMessage.USER_NOT_AUTHENTICATED, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse("ACCESS_DENIED", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(StudioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleStudioNotFound(StudioNotFoundException ex) {
        return buildResponse("STUDIO_NOT_FOUND", ErrorMessage.STUDIO_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StudioAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleStudioAlreadyExists(StudioAlreadyExistsException ex) {
        return buildResponse("STUDIO_ALREADY_EXISTS", ErrorMessage.STUDIO_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBookingNotFound(BookingNotFoundException ex) {
        return buildResponse("BOOKING_NOT_FOUND", ErrorMessage.BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BookingAlreadyCancelledException.class)
    public ResponseEntity<Map<String, String>> handleBookingAlreadyCancelled(BookingAlreadyCancelledException ex) {
        return buildResponse("BOOKING_ALREADY_CANCELLED", ErrorMessage.BOOKING_ALREADY_CANCELLED, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BookingCreationNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleBookingCreationNotAllowed(BookingCreationNotAllowedException ex) {
        return buildResponse("BOOKING_CREATE_FOR_ANOTHER_USER", ErrorMessage.BOOKING_CREATE_FOR_ANOTHER_USER, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TimeSlotNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTimeSlotNotFound(TimeSlotNotFoundException ex) {
        return buildResponse("TIME_SLOT_NOT_FOUND", ErrorMessage.TIME_SLOT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TimeSlotNotAvailableException.class)
    public ResponseEntity<Map<String, String>> handleTimeSlotNotAvailable(TimeSlotNotAvailableException ex) {
        return buildResponse("TIME_SLOT_NOT_AVAILABLE", ErrorMessage.TIME_SLOT_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TrialSessionLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleTrialSessionLimitExceeded(TrialSessionLimitExceededException ex) {
        return buildResponse("TRIAL_SESSION_LIMIT_EXCEEDED", ErrorMessage.TRIAL_SESSION_LIMIT_EXCEEDED, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TrainerNotAvailableException.class)
    public ResponseEntity<Map<String, String>> handleTrainerNotAvailable(TrainerNotAvailableException ex) {
        return buildResponse("TRAINER_NOT_AVAILABLE", ex.getMessage(), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Дополнительно можно проверить текст ex.getRootCause().getMessage()
        // если нужно обрабатывать разные ограничения
        Map<String, String> body = Map.of(
                "error", "EMAIL_ALREADY_EXISTS",
                "message", ErrorMessage.USER_EMAIL_ALREADY_EXISTS
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
    // --- Общий обработчик непредвиденных ошибок ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllUncaughtException(Exception ex) {
        // Можно логировать ex для отладки
        ex.printStackTrace(); // В проде лучше логировать через логгер

        return buildResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
