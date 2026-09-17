import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

/**
 * Dashboard.jsx - Main dashboard page showing task metrics.
 *
 * INTERVIEW NOTES:
 * - useEffect: React hook that runs code after the component renders.
 *   We use it to fetch tasks from the backend when the page loads.
 *   The empty dependency array [] means "run once, when the component mounts."
 *
 * - We derive task counts from the fetched tasks array using .filter().
 * - The dashboard shows 4 metric cards: Total, TODO, In Progress, Completed.
 * - Each count is computed live from the tasks array — no extra API calls needed.
 */
function Dashboard() {
  const { user } = useAuth();
  const [tasks, setTasks]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  // Fetch user's tasks when the component loads
  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const response = await api.get('/tasks');
        setTasks(response.data);
      } catch (err) {
        setError('Failed to load task data.');
      } finally {
        setLoading(false);
      }
    };

    fetchTasks();
  }, []); // [] = only run when component first mounts

  // Derive task counts by filtering the tasks array
  const todoCount       = tasks.filter(t => t.status === 'TODO').length;
  const inProgressCount = tasks.filter(t => t.status === 'IN_PROGRESS').length;
  const completedCount  = tasks.filter(t => t.status === 'COMPLETED').length;

  const metrics = [
    { label: 'Total Tasks',  count: tasks.length,  color: '#3b82f6', icon: '📋' },
    { label: 'To Do',        count: todoCount,      color: '#64748b', icon: '⏳' },
    { label: 'In Progress',  count: inProgressCount, color: '#f59e0b', icon: '🔄' },
    { label: 'Completed',    count: completedCount,  color: '#22c55e', icon: '✅' },
  ];

  if (loading) return <div className="spinner"></div>;

  return (
    <div>
      {/* Page Header */}
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.6rem', fontWeight: '700', marginBottom: '0.25rem' }}>
          Welcome back, {user?.name}! 👋
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Here's a summary of your tasks today.
        </p>
      </div>

      {error && <div className="alert-error" style={{ marginBottom: '1.5rem' }}>{error}</div>}

      {/* Metrics Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: '1rem',
        marginBottom: '2rem',
      }}>
        {metrics.map((metric) => (
          <div key={metric.label} className="card" style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>{metric.icon}</div>
            <div style={{
              fontSize: '2.5rem',
              fontWeight: '700',
              color: metric.color,
              lineHeight: '1.1',
              marginBottom: '0.25rem',
            }}>
              {metric.count}
            </div>
            <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
              {metric.label}
            </div>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="card">
        <h2 style={{ fontWeight: '600', marginBottom: '1rem', fontSize: '1rem' }}>
          Quick Actions
        </h2>
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <a href="/tasks" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-block' }}>
            + New Task
          </a>
          <a href="/tasks" className="btn-ghost" style={{ textDecoration: 'none', display: 'inline-block' }}>
            View All Tasks
          </a>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
