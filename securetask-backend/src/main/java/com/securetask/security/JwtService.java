package com.securetask.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtService - Handles JWT token generation and validation.
 *
 * INTERVIEW NOTES:
 * - JWT (JSON Web Token) has three parts: Header.Payload.Signature
 *   Header: {"alg": "HS256", "typ": "JWT"}
 *   Payload: {"sub": "user@email.com", "iat": 1234567890, "exp": 1234654290}
 *   Signature: HMACSHA256(base64(header) + "." + base64(payload), secretKey)
 *
 * - Why JWT?
 *   JWT is stateless — the server does NOT store sessions. The token itself
 *   contains all needed info. Every request includes the JWT, and we verify it
 *   using our secret key. If valid → we trust the request. This scales well.
 *
 * - @Value("${jwt.secret}") : Injects the jwt.secret property from application.properties.
 *   This keeps the secret out of the source code.
 *
 * - HS256 (HMAC-SHA256) : A symmetric algorithm — same key is used to sign and verify.
 *   The key must stay private on the server. If compromised, tokens can be forged.
 */
@Service
public class JwtService {

    // Secret key injected from application.properties — never hardcoded here
    @Value("${jwt.secret}")
    private String secretKey;

    // Token validity period injected from application.properties (in milliseconds)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * generateToken(userDetails) - Creates a signed JWT token for an authenticated user.
     *
     * @param userDetails The authenticated user (our User entity implements this)
     * @return A signed JWT string like "eyJhbGci...header.payload.signature"
     *
     * The token contains:
     *   - subject: the user's email (used to identify the user on later requests)
     *   - issuedAt: when the token was created
     *   - expiration: when the token expires
     * The token is signed with our secret key — any tampering will invalidate the signature.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        return buildToken(extraClaims, userDetails);
    }

    /**
     * buildToken() - Constructs and signs the JWT using the JJWT library.
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())           // sets "sub" claim = email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())                    // signs with our secret key
                .compact();                                   // produces the final JWT string
    }

    /**
     * extractUsername(token) - Reads the "sub" (subject) claim from the JWT.
     * We use the email as the subject, so this returns the user's email.
     * Called in the JWT filter to identify which user made the request.
     *
     * @param token The JWT string from the Authorization header
     * @return The user's email address
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * isTokenValid(token, userDetails) - Checks two things:
     *   1. Does the username in the token match our user?
     *   2. Has the token expired?
     * Both must be true for the token to be considered valid.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * isTokenExpired() - Checks if the token's expiration date is in the past.
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractAllClaims(token).getExpiration();
        return expirationDate.before(new Date());
    }

    /**
     * extractAllClaims() - Parses and verifies the JWT, then returns all claims.
     * If the token is tampered with or the signature does not match, JJWT throws
     * a JwtException here — which our exception handler converts to a 401 response.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // verifies the signature
                .build()
                .parseSignedClaims(token)      // parses and validates the token
                .getPayload();                 // returns the claims (payload)
    }

    /**
     * getSigningKey() - Converts our string secret into a cryptographic SecretKey.
     * JJWT requires a SecretKey object for signing/verifying HS256 tokens.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
