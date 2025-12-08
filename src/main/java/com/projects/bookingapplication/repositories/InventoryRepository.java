package com.projects.bookingapplication.repositories;

import com.projects.bookingapplication.models.Hotel;
import com.projects.bookingapplication.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByHotelAndDateBetween(
            Hotel hotel,
            LocalDate startDate,
            LocalDate endDate
    );
}
