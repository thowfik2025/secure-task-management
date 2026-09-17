import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import TaskCard from '../components/TaskCard';
import TaskForm from '../components/TaskForm';

/**
 * Tasks.jsx - Main task management page.
 *
 * INTERVIEW NOTES — "What happens when a user creates a task?":
 * 1. User clicks "New Task" → setShowForm(true) → TaskForm modal opens.
 * 2. User fills title, description, priority, status → clicks "Create Task".
 * 3. handleCreateTask(formData) is called.
 * 4. api.post('/tasks', formData) → Axios adds Authorization: Bearer <token> from localStorage.
 * 5. Spring Boot receives request → JwtAuthenticationFilter validates JWT.
 * 6. TaskController.createTask() is called → passes to TaskService.createTask().
 * 7. TaskService assigns task to currentUser, saves to DB.
 * 8. Response: { id, title, description, priority, status, createdDate, userId }
 * 9. We add the new task to the tasks state → TaskCard renders it.
 *
 * State management:
 * - tasks: Array of task objects fetched from the backend.
 * - showForm: Boolean controlling TaskForm modal visibility.
 * - editingTask: The task being edited (null for create mode).
 * - statusFilter: Filter tasks by status ('' = show all).
 */
function Tasks() {
  const { user } = useAuth();
  const [tasks, setTasks]           = useState([]);
  const [loading, setLoading]       = useState(true);
  const [formLoading, setFormLoading] = useState(false);
  const [showForm, setShowForm]     = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [statusFilter, setStatusFilter] = useState('');
  const [error, setError]           = useState('');
  const [success, setSuccess]       = useState('');

  // Fetch tasks on component mount
  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      const response = await api.get('/tasks');
      setTasks(response.data);
    } catch (err) {
      setError('Failed to load tasks. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Filter tasks by selected status
  const filteredTasks = statusFilter
    ? tasks.filter(t => t.status === statusFilter)
    : tasks;

  // ---- CRUD Handler Functions ----

  const handleCreateTask = async (formData) => {
    setFormLoading(true);
    try {
      const response = await api.post('/tasks', formData);
      setTasks(prev => [response.data, ...prev]); // Add new task to top of list
      setShowForm(false);
      showSuccessMessage('Task created successfully!');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create task.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateTask = async (formData) => {
    setFormLoading(true);
    try {
      const response = await api.put(`/tasks/${editingTask.id}`, formData);
      // Replace the old task with the updated one in the tasks array
      setTasks(prev => prev.map(t => t.id === editingTask.id ? response.data : t));
      setEditingTask(null);
      setShowForm(false);
      showSuccessMessage('Task updated successfully!');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update task.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Are you sure you want to delete this task?')) return;
    try {
      await api.delete(`/tasks/${taskId}`);
      // Remove the deleted task from the local state (no need to re-fetch)
      setTasks(prev => prev.filter(t => t.id !== taskId));
      showSuccessMessage('Task deleted.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete task.');
    }
  };

  const handleEditClick = (task) => {
    setEditingTask(task);
    setShowForm(true);
  };

  const handleCloseForm = () => {
    setShowForm(false);
    setEditingTask(null);
  };

  const handleFormSubmit = (formData) => {
    if (editingTask) {
      handleUpdateTask(formData);
    } else {
      handleCreateTask(formData);
    }
  };

  const showSuccessMessage = (msg) => {
    setSuccess(msg);
    setTimeout(() => setSuccess(''), 3000); // Auto-hide after 3 seconds
  };

  if (loading) return <div className="spinner"></div>;

  return (
    <div>
      {/* Page Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.6rem', fontWeight: '700', marginBottom: '0.25rem' }}>Tasks</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            {user?.role === 'ROLE_ADMIN' ? 'All organization tasks' : 'Your personal tasks'}
          </p>
        </div>
        <button className="btn-primary" onClick={() => { setEditingTask(null); setShowForm(true); }}>
          + New Task
        </button>
      </div>

      {/* Alerts */}
      {error   && <div className="alert-error"   style={{ marginBottom: '1rem' }}>{error}</div>}
      {success && <div className="alert-success" style={{ marginBottom: '1rem' }}>{success}</div>}

      {/* Status Filter Bar */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        {['', 'TODO', 'IN_PROGRESS', 'COMPLETED'].map((s) => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            style={{
              padding: '0.4rem 0.9rem',
              borderRadius: '20px',
              border: '1px solid',
              borderColor: statusFilter === s ? 'var(--accent-blue)' : 'var(--border-color)',
              background:   statusFilter === s ? 'rgba(59,130,246,0.15)' : 'transparent',
              color:        statusFilter === s ? 'var(--accent-blue)' : 'var(--text-secondary)',
              fontSize: '0.8rem',
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
          >
            {s === '' ? 'All' : s.replace('_', ' ')}
          </button>
        ))}
        <span style={{ marginLeft: 'auto', color: 'var(--text-secondary)', fontSize: '0.8rem', alignSelf: 'center' }}>
          {filteredTasks.length} task{filteredTasks.length !== 1 ? 's' : ''}
        </span>
      </div>

      {/* Task List */}
      {filteredTasks.length === 0 ? (
        <div className="empty-state">
          <h3>No tasks found</h3>
          <p style={{ fontSize: '0.875rem', marginTop: '0.25rem' }}>
            {statusFilter ? `No tasks with status "${statusFilter.replace('_', ' ')}"` : 'Create your first task to get started!'}
          </p>
        </div>
      ) : (
        <div>
          {filteredTasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              currentUser={user}
              onEdit={handleEditClick}
              onDelete={handleDeleteTask}
            />
          ))}
        </div>
      )}

      {/* Task Form Modal */}
      {showForm && (
        <TaskForm
          existingTask={editingTask}
          onSubmit={handleFormSubmit}
          onClose={handleCloseForm}
          loading={formLoading}
        />
      )}
    </div>
  );
}

export default Tasks;
