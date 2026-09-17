package com.securetask.service;

import com.securetask.entity.User;
import com.securetask.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserService - Handles user-related operations for the ADMIN panel.
 *
 * INTERVIEW NOTES:
 * - This service is intentionally simple — currently it only provides getAllUsers()
 *   for the ADMIN dashboard. The endpoint itself is already protected at the
 *   SecurityConfig level (hasAuthority("ROLE_ADMIN")), so no role check is needed here.
 *
 * - Note: We return the List<User> objects directly. In a production system, you might
 *   create a UserResponseDto to explicitly exclude the password field from the response.
 *   However, Spring's @JsonProperty(access = Access.WRITE_ONLY) on the password field
 *   or Jackson serialization exclusions handle this safely.
 *   For this project, we handle it at the Jackson level via @JsonIgnore in the entity.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * getAllUsers() - Returns all registered users.
     * Only called for ADMIN — the security layer ensures normal users never reach this.
     *
     * @return List of all User objects (passwords are excluded via @JsonIgnore on the entity)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
