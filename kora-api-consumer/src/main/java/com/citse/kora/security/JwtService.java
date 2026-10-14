package com.citse.kora.security;

import com.citse.kora.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Valida los tokens JWT que emite Kundu API (HS256, misma clave secreta). Este servicio NO emite tokens.
 */
@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "application.security.jwt.secret-key")
    String SECRET_KEY;

    public String extractUsername(String TOKEN) {
        return extractClaim(TOKEN, Claims::getSubject);
    }

    public <T> T extractClaim(String TOKEN, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(TOKEN);
        return claimResolver.apply(claims);
    }

    public boolean isTokenValid(String TOKEN, User user) {
        final String username = extractUsername(TOKEN);
        return username.equals(user.getUsername()) && !isTokenExpired(TOKEN);
    }

    private boolean isTokenExpired(String TOKEN) {
        return extractClaim(TOKEN, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String TOKEN) {
        return Jwts.parserBuilder().setSigningKey(getSignKey())
                .build().parseClaimsJws(TOKEN).getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
