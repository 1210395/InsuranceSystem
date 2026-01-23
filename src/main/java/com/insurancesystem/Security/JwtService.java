package com.insurancesystem.Security;

import com.insurancesystem.Model.Entity.RevokedToken;
import com.insurancesystem.Repository.RevokedTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    private final Key key;
    private final long ttlMillis;
    private final RevokedTokenRepository revokedTokenRepository;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-ms:86400000}") long ttlMillis,
            RevokedTokenRepository revokedTokenRepository
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = ttlMillis;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    // أبسط توليد
    public String generateToken(String email) {
        return generateToken(email, Map.of());
    }


    // توليد مع Claims إضافية (اختياري)
    public String generateToken(String email, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256);

        if (extraClaims != null && !extraClaims.isEmpty()) {
            builder.addClaims(extraClaims);
        }
        return builder.compact();
    }

    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return getAllClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            if (isRevoked(token)) return false;
            Claims claims = getAllClaims(token);
            boolean notExpired = claims.getExpiration() != null && claims.getExpiration().after(new Date());
            boolean subjectOk = username == null || username.equalsIgnoreCase(claims.getSubject());
            return notExpired && subjectOk;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // تعليم التوكن كملغى حتى وقت انتهاء صلاحيته
    public void revoke(String token) {
        try {
            Date exp = extractExpiration(token);
            if (exp != null && !revokedTokenRepository.existsByToken(token)) {
                RevokedToken revokedToken = RevokedToken.builder()
                        .token(token)
                        .revokedAt(Instant.now())
                        .expiresAt(exp.toInstant())
                        .build();
                revokedTokenRepository.save(revokedToken);
            }
        } catch (Exception e) {
            log.warn("Failed to revoke token: {}", e.getMessage());
        }
    }

    public boolean isRevoked(String token) {
        return revokedTokenRepository.existsByToken(token);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
