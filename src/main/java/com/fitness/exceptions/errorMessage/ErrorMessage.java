package com.fitness.exceptions.errorMessage;

public class ErrorMessage {
    public static final String ACCESS_DENIED_SELF_ONLY = "Access denied: можно смотреть/удалять только себя или обращаться к администратору";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_EMAIL_ALREADY_EXISTS = "User with this email already exists";
    public static final String USER_NOT_AUTHENTICATED = "User not authenticated";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String STUDIO_NOT_FOUND = "Studio not found";
    public static final String BOOKING_NOT_FOUND = "Booking not found";
    public static final String TIME_SLOT_NOT_FOUND = "Time slot not found";
    public static final String TIME_SLOT_NOT_AVAILABLE = "Time slot is not available";
    public static final String TRIAL_SESSION_LIMIT_EXCEEDED = "Trial session allowed only once per year";
    public static final String TRAINER_NOT_AVAILABLE = "Trainer not available at this time";
    public static final String BOOKING_CREATE_FOR_ANOTHER_USER = "You are not allowed to create a booking for another user.";
    public static final String BOOKING_ALREADY_CANCELLED = "Booking is already cancelled";
    public static final String STUDIO_ALREADY_EXISTS = "Studio with this name already exists";
    public static final String ACCESS_DENIED_NOT_YOUR_STUDIO = "Access denied: not your studio";
    public static final String TIME_SLOT_OVERLAP = "В этом зале на указанное время уже создан слот";
    public static final String INVALID_TIME_RANGE = "Время конца должно быть позже начала";
    public static final String TRAINER_NOT_AVAILABLE_STUDIO_SWITCH = "Trainer is not available for this time slot considering studio switch";
}
