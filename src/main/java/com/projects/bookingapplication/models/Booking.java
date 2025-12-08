package com.projects.bookingapplication.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;
import java.util.Optional;

@Entity
public class Booking {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Hotel hotel;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer guests;

            public void setUser(Optional<User> user) {
    }

    public void setHotel(Hotel hotel) {

    }
}
