package com.projects.bookingapplication.util;


import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // IMPORTANT: The secret key should be stored in application.properties in a real app
    private static final String SECRET = "0778901234koomemwendabaraka0714914897";
    private static final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());

    // --- Token Generation ---
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hours
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    // --- Token Validation ---
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // The token is valid if the username matches the UserDetails username AND it's not expired.
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // --- Claim Extraction ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) //
                .build()
                .parseSignedClaims(token) //
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}