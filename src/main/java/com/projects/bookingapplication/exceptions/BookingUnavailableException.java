package com.projects.bookingapplication.exceptions;

// A RuntimeException is sufficient for simplicity
public class BookingUnavailableException extends RuntimeException {
    public BookingUnavailableException(String message) {
        super(message);
    }
}
