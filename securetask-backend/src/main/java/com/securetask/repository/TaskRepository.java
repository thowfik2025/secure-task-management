package com.securetask.repository;

import com.securetask.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TaskRepository - Provides all database operations for the Task entity.
 *
 * INTERVIEW NOTES:
 * - JpaRepository<Task, Long> : Provides built-in methods for Task CRUD:
 *     save(task)       → INSERT or UPDATE a task
 *     findById(id)     → SELECT * FROM tasks WHERE id = ?
 *     findAll()        → SELECT * FROM tasks (all tasks across all users)
 *     deleteById(id)   → DELETE FROM tasks WHERE id = ?
 *
 * - findByUserId(userId) : Custom JPQL query — fetches tasks by user.id:
 *     SELECT * FROM tasks WHERE user_id = ?
 *     This is how we get only the tasks belonging to one specific user (regular USER role).
 *
 * The ADMIN uses findAll() to see every task.
 * A regular USER uses findByUserId(their own id) to see only their own tasks.
 * This is the core of our task-level access control.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds all tasks owned by a specific user using JPQL.
     * Maps to "WHERE user_id = ?" in SQL.
     * This is how a regular user sees only their own tasks.
     */
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId")
    List<Task> findByUserId(@Param("userId") Long userId);
}
