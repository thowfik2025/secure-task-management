package com.securetask.entity;

/**
 * Priority - Enum representing the urgency level of a task.
 *
 * LOW    : Not urgent, can be done later.
 * MEDIUM : Moderate urgency.
 * HIGH   : Urgent — needs immediate attention.
 *
 * Stored as a String in the 'tasks' table (via @Enumerated(EnumType.STRING)).
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH
}
