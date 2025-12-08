package com.projects.bookingapplication.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; // Use this for proper identity generation
import jakarta.persistence.Id;
import jakarta.persistence.Table; // Optional: Explicit table name

@Entity
@Table(name = "hotel") // Good practice to explicitly name the table
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Best practice for MySQL
    private Long id;

    private String name;
    private String city;
    private Double price;
    private String description;
    // Add the rating field that was in the DataInitializer example:
    private Integer rating;

    // --- Constructors ---

    // 1. No-argument constructor (REQUIRED by JPA/Hibernate)
    public Hotel() {
    }

    // 2. Parameterized constructor (Optional: helpful for initialization)
    public Hotel(String name, String city, Double price, String description, Integer rating) {
        this.name = name;
        this.city = city;
        this.price = price;
        this.description = description;
        this.rating = rating;
    }

    // --- Getters and Setters (REQUIRED for all fields) ---

    // Getter and Setter for ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter and Setter for Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for City (Used by HotelService)
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    // Getter and Setter for Price
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    // Getter and Setter for Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter and Setter for Rating
    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}