package com.securetask.dto;

import com.securetask.entity.Priority;
import com.securetask.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * TaskRequest - DTO for creating or updating a task.
 * Only accepts user-controlled fields: title, description, priority, status.
 * Fields 'createdDate' and 'userId' are set server-side, not by the client.
 */
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title cannot exceed 200 characters")
    private String title;

    private String description;

    // Optional — defaults to MEDIUM if not provided
    private Priority priority;

    // Optional — defaults to TODO if not provided
    private TaskStatus status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
