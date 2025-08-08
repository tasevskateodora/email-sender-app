package com.example.iwemailsender.security.jwt;

import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // Use a strong secret key (in production, load from environment variables)
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long JWT_EXPIRATION = 86400000; // 24 hours

    private final UserRepository userRepository;

    public JwtUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

  // Generate JWT token
    public String generateToken(UserDetails userDetails) {
        log.debug("Generating JWT token for user: {}", userDetails.getUsername());

        Map<String, Object> claims = new HashMap<>();

        // Add roles to JWT claims
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        log.debug("Adding roles to JWT: {}", claims.get("roles"));

        String token = createToken(claims, userDetails.getUsername());
        log.debug("JWT token generated successfully for: {}", userDetails.getUsername());

        return token;
    }


        // Create token with claims
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Extract username from JWT
    public String extractUsername(String token) {
        log.debug("Extracting username from JWT");
        String username = extractClaim(token, Claims::getSubject);
        log.debug("Extracted username: {}", username);
        return username;
    }

    // Extract expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract claim using function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims
    private Claims extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.debug("JWT claims extracted: sub={}, roles={}, exp={}",
                    claims.getSubject(),
                    claims.get("roles"),
                    claims.getExpiration());

            return claims;
        } catch (Exception e) {
            log.error("Failed to parse JWT claims: {}", e.getMessage());
            throw e;
        }
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean expired = expiration.before(new Date());
        log.debug("Token expired: {}, expiration: {}", expired, expiration);
        return expired;
    }

    // Validate JWT token
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean usernameMatches = username.equals(userDetails.getUsername());
            boolean tokenNotExpired = !isTokenExpired(token);

            log.debug("JWT validation - Username matches: {}, Token not expired: {}",
                    usernameMatches, tokenNotExpired);

            boolean isValid = usernameMatches && tokenNotExpired;
            log.debug("JWT token validation result: {}", isValid);

            return isValid;

        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
