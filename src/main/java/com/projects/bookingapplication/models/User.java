package com.projects.bookingapplication.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;
    private String password; // ⭐️ This field holds the hashed password

    // --- Constructors (Recommended to include for JPA) ---
    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // --- Getters and Setters (CRITICALLY CORRECTED) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // 🎯 FIX: Correct setter assigns the parameter value to the instance field.
    public void setPassword(String password) {
        this.password = password;
    }

    // 🎯 FIX: Correct getter returns the instance field's value.
    public String getPassword() {
        return this.password;
    }
}