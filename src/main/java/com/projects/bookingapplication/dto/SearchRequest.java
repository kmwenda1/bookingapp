package com.projects.bookingapplication.dto;

import java.time.LocalDate;

public class SearchRequest {

    private String city;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numRooms;

    // ✅ Required: No-argument constructor for Jackson
    public SearchRequest() {}

    // Optional: Full constructor (useful for tests or manual creation)
    public SearchRequest(String city, LocalDate checkInDate, LocalDate checkOutDate, int numRooms) {
        this.city = city;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numRooms = numRooms;
    }

    // Getters and setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getNumRooms() { return numRooms; }
    public void setNumRooms(int numRooms) { this.numRooms = numRooms; }
}