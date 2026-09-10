package com.paybank.hexagonal.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import io.jsonwebtoken.Jwts;
import java.util.Date;

@Service
public class JwtService {

    //Remplacez par une clé secrète robuste d'au moins 256 bits (32 caractères minimum)
    private final String SECRET_KEY_STRING = "cqo5B75Y+m4UgpVzeRcLvviCOGNVPLwCcLWWGt8weag=";

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) //Valide 24 heures
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Génère un token signé attestant qu'un utilisateur (identifié par son email) a validé
     * le mot de passe standard d'un rôle donné, via le sélecteur "opérateur actif".
     * Ce token est stocké dans un second cookie (activeRoleToken), distinct du cookie de
     * connexion (accessToken), et vérifié à chaque requête par JwtCookieFilter.
     */
    public String generateRoleToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("activeRole", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 8)) //Valide 8 heures
                .signWith(getSigningKey())
                .compact();
    }

    /** Extrait le rôle actif porté par un token généré via generateRoleToken(). */
    public String extractActiveRole(String token) {
        return extractClaim(token, claims -> claims.get("activeRole", String.class));
    }
}