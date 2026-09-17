package com.securetask.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Task - JPA entity that maps to the 'tasks' table in MySQL.
 *
 * INTERVIEW NOTES:
 * - @ManyToOne    : Many Tasks belong to one User. This is the child/many side.
 * - @JoinColumn   : Creates 'user_id' as foreign key column in the 'tasks' table.
 * - @JsonIgnore   : Applied to 'user' field to prevent circular JSON serialization
 *                   (Task → User → taskList → Task → ... infinite loop).
 * - @Transient    : Tells JPA not to persist this as a column. getUserId() computes
 *                   the owner's ID from the user relationship for safe JSON serialization.
 * - @PrePersist   : JPA lifecycle hook — called automatically before INSERT.
 *                   We use it to set 'createdDate' to the current timestamp.
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Stored as string "LOW"/"MEDIUM"/"HIGH" in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    // Stored as string "TODO"/"IN_PROGRESS"/"COMPLETED" in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    // Auto-set on first save via @PrePersist. updatable=false prevents it from changing.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // @JsonIgnore: prevents circular serialization Task→User→taskList→Task→...
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // =========================================================
    // Standard Getters and Setters
    // =========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    @JsonIgnore
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    /**
     * getUserId() - @Transient means JPA does NOT create a column for this method.
     * It computes the userId from the user relationship so the frontend can see
     * which user owns a task in the JSON response, without exposing the full User object.
     */
    @Transient
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * @PrePersist - Called automatically by JPA right before the task is INSERT-ed.
     * Sets createdDate to now, so every task always has a creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
