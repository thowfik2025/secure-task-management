package com.securetask.entity;

/**
 * TaskStatus - Enum representing the current state of a task.
 *
 * TODO        : Task has been created but work has not started yet.
 * IN_PROGRESS : Work on this task is currently ongoing.
 * COMPLETED   : Task has been finished.
 *
 * Stored as a String in the 'tasks' table (via @Enumerated(EnumType.STRING)).
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED
}
