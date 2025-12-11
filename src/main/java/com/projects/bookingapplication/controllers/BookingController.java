package com.projects.bookingapplication.controllers;

import com.projects.bookingapplication.dto.BookingResponseDTO; // ⭐️ New Import
import com.projects.bookingapplication.exceptions.BookingUnavailableException;
import com.projects.bookingapplication.models.Booking;
import com.projects.bookingapplication.models.Hotel;
import com.projects.bookingapplication.models.Inventory;
import com.projects.bookingapplication.models.User;
import com.projects.bookingapplication.repositories.BookingRepository;
import com.projects.bookingapplication.repositories.HotelRepository;
import com.projects.bookingapplication.repositories.InventoryRepository;
import com.projects.bookingapplication.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    @Autowired
    private InventoryRepository inventoryRepository;

    @PostMapping("/{hotelId}")
    // ⭐️ DTO CHANGE: Return the safe DTO instead of the raw Entity
    public BookingResponseDTO create(
            @PathVariable Long hotelId,
            @RequestBody Booking booking,
            Authentication auth
    ) {
        // 1. Resolve User and Hotel Entities
        String userEmail = auth.getName();
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB"));

        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        // 2. Link entities to the Booking
        booking.setHotel(hotel);
        booking.setUser(user);

        // 3. INVENTORY DEDUCTION LOGIC
        LocalDate date = booking.getStartDate();
        LocalDate lastBookedDate = booking.getEndDate().minusDays(1);

        while (!date.isAfter(lastBookedDate)) {
            List<Inventory> inventoryRecords = inventoryRepository.findByHotelAndDateBetween(
                    hotel,
                    date,
                    date
            );

            if (inventoryRecords.isEmpty() || inventoryRecords.get(0).getAvailableRooms() < 1) {
                throw new BookingUnavailableException("Inventory not available for date: " + date + ". Booking declined.");
            }

            Inventory inventory = inventoryRecords.get(0);
            inventory.setAvailableRooms(inventory.getAvailableRooms() - 1);
            inventoryRepository.save(inventory);

            date = date.plusDays(1);
        }

        // 4. Save the new booking entity
        Booking savedBooking = bookingRepo.save(booking);

        // 5. ⭐️ DTO CHANGE: Convert the saved entity to the safe DTO before returning
        return BookingResponseDTO.fromEntity(savedBooking);
    }

    @GetMapping
    // ⭐️ DTO CHANGE: Return a List of safe DTOs
    public List<BookingResponseDTO> myBookings(Authentication auth) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the list of raw Booking entities
        List<Booking> bookings = bookingRepo.findByUserId(user.getId());

        // ⭐️ DTO CHANGE: Map the list of entities to a list of DTOs
        return bookings.stream()
                .map(BookingResponseDTO::fromEntity)
                .toList();
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        // 1. Resolve User and Booking
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found."));

        // 2. Security Check: Ensure the logged-in user owns the booking
        if (!booking.getUser().getId().equals(user.getId())) {
            return new ResponseEntity<>("Forbidden: You do not own this booking.", HttpStatus.FORBIDDEN); // 403
        }

        // 3. Inventory Reversal
        LocalDate date = booking.getStartDate();
        LocalDate lastBookedDate = booking.getEndDate().minusDays(1);

        while (!date.isAfter(lastBookedDate)) {
            List<Inventory> inventoryRecords = inventoryRepository.findByHotelAndDateBetween(
                    booking.getHotel(),
                    date,
                    date
            );

            if (!inventoryRecords.isEmpty()) {
                Inventory inventory = inventoryRecords.get(0);
                inventory.setAvailableRooms(inventory.getAvailableRooms() + 1);
                inventoryRepository.save(inventory);
            }

            date = date.plusDays(1);
        }

        // 4. Delete the Booking Record
        bookingRepo.delete(booking);

        return new ResponseEntity<>("Booking cancelled successfully, and inventory restored.", HttpStatus.NO_CONTENT);
    }
}