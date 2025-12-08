package com.projects.bookingapplication.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private int availableRooms;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    // --- Constructors ---

    // Required by JPA
    public Inventory() {
    }

    // The constructor used in your DataInitializer
    public Inventory(Hotel hotel, LocalDate date, int availableRooms) {
        this.hotel = hotel;
        this.date = date;
        this.availableRooms = availableRooms;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(int availableRooms) {
        this.availableRooms = availableRooms;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}
