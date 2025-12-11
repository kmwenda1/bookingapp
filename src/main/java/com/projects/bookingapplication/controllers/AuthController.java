package com.projects.bookingapplication.controllers;

import com.projects.bookingapplication.models.User;
import com.projects.bookingapplication.repositories.UserRepository;
import com.projects.bookingapplication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // ⭐️ REMOVE THIS if you use the interface
import org.springframework.security.crypto.password.PasswordEncoder; // ⭐️ New Import: Use the Interface
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private PasswordEncoder passwordEncoder; // ⭐️ INJECTED THE BEAN

    // --- REGISTER METHOD (FIXED to use the injected PasswordEncoder bean) ---
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        // ⭐️ FIX: Use the injected passwordEncoder bean to ensure consistency
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    // --- LOGIN METHOD (Correctly uses AuthenticationManager) ---
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String password = request.get("password");

        try {
            // This is correct: delegates authentication to the manager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password", e);
        }

        // If authentication succeeds
        String token = jwtUtil.generateToken(email);

        return Map.of("token", token);
    }

    // --- CURRENT USER METHOD ---
    @GetMapping("/me")
    public Optional<User> currentUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName());
    }
}