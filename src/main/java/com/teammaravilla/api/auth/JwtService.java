package com.teammaravilla.api.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private static final String ISSUER = "teammaravilla-api";
    private final SecretKey key;
    private final Duration expiration;
    private final JwtParser parser;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration}") String expiration) {
        try {
            byte[] bytes = Base64.getDecoder().decode(secret);
            if (bytes.length != 32) throw new IllegalArgumentException();
            this.key = Keys.hmacShaKeyFor(bytes);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT_SECRET debe ser Base64 de 32 bytes aleatorios");
        }
        try {
            this.expiration = Duration.parse(expiration);
            if (this.expiration.isNegative() || this.expiration.toSeconds() == 0
                    || this.expiration.getNano() != 0) throw new IllegalArgumentException();
        } catch (RuntimeException ex) {
            throw new IllegalStateException("JWT_EXPIRATION debe ser una duración ISO-8601 positiva en segundos enteros");
        }
        this.parser = Jwts.parser().verifyWith(key).requireIssuer(ISSUER)
                .sig().clear().add(Jwts.SIG.HS256).and()
                .zip().clear().and().build();
    }

    public String create(UUID userId) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return Jwts.builder().issuer(ISSUER).subject(userId.toString())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(expiration)))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public UUID validateAndGetUserId(String token) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null) throw new JwtException("Falta expiración");
        String subject = claims.getSubject();
        if (subject == null) throw new JwtException("Falta identidad");
        UUID id = UUID.fromString(subject);
        if (!id.toString().equals(subject)) throw new JwtException("Identidad inválida");
        return id;
    }

    public long expiresIn() { return expiration.toSeconds(); }
}
