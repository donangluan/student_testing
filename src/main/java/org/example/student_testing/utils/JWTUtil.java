package org.example.student_testing.utils;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JWTUtil {
    @Value("${jwt.secretKey}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private int expiration;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }


    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis()+expiration))
        .signWith(key)
        .compact();
    }

    public Claims extractAllClaims(String token){
        return Jwts
        .parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        String username = extractAllClaims(token).getSubject();
        return (userDetails.getUsername() == username && isTokenExpired(token));
    }

    private boolean isTokenExpired(String token){
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
