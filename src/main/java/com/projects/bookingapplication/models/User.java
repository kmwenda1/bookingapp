package com.projects.bookingapplication.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.jspecify.annotations.Nullable;

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;
    private String password;


    public Long getId() {
        return id;
    }

    public void setPassword(@Nullable String encode) {
    }

    public static @Nullable CharSequence getPassword() {
        return null;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return  name;
    }
}

