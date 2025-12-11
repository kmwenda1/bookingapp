package com.projects.bookingapplication.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm}")
    private String SECRET;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long expiration;

    // Generate key properly for HS256
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)  // ✅ Use setSubject instead of subject
                .setIssuedAt(new Date())  // ✅ Use setIssuedAt instead of issuedAt
                .setExpiration(new Date(System.currentTimeMillis() + expiration))  // ✅ Use setExpiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // ✅ Use SignatureAlgorithm.HS256
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

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
        return Jwts.parserBuilder()  // ✅ Use parserBuilder instead of parser
                .setSigningKey(getSigningKey())  // ✅ Use setSigningKey
                .build()
                .parseClaimsJws(token)  // ✅ Use parseClaimsJws
                .getBody();  // ✅ Use getBody instead of getPayload
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}