package com.projects.bookingapplication.controllers;

import com.projects.bookingapplication.exceptions.BookingUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles our custom inventory exception
    @ExceptionHandler(BookingUnavailableException.class)
    public ResponseEntity<String> handleBookingUnavailableException(BookingUnavailableException ex) {
        // Return 409 Conflict, meaning the request was valid but could not be completed
        // due to a resource conflict (inventory issue).
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // Returns 409
    }

    // Optional: Add more handlers for other exceptions like Hotel Not Found (404)
    // @ExceptionHandler(NoSuchElementException.class)
    // public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
    //     return new ResponseEntity<>("Resource not found.", HttpStatus.NOT_FOUND); // Returns 404
    // }
}