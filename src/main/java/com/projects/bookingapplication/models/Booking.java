package com.projects.bookingapplication.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Booking {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user; // Holds the User object

    @ManyToOne
    private Hotel hotel; // Holds the Hotel object

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer guests;

    // --- Constructors ---
    // Required by JPA
    public Booking() {}

    // --- Getters and Setters (FIXED and COMPLETED) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ⭐️ FIXED: Now accepts a User object and assigns it to the field
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // ⭐️ FIXED: Now assigns the Hotel object to the field
    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getGuests() {
        return guests;
    }

    public void setGuests(Integer guests) {
        this.guests = guests;
    }
}