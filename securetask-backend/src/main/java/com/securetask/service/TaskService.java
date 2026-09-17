package com.securetask.service;

import com.securetask.dto.TaskRequest;
import com.securetask.entity.Task;
import com.securetask.entity.User;
import com.securetask.entity.UserRole;
import com.securetask.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TaskService - Handles all task management business logic.
 *
 * INTERVIEW NOTES:
 * - This service enforces two important business rules:
 *   1. ADMIN can see/modify ALL tasks.
 *      A regular USER can only see/modify THEIR OWN tasks.
 *   2. We verify task ownership before allowing update/delete
 *      (a USER cannot update or delete someone else's task, even with a valid JWT).
 *
 * - The authenticated 'currentUser' is passed in from the controller.
 *   The controller extracts it from the SecurityContextHolder,
 *   so the service never needs to touch HTTP or security code directly.
 *   This keeps the service layer clean and easy to test.
 *
 * - EntityNotFoundException : Thrown when a task ID doesn't exist.
 *   Caught by GlobalExceptionHandler and converted to a 404 HTTP response.
 *
 * - SecurityException : Thrown when a USER tries to access a task they don't own.
 *   Caught by GlobalExceptionHandler and converted to a 403 HTTP response.
 */
/**
 * @Transactional at the class level means every method in this service runs
 * inside a JPA transaction. This is important because:
 * 1. It allows lazy-loaded relationships (like task.getUser()) to be accessed safely.
 * 2. It ensures that if an error occurs mid-operation, the database is not left in a broken state.
 * Methods that only read data use @Transactional(readOnly = true) for better performance.
 */
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * getAllTasks() - Returns tasks based on the user's role.
     * ADMIN → all tasks in the system.
     * USER → only tasks belonging to that user.
     *
     * @param currentUser The authenticated user from the security context
     * @return List of Task objects
     */
    public List<Task> getAllTasks(User currentUser) {
        if (currentUser.getRole() == UserRole.ROLE_ADMIN) {
            // ADMIN sees everything: SELECT * FROM tasks
            return taskRepository.findAll();
        } else {
            // Regular user sees only their own: SELECT * FROM tasks WHERE user_id = ?
            return taskRepository.findByUserId(currentUser.getId());
        }
    }

    /**
     * getTaskById() - Returns a single task by its ID.
     * Users can only retrieve their own tasks; ADMIN can retrieve any task.
     *
     * @param taskId The ID of the task to retrieve
     * @param currentUser The authenticated user making the request
     * @return The Task object if found and accessible
     * @throws EntityNotFoundException if the task doesn't exist
     * @throws SecurityException if a USER tries to access another user's task
     */
    public Task getTaskById(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));

        // If not ADMIN, ensure the task belongs to the requesting user
        if (currentUser.getRole() != UserRole.ROLE_ADMIN &&
                !task.getUserId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to access this task.");
        }

        return task;
    }

    /**
     * createTask() - Creates a new task and assigns it to the current user.
     *
     * @param request TaskRequest DTO containing title, description, priority, status
     * @param currentUser The authenticated user who is creating the task
     * @return The saved Task with an auto-generated ID and createdDate
     */
    public Task createTask(TaskRequest request, User currentUser) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        // Use provided priority/status or keep entity defaults (MEDIUM / TODO)
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        // Assign task to the authenticated user
        // This creates the foreign key relationship: tasks.user_id = users.id
        task.setUser(currentUser);

        // Save to database — @PrePersist will auto-set createdDate
        return taskRepository.save(task);
    }

    /**
     * updateTask() - Updates an existing task's details.
     * Only the task owner (or ADMIN) can update a task.
     *
     * @param taskId The ID of the task to update
     * @param request The new task data from the request body
     * @param currentUser The authenticated user making the update request
     * @return The updated and saved Task
     * @throws EntityNotFoundException if the task doesn't exist
     * @throws SecurityException if a USER tries to update another user's task
     */
    public Task updateTask(Long taskId, TaskRequest request, User currentUser) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));

        // Only the task owner or an ADMIN can update the task
        if (currentUser.getRole() != UserRole.ROLE_ADMIN &&
                !existingTask.getUserId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to update this task.");
        }

        // Update only the fields that were provided in the request
        existingTask.setTitle(request.getTitle());
        existingTask.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            existingTask.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            existingTask.setStatus(request.getStatus());
        }

        return taskRepository.save(existingTask);
    }

    /**
     * deleteTask() - Deletes a task by its ID.
     * Only the task owner (or ADMIN) can delete a task.
     *
     * @param taskId The ID of the task to delete
     * @param currentUser The authenticated user requesting the deletion
     * @throws EntityNotFoundException if the task doesn't exist
     * @throws SecurityException if a USER tries to delete another user's task
     */
    public void deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));

        // Ownership check before deletion — use getUserId() to avoid lazy-loading the full User
        if (currentUser.getRole() != UserRole.ROLE_ADMIN &&
                !task.getUserId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to delete this task.");
        }

        taskRepository.deleteById(taskId);
    }
}
