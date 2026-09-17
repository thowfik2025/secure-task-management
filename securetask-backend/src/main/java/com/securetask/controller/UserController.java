package com.securetask.controller;

import com.securetask.entity.User;
import com.securetask.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UserController - Handles user-related HTTP endpoints.
 *
 * INTERVIEW NOTES:
 * - GET /api/users is restricted to ROLE_ADMIN only — this is enforced in SecurityConfig
 *   (.requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("ROLE_ADMIN")).
 *   If a regular USER calls this endpoint, Spring Security returns 403 Forbidden
 *   before the controller method even runs.
 *
 * - @AuthenticationPrincipal : A convenient Spring annotation that injects the
 *   currently authenticated user (from SecurityContextHolder) as a method parameter.
 *   Our User entity implements UserDetails, so Spring knows exactly what to inject here.
 *   This gives us direct access to the logged-in user's data without any DB call.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users
     * Returns a list of all registered users. ADMIN only.
     * Passwords are excluded from the response via @JsonIgnore on the User entity.
     *
     * @return 200 OK with a list of User objects
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
