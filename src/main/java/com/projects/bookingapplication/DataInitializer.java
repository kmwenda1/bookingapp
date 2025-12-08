package com.projects.bookingapplication;



import com.projects.bookingapplication.models.Hotel;
import com.projects.bookingapplication.models.Inventory;
import com.projects.bookingapplication.repositories.HotelRepository;
import com.projects.bookingapplication.repositories.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;

    public DataInitializer(HotelRepository hotelRepository, InventoryRepository inventoryRepository) {
        this.hotelRepository = hotelRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing dummy hotel and inventory data...");

        // 1. Create a Hotel
        Hotel hilton = new Hotel();
        hilton.setName("Hilton Nairobi");
        hilton.setCity("Nairobi");
        hilton.setRating(5);
        hotelRepository.save(hilton);

        // 2. Create Inventory for the next few days
        LocalDate today = LocalDate.now();
        List<Inventory> inventoryList = List.of(
                new Inventory(hilton, today, 20), // 20 rooms available today
                new Inventory(hilton, today.plusDays(1), 15), // 15 rooms tomorrow
                new Inventory(hilton, today.plusDays(2), 25) // 25 rooms day after
        );
        inventoryRepository.saveAll(inventoryList);

        System.out.println("Dummy data initialization complete.");
    }
}