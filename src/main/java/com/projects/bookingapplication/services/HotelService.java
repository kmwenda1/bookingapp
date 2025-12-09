package com.projects.bookingapplication.services;

import com.projects.bookingapplication.dtos.SearchRequest; // Assuming you placed SearchRequest here
import com.projects.bookingapplication.models.Hotel; // Assuming your Hotel model is here
import com.projects.bookingapplication.models.Inventory; // Assuming your Inventory model is here
import com.projects.bookingapplication.repositories.HotelRepository;
import com.projects.bookingapplication.repositories.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HotelService {

    // 1. Inject Repositories
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;

    // Use Constructor Injection (Preferred method in Spring)
    @Autowired
    public HotelService(HotelRepository hotelRepository, InventoryRepository inventoryRepository) {
        this.hotelRepository = hotelRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Finds hotels in a city that have enough available rooms for the entire stay period.
     * @param request The user's search criteria (SearchRequest DTO).
     * @return List of available Hotel entities.
     */
    public List<Hotel> searchAvailableHotels(SearchRequest request) {
        // --- 2. Input Validation (Basic) ---
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null || request.getNumRooms() <= 0) {
            // A real application would throw a custom validation exception here
            return List.of();
        }

        // 3. Basic filter by city
        List<Hotel> candidateHotels = hotelRepository.findByCity(request.getCity());
        List<Hotel> availableHotels = new ArrayList<>();

        if (candidateHotels.isEmpty()) {
            return availableHotels;
        }

        // 4. Iterate through each candidate hotel to check inventory
        for (Hotel hotel : candidateHotels) {

            // Booking check usually runs up to the day BEFORE check-out
            LocalDate endDate = request.getCheckOutDate().minusDays(1);

            List<Inventory> inventoryRecords = inventoryRepository.findByHotelAndDateBetween(
                    hotel,
                    request.getCheckInDate(),
                    endDate);

            // 5. Perform the availability check
            if (isHotelAvailable(inventoryRecords, request.getCheckInDate(), endDate, request.getNumRooms())) {
                availableHotels.add(hotel);
            }
        }

        return availableHotels;
    }

    /**
     * Helper method to check if the inventory records cover all dates and satisfy the room count.
     */
    private boolean isHotelAvailable(
            List<Inventory> inventoryRecords,
            LocalDate startDate,
            LocalDate endDate,
            int requiredRooms) {

        // Map inventory records by date for easy lookup
        Map<LocalDate, Integer> availabilityMap = inventoryRecords.stream()
                .collect(Collectors.toMap(
                        Inventory::getDate,
                        Inventory::getAvailableRooms));

        // Iterate from start date up to and including the end date
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            Integer available = availabilityMap.get(date);

            // If no inventory record exists for the date, or rooms are insufficient
            if (available == null || available < requiredRooms) {
                return false;
            }

            date = date.plusDays(1);
        }

        // Passed all checks for the entire stay duration
        return true;
    }


}