package com.securetask.entity;

/**
 * UserRole - Enum representing the two roles in the system.
 *
 * ROLE_ADMIN : Has full access — can view all users, all tasks, create/update/delete anything.
 * ROLE_USER  : Has limited access — can only manage their own tasks.
 *
 * This enum is stored in the 'role' column of the 'users' table as a String.
 * Spring Security uses these role names directly (e.g., hasRole("ADMIN") checks for "ROLE_ADMIN").
 */
public enum UserRole {
    ROLE_ADMIN,
    ROLE_USER
}
