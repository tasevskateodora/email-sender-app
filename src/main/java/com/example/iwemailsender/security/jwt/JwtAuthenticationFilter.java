package com.example.iwemailsender.security.jwt;

import com.example.iwemailsender.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("JWT Filter processing: {} {}", request.getMethod(), request.getRequestURI());


        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/auth/")) {
            log.debug("Skipping JWT filter for auth endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String authHeader = request.getHeader("Authorization");
            log.debug("Authorization header: {}", authHeader != null ? "Present (Bearer ...)" : "Missing");

            String jwt = null;
            String username = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                log.debug("JWT token extracted: {}...", jwt.substring(0, Math.min(jwt.length(), 20)));

                try {
                    username = jwtUtil.extractUsername(jwt);
                    log.debug("Username extracted from JWT: {}", username);
                } catch (Exception e) {
                    log.error("Failed to extract username from JWT: {}", e.getMessage());
                }
            } else {
                log.debug("No Bearer token found in Authorization header");
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("Loading user details for: {}", username);

                try {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    log.debug("User details loaded: {} with authorities: {}",
                            userDetails.getUsername(), userDetails.getAuthorities());

                    if (jwtUtil.isTokenValid(jwt, userDetails)) {
                        log.debug("JWT token is valid for user: {}", username);

                        // Create authentication token
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("SecurityContext updated with authentication for: {}", username);

                    } else {
                        log.warn("JWT token validation failed for user: {}", username);
                    }

                } catch (Exception e) {
                    log.error("Error during JWT authentication: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("JWT Filter error: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}