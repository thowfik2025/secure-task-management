package com.securetask.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Intercepts every incoming HTTP request and validates the JWT.
 *
 * INTERVIEW NOTES:
 * - OncePerRequestFilter : A Spring class that guarantees this filter runs exactly once
 *   per request — even if the request is forwarded internally multiple times.
 *
 * - How it works (step by step):
 *   1. Read the 'Authorization' header from the request.
 *   2. If the header is missing or doesn't start with "Bearer ", skip this filter
 *      (public routes like /api/auth/** will have no token).
 *   3. Extract the JWT string (everything after "Bearer ").
 *   4. Extract the username (email) from the JWT using JwtService.
 *   5. Load the full UserDetails from the database using that email.
 *   6. Validate the token — check signature and expiry.
 *   7. If valid, create an Authentication object and put it into SecurityContextHolder.
 *      This tells Spring Security: "This request is authenticated as this user."
 *   8. Call filterChain.doFilter() to pass the request to the next filter/controller.
 *
 * - SecurityContextHolder : Spring Security's "memory" for the current request's
 *   authentication state. Setting authentication here is what makes the request "logged in."
 *
 * - Why do we load from DB even if the token is valid?
 *   To ensure the user still exists and is still active (not deleted/banned after token issuance).
 *
 * - @Lazy UserDetailsService: Resolves circular dependency between SecurityConfig and JwtAuthenticationFilter.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Constructor injection with @Lazy to break circular bean initialization with SecurityConfig
    public JwtAuthenticationFilter(JwtService jwtService, @Lazy UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Check for the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2: If no Authorization header, or it doesn't start with "Bearer ",
        //         skip JWT processing and let Spring Security handle it (will likely block protected routes)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract token — "Bearer eyJhbGci..." → "eyJhbGci..."
        String jwt = authHeader.substring(7);

        // Step 4: Extract username (email) from JWT subject claim
        String userEmail = jwtService.extractUsername(jwt);

        // Step 5: Only proceed if we got a username AND the user isn't already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 5a: Load user details from the database using the email from the token
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Step 6: Validate the token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Step 7: Create an authentication token — this is what Spring Security recognizes
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,                           // credentials null — we already verified via JWT
                        userDetails.getAuthorities()    // roles (ROLE_ADMIN or ROLE_USER)
                );

                // Attach request metadata (IP address, session info) to the authentication
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 7a: Set the authentication in the security context — marks request as authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 8: Pass the request to the next filter/controller in the chain
        filterChain.doFilter(request, response);
    }
}
