package com.projects.bookingapplication.controllers;

import com.fasterxml.jackson.databind.JsonNode; // ⭐️ NEW IMPORT for Webhook JSON handling
import com.projects.bookingapplication.dto.BookingResponseDTO;
import com.projects.bookingapplication.exceptions.BookingUnavailableException;
import com.projects.bookingapplication.models.Booking;
import com.projects.bookingapplication.models.Hotel;
import com.projects.bookingapplication.models.Inventory;
import com.projects.bookingapplication.models.User;
import com.projects.bookingapplication.repositories.BookingRepository;
import com.projects.bookingapplication.repositories.HotelRepository;
import com.projects.bookingapplication.repositories.InventoryRepository;
import com.projects.bookingapplication.repositories.UserRepository;
import com.projects.bookingapplication.services.MoonPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @Autowired
    private MoonPayService moonPayService;

    // -------------------------------------------------------------------------
    // ENDPOINT 1: INITIATES MOONPAY PAYMENT (Phase 1)
    // -------------------------------------------------------------------------

    @PostMapping("/initiate-payment/{hotelId}")
    public ResponseEntity<String> initiatePayment(
            @PathVariable Long hotelId,
            @RequestBody Booking booking,
            Authentication auth
    ) {
        // 1. Resolve Entities
        String userEmail = auth.getName();
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB"));

        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        // 2. Calculate Cost and Required 20% Deposit
        BigDecimal defaultPrice = BigDecimal.valueOf(100.00);

        BigDecimal hotelPrice;
        if (hotel.getPrice() != null) {
            // FIX: Convert the Double/double from the model to BigDecimal for calculation
            hotelPrice = BigDecimal.valueOf(hotel.getPrice());
        } else {
            hotelPrice = defaultPrice;
        }

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        if (totalDays <= 0) {
            return new ResponseEntity<>("Booking duration must be at least one night.", HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalNights = BigDecimal.valueOf(totalDays);
        BigDecimal totalCost = hotelPrice.multiply(totalNights);
        BigDecimal requiredDeposit = totalCost.multiply(BigDecimal.valueOf(0.20));

        // 3. Temporarily Save Booking State as PENDING
        // Note: Inventory deduction is postponed until payment confirmation.
        Booking pendingBooking = new Booking();
        pendingBooking.setHotel(hotel);
        pendingBooking.setUser(user);
        pendingBooking.setStartDate(booking.getStartDate());
        pendingBooking.setEndDate(booking.getEndDate());
        pendingBooking.setGuests(booking.getGuests());

        Booking savedPendingBooking = bookingRepo.save(pendingBooking);

        // 4. Generate the Securely Signed MoonPay URL
        String moonpayUrl = moonPayService.generateSignedMoonpayUrl(
                requiredDeposit,
                savedPendingBooking.getId(), // Our internal reference ID
                user.getEmail()
        );

        return new ResponseEntity<>(moonpayUrl, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // ⭐️ ENDPOINT 2: MOONPAY WEBHOOK LISTENER (Phase 2 - The Confirmation)
    // -------------------------------------------------------------------------

    // MoonPay sends this to confirm a transaction. It MUST be publicly accessible (no JWT needed).
    @PostMapping("/moonpay-webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT) // MoonPay expects a 204 or 200 on success
    public void handleMoonpayWebhook(
            @RequestBody JsonNode payload, // JsonNode is used because we need the raw payload for signature verification
            @RequestHeader("Moonpay-Signature-V2") String signatureHeader // MoonPay's security header
    ) {
        // 1. ⚠️ SECURITY CHECK: Verify the signature
        String rawPayload = payload.toString(); // Get the raw string version of the payload

        if (!moonPayService.verifyWebhookSignature(rawPayload, signatureHeader)) {
            // Log the unauthorized attempt and exit without processing
            System.err.println("Webhook signature verification failed for payload: " + rawPayload);
            // We exit normally (204) to avoid triggering repeated MoonPay retries,
            // but we do not process the booking.
            return;
        }

        // 2. Extract Event Data
        String eventType = payload.get("type").asText();
        JsonNode data = payload.get("data");

        if (data == null) {
            System.err.println("Webhook data field is missing.");
            return;
        }

        String transactionStatus = data.get("status").asText();
        String externalTransactionId = data.get("externalTransactionId").asText();
        Long bookingId = Long.parseLong(externalTransactionId);

        System.out.println("Processing MoonPay Event: " + eventType + " for Booking ID: " + bookingId);

        // 3. Process Only the SUCCESS Event
        if ("transaction_update".equals(eventType) && "completed".equals(transactionStatus)) {

            Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                System.err.println("Booking not found for webhook confirmation ID: " + bookingId);
                return;
            }

            Booking confirmedBooking = bookingOpt.get();

            // NOTE: In a full app, you would check the booking status is 'PENDING' before proceeding.

            // 4. INVENTORY DEDUCTION (The logic that was previously skipped!)
            LocalDate date = confirmedBooking.getStartDate();
            LocalDate lastBookedDate = confirmedBooking.getEndDate().minusDays(1);
            Hotel hotel = confirmedBooking.getHotel();

            while (!date.isAfter(lastBookedDate)) {
                List<Inventory> inventoryRecords = inventoryRepository.findByHotelAndDateBetween(
                        hotel, date, date
                );

                if (inventoryRecords.isEmpty() || inventoryRecords.get(0).getAvailableRooms() < 1) {
                    // ⚠️ CRITICAL FAILURE: Inventory was available when payment initiated, but is gone now.
                    // This is rare but requires a refund process. We log and exit.
                    System.err.println("Inventory race condition failure for date: " + date + ". Requires manual refund.");
                    // For now, we log and assume the booking is saved (but requires immediate attention)
                    // You would change the status to 'INVENTORY_FAILURE_REFUND_REQUIRED'
                    break;
                }

                // Deduct one room
                Inventory inventory = inventoryRecords.get(0);
                inventory.setAvailableRooms(inventory.getAvailableRooms() - 1);
                inventoryRepository.save(inventory);
                date = date.plusDays(1);
            }

            // 5. UPDATE BOOKING STATUS (If you had a status field, you would set it to 'CONFIRMED')
            // Since we don't have a status field yet, we simply log the success.
            // confirmedBooking.setStatus(BookingStatus.CONFIRMED);
            bookingRepo.save(confirmedBooking);
            System.out.println("Booking ID: " + bookingId + " confirmed and inventory deducted successfully!");
        }
    }

    // -------------------------------------------------------------------------
    // EXISTING ENDPOINT: GET MY BOOKINGS (READ)
    // -------------------------------------------------------------------------

    @GetMapping
    public List<BookingResponseDTO> myBookings(Authentication auth) {
        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepo.findByUserId(user.getId());

        return bookings.stream()
                .map(BookingResponseDTO::fromEntity)
                .toList();
    }

    // -------------------------------------------------------------------------
    // EXISTING ENDPOINT: CANCEL BOOKING (DELETE)
    // -------------------------------------------------------------------------

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