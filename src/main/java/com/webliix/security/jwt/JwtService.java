package com.webliix.security.jwt;

import com.webliix.security.model.WebliixPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        if (userDetails instanceof WebliixPrincipal principal) {
            extraClaims.put("uid", principal.getUserId());
            extraClaims.put("tv", principal.getTokenVersion() != null ? principal.getTokenVersion() : 0);
        }
        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Integer extractTokenVersion(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object tv = claims.get("tv");
            if (tv instanceof Number num) {
                return num.intValue();
            }
        } catch (Exception e) {
            log.debug("No valid token_version claim in token: {}", e.getMessage());
        }
        return null;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
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

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
                return false;
            }

            if (userDetails instanceof WebliixPrincipal principal) {
                if (!principal.isEnabled()) {
                    log.warn("Token validation failed: User {} is disabled", username);
                    return false;
                }
                if (!principal.isEmailVerified()) {
                    log.warn("Token validation failed: User {} email is not verified", username);
                    return false;
                }
                Integer tokenVersionInClaim = extractTokenVersion(token);
                if (tokenVersionInClaim != null && principal.getTokenVersion() != null) {
                    if (!tokenVersionInClaim.equals(principal.getTokenVersion())) {
                        log.warn("Token validation failed: Stale token_version {} for user {} (current is {})",
                                tokenVersionInClaim, username, principal.getTokenVersion());
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
