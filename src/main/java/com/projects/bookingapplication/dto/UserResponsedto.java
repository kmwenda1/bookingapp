package com.projects.bookingapplication.dto;



import com.projects.bookingapplication.models.User;

public class UserResponsedto {
    private Long id;
    private String name;
    private String email;

    // Static method to easily convert a User entity to this DTO
    public static UserResponsedto fromEntity(User user) {
        UserResponsedto dto = new UserResponsedto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    // --- Getters and Setters (REQUIRED) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}