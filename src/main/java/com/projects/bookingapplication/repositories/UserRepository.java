
package com.projects.bookingapplication.repositories;

import com.projects.bookingapplication.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Add this method for Spring Security to fetch users by their email (username)
    Optional<User> findByEmail(String email);
}