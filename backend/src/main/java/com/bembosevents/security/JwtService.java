package com.bembosevents.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {

        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {

        Date now = new Date();
        Date expiration =
                new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        Claims claims = extractAllClaims(token);

        return claims.getSubject()
                .equalsIgnoreCase(userDetails.getUsername())
                && claims.getExpiration()
                .after(new Date());
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET no esta configurado");
        }

        byte[] keyBytes;

        try {
            keyBytes = Decoders.BASE64.decode(secret);
        }
        catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "JWT_SECRET debe estar codificado en Base64",
                    ex);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe contener al menos 256 bits");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}