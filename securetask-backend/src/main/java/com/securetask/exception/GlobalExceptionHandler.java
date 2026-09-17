package com.securetask.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Centralized error handling for all controllers.
 *
 * INTERVIEW NOTES:
 * - @RestControllerAdvice : A Spring annotation that makes this class apply globally
 *   to all @RestController classes. Instead of handling exceptions in every controller,
 *   we handle them all in one place. This is the DRY (Don't Repeat Yourself) principle.
 *
 * - @ExceptionHandler(SomeException.class) : This method will be called automatically
 *   when the specified exception is thrown anywhere in the controller/service layer.
 *   Spring catches it and returns the structured error response we define here.
 *
 * - Without this class: if an unhandled exception occurs, Spring returns a generic
 *   500 error with a large HTML stack trace — not useful for a REST API.
 *   With this class: we return a clean JSON error response like:
 *   { "status": 404, "error": "Not Found", "message": "Task not found with ID: 5", "timestamp": "..." }
 *
 * Exceptions handled:
 * - IllegalArgumentException    → 400 Bad Request (e.g., duplicate email on registration)
 * - MethodArgumentNotValidException → 400 Bad Request (e.g., @Valid fails — missing required fields)
 * - BadCredentialsException     → 401 Unauthorized (wrong password on login)
 * - UsernameNotFoundException   → 401 Unauthorized (email not found on login)
 * - EntityNotFoundException     → 404 Not Found (task or user not found in DB)
 * - SecurityException           → 403 Forbidden (user tries to access another's task)
 * - Exception (catch-all)       → 500 Internal Server Error (unexpected errors)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Builds a standard error response map with status, error, message, and timestamp.
     * Reusable helper to keep all @ExceptionHandler methods clean.
     */
    private Map<String, Object> buildErrorResponse(int status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }

    /**
     * Handles IllegalArgumentException — e.g., registering with a duplicate email.
     * Returns: 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    /**
     * Handles @Valid validation failures — e.g., missing name, invalid email format.
     * Spring throws MethodArgumentNotValidException when any @NotBlank/@Email/@Size fails.
     * We extract all field-specific error messages and return them as a clean list.
     * Returns: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("errors", fieldErrors);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles wrong password or non-existent email during login.
     * Both BadCredentialsException and UsernameNotFoundException map to 401.
     * Note: We return a GENERIC message — never tell the user specifically
     * whether the email or password was wrong (information disclosure risk).
     * Returns: 401 Unauthorized
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationFailure(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorResponse(401, "Unauthorized", "Invalid email or password."));
    }

    /**
     * Handles task/user not found in the database.
     * Returns: 404 Not Found
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(404, "Not Found", ex.getMessage()));
    }

    /**
     * Handles ownership violations — a user trying to access/modify another user's task.
     * Returns: 403 Forbidden
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorResponse(403, "Forbidden", ex.getMessage()));
    }

    /**
     * Catch-all handler for any unexpected exception.
     * Logs the actual error on the server but returns a generic message to the client.
     * (Never expose internal stack traces or system details to the client.)
     * Returns: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // In production, log this with a proper logging framework like SLF4J/Logback
        System.err.println("Unexpected error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(500, "Internal Server Error",
                        "An unexpected error occurred. Please try again."));
    }
}
