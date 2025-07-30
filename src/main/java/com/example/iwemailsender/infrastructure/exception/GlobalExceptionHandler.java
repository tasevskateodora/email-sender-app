package com.example.iwemailsender.infrastructure.exception;

import com.example.iwemailsender.email.domain.Role;
import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.dto.SchedulerErrorDto;
import com.example.iwemailsender.email.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final UserRepository userRepository;

    public GlobalExceptionHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SchedulerErrorDto> handleGenericException(Exception ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();

        log.error("Error ID: {} - Unexpected error in {}: {}", errorId, request.getDescription(false), ex.getMessage(), ex);

        SchedulerErrorDto error = new SchedulerErrorDto(
                "Please contact the administrator with the following error identifier: " + errorId,
                errorId,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public ResponseEntity<SchedulerErrorDto> handleSecurityException(Exception ex) {
        log.error("Security error: {}", ex.getMessage());

        String redirectUrl = determineDefaultLandingPage();

        SchedulerErrorDto error = new SchedulerErrorDto(
                "Access denied. Redirecting to default page.",
                null,
                HttpStatus.FORBIDDEN.value()
        );
        error.setRedirectUrl(redirectUrl);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<SchedulerErrorDto> handleOptimisticLocking(OptimisticLockException ex) {
        String errorId = UUID.randomUUID().toString();

        log.error("Error ID: {} - Optimistic locking conflict: {}", errorId, ex.getMessage());

        SchedulerErrorDto error = new SchedulerErrorDto(
                "Record was modified by another user. Please contact administrator with error ID: " + errorId,
                errorId,
                HttpStatus.CONFLICT.value()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ResponseEntity<SchedulerErrorDto> handleDatabaseException(Exception ex) {
        String errorId = UUID.randomUUID().toString();

        log.error("Error ID: {} - Database error: {}", errorId, ex.getMessage(), ex);

        SchedulerErrorDto error = new SchedulerErrorDto(
                "Database temporarily unavailable. Please contact administrator with error ID: " + errorId,
                errorId,
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler({ConnectException.class, SocketTimeoutException.class})
    public ResponseEntity<SchedulerErrorDto> handleExternalServiceException(Exception ex) {
        String errorId = UUID.randomUUID().toString();

        log.error("Error ID: {} - External service error: {}", errorId, ex.getMessage());

        SchedulerErrorDto error = new SchedulerErrorDto(
                "External service temporarily unavailable. Please contact administrator with error ID: " + errorId,
                errorId,
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<SchedulerErrorDto> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        SchedulerErrorDto error = new SchedulerErrorDto(
                ex.getMessage(),
                null,
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SchedulerErrorDto> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse("Validation failed");

        SchedulerErrorDto error = new SchedulerErrorDto(
                "Validation failed: " + validationErrors,
                null,
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<SchedulerErrorDto> handleBusinessLogicException(RuntimeException ex) {
        log.warn("Business logic error: {}", ex.getMessage());

        SchedulerErrorDto error = new SchedulerErrorDto(
                ex.getMessage(),
                null,
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private String determineDefaultLandingPage() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getName() != null) {
                String username = auth.getName();

                Optional<User> userOpt = userRepository.findAll().stream()
                        .filter(user -> username.equals(user.getUsername()))
                        .findFirst();

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    for (Role role : user.getRoles()) {
                        String roleName = role.getName();

                        if (roleName != null) {
                            switch (roleName.toUpperCase()) {
                                case "ADMIN":
                                case "ADMINISTRATOR":
                                    return "/admin/dashboard";
                                case "HR":
                                case "HR_MANAGER":
                                case "HUMAN_RESOURCES":
                                    return "/hr/email-jobs";
                                case "USER":
                                case "EMPLOYEE":
                                case "STAFF":
                                    return "/user/profile";
                                default:
                                    log.debug("Unknown role: {}", roleName);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error determining landing page: {}", e.getMessage());
        }

        return "/login";
    }
}


