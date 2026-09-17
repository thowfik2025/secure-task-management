/**
 * TaskCard.jsx - Displays a single task in a card format.
 *
 * INTERVIEW NOTES:
 * - This is a "presentational" or "dumb" component — it only receives data via
 *   props and renders it. It does NOT fetch data or manage state itself.
 * - Props: task (the task object), onEdit (callback for edit button), onDelete (callback for delete button).
 * - We use callback functions (onEdit, onDelete) passed from the parent (Tasks.jsx).
 *   This follows React's "lifting state up" pattern — the Tasks page manages the
 *   list of tasks and passes down the handler functions.
 */

// Helper: returns the CSS class name for a status badge
function getStatusBadgeClass(status) {
  switch (status) {
    case 'TODO':        return 'badge badge-todo';
    case 'IN_PROGRESS': return 'badge badge-inprogress';
    case 'COMPLETED':   return 'badge badge-completed';
    default:            return 'badge badge-todo';
  }
}

// Helper: returns the CSS class name for a priority badge
function getPriorityBadgeClass(priority) {
  switch (priority) {
    case 'LOW':    return 'badge badge-low';
    case 'MEDIUM': return 'badge badge-medium';
    case 'HIGH':   return 'badge badge-high';
    default:       return 'badge badge-medium';
  }
}

// Format a LocalDateTime string from Java into a readable date
function formatDate(dateStr) {
  if (!dateStr) return 'Unknown';
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
  });
}

function TaskCard({ task, onEdit, onDelete, currentUser }) {
  // Task owner check — user can only edit/delete their own tasks
  // ADMIN can edit/delete any task
  const isOwnerOrAdmin = currentUser?.role === 'ROLE_ADMIN' ||
                         task.userId === currentUser?.userId;

  return (
    <div className="card" style={{ marginBottom: '1rem' }}>
      {/* Card Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
        <h3 style={{ fontSize: '0.95rem', fontWeight: '600', flex: 1, marginRight: '1rem' }}>
          {task.title}
        </h3>
        <div style={{ display: 'flex', gap: '0.5rem', flexShrink: 0 }}>
          <span className={getStatusBadgeClass(task.status)}>
            {task.status?.replace('_', ' ')}
          </span>
          <span className={getPriorityBadgeClass(task.priority)}>
            {task.priority}
          </span>
        </div>
      </div>

      {/* Description */}
      {task.description && (
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '0.75rem', lineHeight: '1.5' }}>
          {task.description}
        </p>
      )}

      {/* Footer: date + action buttons */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.75rem' }}>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
          Created: {formatDate(task.createdDate)}
        </span>

        {/* Only show edit/delete if user owns task or is admin */}
        {isOwnerOrAdmin && (
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button className="btn-ghost" onClick={() => onEdit(task)} style={{ fontSize: '0.78rem', padding: '0.3rem 0.7rem' }}>
              Edit
            </button>
            <button className="btn-danger" onClick={() => onDelete(task.id)} style={{ fontSize: '0.78rem', padding: '0.3rem 0.7rem' }}>
              Delete
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default TaskCard;
