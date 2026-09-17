package com.securetask.controller;

import com.securetask.dto.LoginRequest;
import com.securetask.dto.RegisterRequest;
import com.securetask.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController - Handles HTTP requests for user authentication.
 *
 * INTERVIEW NOTES:
 * - @RestController : Combines @Controller + @ResponseBody.
 *   Every method in this class automatically serializes its return value to JSON.
 *
 * - @RequestMapping("/api/auth") : All routes in this controller start with /api/auth.
 *   So: /api/auth/register and /api/auth/login.
 *   These are PUBLIC endpoints — no JWT required (configured in SecurityConfig).
 *
 * - @PostMapping : Handles HTTP POST requests. POST is the correct method for
 *   "creating something" (a user account) or "submitting credentials."
 *
 * - @RequestBody : Tells Spring to read the HTTP request body as JSON and
 *   convert it into a Java object (RegisterRequest or LoginRequest).
 *
 * - @Valid : Triggers bean validation on the DTO fields annotated with
 *   @NotBlank, @Email, @Size etc. If validation fails, Spring returns 400 automatically.
 *
 * - ResponseEntity<> : Lets us control the HTTP status code precisely.
 *   HttpStatus.CREATED (201) = resource was created successfully.
 *   HttpStatus.OK (200) = request succeeded, response body contains data.
 *
 * - The controller does NOT contain business logic — it delegates to AuthService.
 *   This is the clean Controller → Service pattern.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * Registers a new user account.
     *
     * @param request Validated RegisterRequest from the request body (name, email, password)
     * @return 201 Created with a success message
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", message));
    }

    /**
     * POST /api/auth/login
     * Authenticates a user and returns a JWT token.
     *
     * @param request Validated LoginRequest from the request body (email, password)
     * @return 200 OK with: token, userId, name, email, role
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
