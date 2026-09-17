package com.securetask.security;

import com.securetask.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig - Central Spring Security configuration class.
 *
 * INTERVIEW NOTES:
 * - @Configuration : Marks this as a configuration class — Spring creates beans defined here.
 * - @EnableWebSecurity : Activates Spring Security's web security support.
 *
 * - SecurityFilterChain : Defines the security rules:
 *     - Which routes are public (no token needed).
 *     - Which routes require authentication.
 *     - Which routes are restricted to ADMIN only.
 *
 * - SessionCreationPolicy.STATELESS : We do NOT use HTTP sessions.
 *   Every request must include a JWT — the server doesn't store any session state.
 *   This is what makes JWT-based auth "stateless."
 *
 * - CSRF disabled : CSRF (Cross-Site Request Forgery) protection is needed for
 *   session-based authentication. Since we use stateless JWT, there's no session
 *   to hijack, so CSRF is not needed and is safely disabled.
 *
 * - CORS : Cross-Origin Resource Sharing. Our React app runs on http://localhost:5173
 *   and our backend on http://localhost:8080 — these are different origins.
 *   Browsers block such cross-origin requests by default.
 *   Our CORS config explicitly allows requests from the React dev server.
 *
 * - DaoAuthenticationProvider : Tells Spring Security how to verify credentials:
 *     1. Load user by username (email) using our UserDetailsService.
 *     2. Check the provided password against the stored BCrypt hash.
 *
 * - BCryptPasswordEncoder : The @Bean for password hashing. BCrypt applies a one-way
 *   hash with a random salt — even if two users have the same password, their hashes differ.
 *   It is computationally expensive to brute-force, making it secure for passwords.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          UserRepository userRepository) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userRepository = userRepository;
    }

    /**
     * SecurityFilterChain - Defines the HTTP security rules for all endpoints.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT auth
            .csrf(AbstractHttpConfigurer::disable)

            // Configure CORS to allow React frontend requests
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Define authorization rules for each endpoint
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no token required
                .requestMatchers("/api/auth/**").permitAll()
                // Allow Spring's internal error forwarding endpoint (prevents silent 403s)
                .requestMatchers("/error").permitAll()

                // ADMIN-only endpoint
                .requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("ROLE_ADMIN")

                // All other endpoints require any authenticated user
                .anyRequest().authenticated()
            )

            // Use stateless sessions — Spring will not create or use HTTP sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our custom authentication provider (loads users + checks BCrypt passwords)
            .authenticationProvider(authenticationProvider())

            // Add our JWT filter BEFORE Spring's default username/password filter
            // This ensures JWT validation happens first on every request
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * UserDetailsService - Tells Spring Security how to load a user by their username (email).
     * loadUserByUsername() is called by the DaoAuthenticationProvider during authentication.
     * It is also called by our JwtAuthenticationFilter to look up the user from the database.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /**
     * DaoAuthenticationProvider - Connects the UserDetailsService + PasswordEncoder to Spring Security.
     * When a login request arrives, Spring calls this provider to:
     *   1. Load the user from the database.
     *   2. Compare the provided password with the stored BCrypt hash.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager - The central component that processes authentication requests.
     * We expose it as a @Bean so AuthService can call it to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCryptPasswordEncoder - The password hashing algorithm.
     * When registering: passwordEncoder.encode(rawPassword) → stored hash.
     * When logging in: passwordEncoder.matches(rawPassword, storedHash) → true/false.
     * BCrypt is a one-way function — the original password CANNOT be recovered from the hash.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS Configuration - Allows the React frontend to make API requests to this backend.
     * Without this, browsers will block cross-origin requests with a CORS error.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from the React development server
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // Allow these HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers (including Authorization for JWT)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow cookies/credentials to be sent (needed for some setups)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
