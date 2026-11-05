package com.project.back_end.services.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor  
public class TokenServiceImpl {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;


    public SecretKey getSigningKey(){
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String identifier, String role){
        long expirationTimeInMillis = 7L * 24 * 60 * 60 * 1000;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeInMillis);

        return Jwts.builder()
                .subject(identifier)
                .claim("role", role.toLowerCase())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    private Claims extractClaims(String token){
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token){
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token){
        return extractClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token, String role){
        if (role == null) {
            return false;
        }
        try {
            String identifier = extractEmail(token);
            if (identifier == null) {
                return false;
            }

            return switch (role.toLowerCase()) {
                case "admin"   -> adminRepository.findByUsername(identifier) != null;
                case "doctor"  -> doctorRepository.findByEmail(identifier) != null;
                case "patient" -> patientRepository.findByEmail(identifier) != null;
                default        -> false;
            };
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
