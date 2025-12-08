package com.projects.bookingapplication.controllers;

import com.projects.bookingapplication.models.Booking;
import com.projects.bookingapplication.models.Hotel;
import com.projects.bookingapplication.models.User;
import com.projects.bookingapplication.repositories.BookingRepository;
import com.projects.bookingapplication.repositories.HotelRepository;
import com.projects.bookingapplication.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin
public class BookingController {

    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private HotelRepository hotelRepo;

    @PostMapping("/{hotelId}")
    public Booking create(
            @PathVariable Long hotelId,
            @RequestBody Booking booking,
            Authentication auth
    ) {
        Optional<User> user = userRepo.findByEmail(auth.getName());
        Hotel hotel = hotelRepo.findById(hotelId).orElseThrow();

        booking.setHotel(hotel);
        booking.setUser(user);

        return bookingRepo.save(booking);
    }

    @GetMapping
    public List<Booking> myBookings(Authentication auth) {

        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepo.findByUserId(user.getId());
    }
}
