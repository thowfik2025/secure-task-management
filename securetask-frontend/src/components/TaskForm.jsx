import { useState, useEffect } from 'react';

/**
 * TaskForm.jsx - Modal form for creating or updating a task.
 *
 * INTERVIEW NOTES:
 * - This component handles BOTH create and edit modes.
 *   If 'existingTask' prop is provided → edit mode (pre-fills the form).
 *   If 'existingTask' is null → create mode (empty form).
 * - Modal pattern: A full-screen overlay with the form in the center.
 * - useEffect: When existingTask changes, we populate the form fields.
 * - onSubmit(formData): A callback function passed from Tasks.jsx.
 *   The parent submits the actual API call — this component only handles the form UI.
 * - onClose(): Another callback to close the modal (set in Tasks.jsx state).
 */
function TaskForm({ existingTask, onSubmit, onClose, loading }) {
  const [title, setTitle]             = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority]       = useState('MEDIUM');
  const [status, setStatus]           = useState('TODO');

  // When editing, pre-fill the form with the existing task data
  useEffect(() => {
    if (existingTask) {
      setTitle(existingTask.title || '');
      setDescription(existingTask.description || '');
      setPriority(existingTask.priority || 'MEDIUM');
      setStatus(existingTask.status || 'TODO');
    } else {
      // Reset form for new task creation
      setTitle('');
      setDescription('');
      setPriority('MEDIUM');
      setStatus('TODO');
    }
  }, [existingTask]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ title, description, priority, status });
  };

  const isEditMode = Boolean(existingTask);

  return (
    // Modal overlay — clicking OUTSIDE the box closes the form
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-box"
        onClick={(e) => e.stopPropagation()} // Prevent click from bubbling to overlay
      >
        {/* Modal Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ fontWeight: '700', fontSize: '1.1rem' }}>
            {isEditMode ? 'Edit Task' : 'Create New Task'}
          </h2>
          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', fontSize: '1.25rem', cursor: 'pointer' }}
          >
            ✕
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit}>
          {/* Title */}
          <div className="form-group">
            <label className="label" htmlFor="task-title">Title *</label>
            <input
              id="task-title"
              type="text"
              className="input"
              placeholder="What needs to be done?"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              maxLength={200}
            />
          </div>

          {/* Description */}
          <div className="form-group">
            <label className="label" htmlFor="task-desc">Description (optional)</label>
            <textarea
              id="task-desc"
              className="input"
              placeholder="Add more details..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              style={{ resize: 'vertical' }}
            />
          </div>

          {/* Priority + Status: side by side */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="label" htmlFor="task-priority">Priority</label>
              <select
                id="task-priority"
                className="input"
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </div>

            <div className="form-group">
              <label className="label" htmlFor="task-status">Status</label>
              <select
                id="task-status"
                className="input"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="COMPLETED">Completed</option>
              </select>
            </div>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
            <button type="button" className="btn-ghost" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Saving...' : (isEditMode ? 'Update Task' : 'Create Task')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default TaskForm;
