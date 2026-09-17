package com.securetask.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * User - JPA entity that maps to the 'users' table in MySQL.
 *
 * INTERVIEW NOTES:
 * - @Entity       : Tells JPA/Hibernate this class represents a DB table.
 * - @Table        : Specifies the exact table name.
 * - @Id           : Marks the primary key field.
 * - @GeneratedValue(IDENTITY) : Uses DB AUTO_INCREMENT for the ID.
 * - @Enumerated(STRING) : Stores enum as a readable string ("ROLE_USER") not an index number.
 * - @OneToMany    : One User → many Tasks. mappedBy="user" means the Task side owns the join column.
 * - @JsonIgnore   : Prevents this field from being serialized to JSON in API responses.
 * - implements UserDetails : Spring Security interface that provides user authentication info.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Email is the unique login identifier
    @Column(nullable = false, unique = true)
    private String email;

    // BCrypt-hashed password — never stored or returned as plaintext
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    // Role determines permissions: ROLE_ADMIN or ROLE_USER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // @JsonIgnore prevents circular reference: User → Task → User → (infinite loop)
    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Task> taskList;

    // =========================================================
    // Standard Java Getters and Setters
    // =========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @JsonIgnore
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public List<Task> getTaskList() { return taskList; }
    public void setTaskList(List<Task> taskList) { this.taskList = taskList; }

    // =========================================================
    // UserDetails interface methods — required by Spring Security
    // =========================================================

    /**
     * getAuthorities() - Returns the roles granted to this user.
     * Spring Security checks this to decide what the user can access.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * getUsername() - Spring Security uses this as the unique login identifier.
     * We use email as the username (not the 'name' field).
     */
    @Override
    public String getUsername() {
        return email;
    }

    // These are UserDetails contract methods — all return true for simplicity
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
