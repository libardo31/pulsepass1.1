package com.pulsepass.security.service;

import com.pulsepass.security.auth.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final long expirationSeconds;

    public JwtService(@Value("${pulsepass.security.jwt.secret-base64}") String secretBase64,
                      @Value("${pulsepass.security.jwt.issuer:pulsepass-api}") String issuer,
                      @Value("${pulsepass.security.jwt.expiration-seconds:900}") long expirationSeconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        if (keyBytes.length < 32) throw new IllegalArgumentException("JWT secret must decode to at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UserPrincipal user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("roles", user.getAuthorities().stream().map(a -> a.getAuthority()).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(issuer).build()
                .parseSignedClaims(token).getPayload();
    }

    public long getExpirationSeconds() { return expirationSeconds; }
}
