package com.example.iwemailsender.email.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.net.ConnectException;
import java.util.Map;


@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/test/exceptions")
public class ExceptionTestController {

    @GetMapping("/generic")
    public ResponseEntity<String> testGenericException() {
        throw new RuntimeException("This is a test generic exception to generate UUID error");
    }

    @GetMapping("/security")
    public ResponseEntity<String> testSecurityException() {
        throw new AccessDeniedException("Access denied for testing redirect functionality");
    }

    @GetMapping("/auth")
    public ResponseEntity<String> testAuthException() {
        throw new AuthenticationException("Authentication failed for testing") {};
    }

    @GetMapping("/database")
    public ResponseEntity<String> testDatabaseException() {
        throw new DataAccessResourceFailureException("Database connection failed for testing");
    }

    @GetMapping("/external-service")
    public ResponseEntity<String> testExternalServiceException() throws ConnectException {
        throw new ConnectException("External service unavailable for testing");
    }

    @GetMapping("/optimistic-lock")
    public ResponseEntity<String> testOptimisticLockException() {
        throw new OptimisticLockException("Record modified by another user for testing");
    }

    @GetMapping("/not-found")
    public ResponseEntity<String> testEntityNotFoundException() {
        throw new EntityNotFoundException("Test entity not found");
    }

    @PostMapping("/validation")
    public ResponseEntity<String> testValidationException(@RequestBody Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        return ResponseEntity.ok("Validation passed");
    }

    @GetMapping("/business-logic")
    public ResponseEntity<String> testBusinessLogicException() {
        throw new IllegalStateException("Invalid business state for testing");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTestStatus() {
        Map<String, Object> status = Map.of(
                "message", "Exception Test Controller is running",
                "availableTests", new String[]{
                        "GET /api/test/exceptions/generic - Test generic exception with UUID",
                        "GET /api/test/exceptions/security - Test security exception with redirect",
                        "GET /api/test/exceptions/auth - Test authentication exception",
                        "GET /api/test/exceptions/database - Test database exception",
                        "GET /api/test/exceptions/external-service - Test external service exception",
                        "GET /api/test/exceptions/optimistic-lock - Test optimistic lock exception",
                        "GET /api/test/exceptions/not-found - Test entity not found exception",
                        "POST /api/test/exceptions/validation - Test validation exception",
                        "GET /api/test/exceptions/business-logic - Test business logic exception"
                }
        );

        return ResponseEntity.ok(status);
    }
}
