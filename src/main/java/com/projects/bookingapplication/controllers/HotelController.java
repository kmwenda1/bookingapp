package com.projects.bookingapplication.controllers;

import com.projects.bookingapplication.models.Hotel;
import com.projects.bookingapplication.repositories.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin
public class HotelController {

    @Autowired
    private HotelRepository hotelRepo;

    @GetMapping
    public List<Hotel> search(
            @RequestParam(defaultValue="") String city
    ) {
        return hotelRepo.findByCityContainingIgnoreCase(city);
    }

    @GetMapping("/{id}")
    public Hotel getHotel(@PathVariable Long id) {
        return hotelRepo.findById(id).orElseThrow();
    }
}
