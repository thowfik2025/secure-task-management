package com.securetask.service;

import com.securetask.dto.LoginRequest;
import com.securetask.dto.RegisterRequest;
import com.securetask.entity.User;
import com.securetask.entity.UserRole;
import com.securetask.repository.UserRepository;
import com.securetask.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthService - Handles user registration and login business logic.
 *
 * INTERVIEW NOTES:
 * - @Service : Marks this as a Spring service bean — it holds business logic.
 *   Spring automatically detects and creates an instance of this class.
 *
 * - Why separate from the controller?
 *   The controller handles HTTP — what comes in and goes out.
 *   The service handles the actual logic — what happens in between.
 *   This is the "Separation of Concerns" principle (part of SOLID).
 *
 * - PasswordEncoder : We never store raw passwords. During registration,
 *   encoder.encode(rawPassword) produces a BCrypt hash.
 *   During login, AuthenticationManager internally calls encoder.matches()
 *   to compare the raw input against the stored hash.
 *
 * - AuthenticationManager : The central Spring Security component that
 *   processes an authentication attempt. It calls our DaoAuthenticationProvider,
 *   which loads the user by email and checks the password with BCrypt.
 *   If credentials are wrong, it throws BadCredentialsException (→ 401 response).
 *
 * Registration flow:
 *   1. Check if email is already taken → throw if so
 *   2. Create User object with BCrypt-hashed password and ROLE_USER
 *   3. Save to database
 *   4. Return success message
 *
 * Login flow:
 *   1. Use AuthenticationManager to verify credentials
 *   2. If valid, load the User from the database
 *   3. Generate a JWT with JwtService
 *   4. Return the JWT token + user info to the controller
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * register() - Creates a new user account.
     *
     * @param request contains name, email, password from the registration form
     * @return A success message string
     * @throws IllegalArgumentException if email is already registered
     */
    public String register(RegisterRequest request) {
        // Check if a user with this email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // Create a new User object
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        // SECURITY: Never store the raw password — always hash it with BCrypt
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        // All self-registered users get ROLE_USER — admins are created manually or seeded
        newUser.setRole(UserRole.ROLE_USER);

        // Save to the database — Hibernate generates: INSERT INTO users (name, email, password, role) VALUES (...)
        userRepository.save(newUser);

        return "Registration successful. Please login.";
    }

    /**
     * login() - Authenticates a user and returns a JWT token.
     *
     * @param request contains email and password from the login form
     * @return A Map containing the JWT token and user details (for the frontend)
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are wrong
     */
    public Map<String, Object> login(LoginRequest request) {
        // Attempt authentication — this internally:
        //   1. Calls userDetailsService.loadUserByUsername(email) → loads user from DB
        //   2. Calls passwordEncoder.matches(rawPassword, hashedPassword) → verifies password
        //   3. Throws BadCredentialsException if either step fails
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // If we reach here, authentication succeeded — now load the full user object
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Generate a signed JWT token for this user
        String token = jwtService.generateToken(user);

        // Return the token and user details in a structured response map
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());

        return response;
    }
}
