package com.projects.bookingapplication.controllers;

import com.projects.bookingapplication.dto.SearchRequest;
import com.projects.bookingapplication.models.Hotel;
import com.projects.bookingapplication.repositories.HotelRepository;
import com.projects.bookingapplication.services.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    // ✅ NEW: Public GET endpoint for browser testing (e.g., /api/hotels/search?city=Paris)
    @GetMapping("/search")
    public List<Hotel> quickSearch(@RequestParam(defaultValue = "") String city) {
        SearchRequest request = new SearchRequest();
        request.setCity(city);
        // If your service requires check-in/check-out, set defaults or handle nulls
        return hotelService.searchAvailableHotels(request);
    }

    // 🔹 POST endpoint for full search with body (use from frontend or Postman)
    @PostMapping("/search")
    public List<Hotel> searchHotels(@RequestBody SearchRequest request) {
        return hotelService.searchAvailableHotels(request);
    }

    // 🔹 GET all hotels (optional)
    @GetMapping
    public List<Hotel> getAllHotels(@RequestParam(defaultValue = "") String city) {
        return hotelRepository.findByCityContainingIgnoreCase(city);
    }

    // 🔹 GET hotel by ID
    @GetMapping("/{id}")
    public Hotel getHotel(@PathVariable Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
    }
}