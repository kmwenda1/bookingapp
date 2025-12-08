package com.projects.bookingapplication.repositories;

import com.projects.bookingapplication.models.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Add this method:
    List<Hotel> findByCity(String city);

    List<Hotel> findByCityContainingIgnoreCase(String city);
}