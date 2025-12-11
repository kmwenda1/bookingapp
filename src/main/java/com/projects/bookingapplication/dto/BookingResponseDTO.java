package com.projects.bookingapplication.dto;

import com.projects.bookingapplication.models.Booking;
import com.projects.bookingapplication.models.Hotel;
import java.time.LocalDate;

public class BookingResponseDTO {
    private Long id;
    private UserResponsedto user; // ⭐️ Uses the safe DTO
    private Hotel hotel; // We'll keep the Hotel entity simple for now
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer guests;

    // Static method to easily convert a Booking entity to this DTO
    public static BookingResponseDTO fromEntity(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        // ⭐️ KEY CHANGE: Convert the User entity to the safe DTO
        dto.setUser(UserResponsedto.fromEntity(booking.getUser()));
        dto.setHotel(booking.getHotel());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setGuests(booking.getGuests());
        return dto;
    }

    // --- Getters and Setters (REQUIRED) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserResponsedto getUser() { return user; }
    public void setUser(UserResponsedto user) { this.user = user; }
    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }
}