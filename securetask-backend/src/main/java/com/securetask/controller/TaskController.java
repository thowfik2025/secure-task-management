package com.securetask.controller;

import com.securetask.dto.TaskRequest;
import com.securetask.entity.Task;
import com.securetask.entity.User;
import com.securetask.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TaskController - Handles all HTTP requests for task management.
 *
 * INTERVIEW NOTES:
 * - All endpoints here require authentication (JWT token in Authorization header).
 *   This is enforced by SecurityConfig's .anyRequest().authenticated() rule.
 *
 * - @AuthenticationPrincipal User currentUser : Spring Security injects the
 *   authenticated user from the SecurityContextHolder directly as a parameter.
 *   This is how we know WHICH user is making the request — we can then
 *   pass them to the service to enforce ownership rules.
 *
 * - @PathVariable Long id : Extracts the {id} segment from the URL.
 *   Example: GET /api/tasks/5 → id = 5
 *
 * - ResponseEntity.noContent() : Returns HTTP 204 No Content — the correct
 *   response for a successful DELETE that has nothing to return.
 *
 * - Why pass currentUser to the service?
 *   The service uses the user's role and ID to:
 *     1. Filter tasks (ADMIN sees all, USER sees own).
 *     2. Verify ownership before update/delete.
 *   This keeps authorization logic in the service (not scattered everywhere).
 *
 * --- API endpoints exposed by this controller ---
 * GET    /api/tasks          → get all tasks (role-aware)
 * GET    /api/tasks/{id}     → get one task by ID
 * POST   /api/tasks          → create a new task
 * PUT    /api/tasks/{id}     → update an existing task
 * DELETE /api/tasks/{id}     → delete a task
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * GET /api/tasks
     * Returns tasks based on the authenticated user's role:
     *   ADMIN → all tasks | USER → only their own tasks
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(@AuthenticationPrincipal User currentUser) {
        List<Task> tasks = taskService.getAllTasks(currentUser);
        return ResponseEntity.ok(tasks);
    }

    /**
     * GET /api/tasks/{id}
     * Returns a single task by ID. Access is role/ownership-controlled in the service.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        Task task = taskService.getTaskById(id, currentUser);
        return ResponseEntity.ok(task);
    }

    /**
     * POST /api/tasks
     * Creates a new task and assigns it to the current user.
     * Returns 201 Created with the saved task object.
     */
    @PostMapping
    public ResponseEntity<Task> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User currentUser) {
        Task created = taskService.createTask(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/tasks/{id}
     * Updates an existing task. Only the owner (or ADMIN) can update.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User currentUser) {
        Task updated = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/tasks/{id}
     * Deletes a task by ID. Only the owner (or ADMIN) can delete.
     * Returns 204 No Content (nothing to return after deletion).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
