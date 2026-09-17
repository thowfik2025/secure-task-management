package com.securetask.repository;

import com.securetask.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Provides all database operations for the User entity.
 *
 * INTERVIEW NOTES:
 * - JpaRepository<User, Long> : We extend JpaRepository, providing us with built-in methods:
 *     save(user)       → INSERT or UPDATE a user in the database
 *     findById(id)     → SELECT * FROM users WHERE id = ?  (returns Optional<User>)
 *     findAll()        → SELECT * FROM users
 *     delete(user)     → DELETE FROM users WHERE id = ?
 *     existsById(id)   → Returns true/false — useful for checking existence before operations
 *
 * - findByEmail(email) : This is a Spring Data JPA "derived query" — Spring automatically
 *     generates the SQL query "SELECT * FROM users WHERE email = ?" just from the method name.
 *     We use Optional<User> so the caller can safely handle the case where no user is found.
 *
 * - @Repository : A Spring annotation that marks this as a data access component.
 *     Not strictly required when extending JpaRepository, but makes the intent clear.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Used during login to load the user and verify their password.
     * Returns Optional.empty() if no user with that email exists.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if an email address is already registered.
     * Used during registration to prevent duplicate accounts.
     */
    boolean existsByEmail(String email);
}
