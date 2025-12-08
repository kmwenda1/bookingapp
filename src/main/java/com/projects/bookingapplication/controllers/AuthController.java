package com.projects.bookingapplication.controllers;

import com.projects.bookingapplication.models.User;
import com.projects.bookingapplication.repositories.UserRepository;
import com.projects.bookingapplication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        return userRepo.save(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String password = request.get("password");

        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null || !new BCryptPasswordEncoder().matches((CharSequence) password, (String) user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return Map.of("token", token);
    }


    @GetMapping("/me")
    public Optional<User> currentUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName());
    }
}
