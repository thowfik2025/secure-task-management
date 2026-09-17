import { useState, useEffect } from 'react';
import api from '../services/api';

/**
 * AdminUsers.jsx - Admin-only page showing all registered users.
 *
 * INTERVIEW NOTES:
 * - This page is only accessible to ROLE_ADMIN users.
 *   Access is restricted at TWO levels:
 *   1. Frontend: ProtectedRoute wraps this with adminOnly={true} → non-admins are redirected.
 *   2. Backend: SecurityConfig restricts GET /api/users to hasAuthority("ROLE_ADMIN").
 *      Even if someone bypasses the frontend, the API call will return 403 Forbidden.
 *
 * - This is called "Defense in Depth" — multiple layers of security.
 *   Front-end restrictions are for UX. Backend restrictions are the real security.
 */
function AdminUsers() {
  const [users, setUsers]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await api.get('/users'); // GET /api/users — ADMIN only
        setUsers(response.data);
      } catch (err) {
        if (err.response?.status === 403) {
          setError('Access denied. Admin privileges required.');
        } else {
          setError('Failed to load users.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  if (loading) return <div className="spinner"></div>;

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.6rem', fontWeight: '700', marginBottom: '0.25rem' }}>
          🛡️ Admin — User Management
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          All registered users in the system ({users.length} total)
        </p>
      </div>

      {error && <div className="alert-error" style={{ marginBottom: '1.5rem' }}>{error}</div>}

      {users.length === 0 && !error ? (
        <div className="empty-state">
          <h3>No users found</h3>
        </div>
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td style={{ color: 'var(--text-secondary)', fontFamily: 'monospace', fontSize: '0.8rem' }}>
                      #{u.id}
                    </td>
                    <td style={{ fontWeight: '500' }}>{u.name}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{u.email}</td>
                    <td>
                      <span className={`badge ${u.role === 'ROLE_ADMIN' ? 'badge-high' : 'badge-low'}`}>
                        {u.role === 'ROLE_ADMIN' ? '🛡️ Admin' : '👤 User'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Security info box */}
      <div className="card" style={{ marginTop: '1.5rem', borderColor: 'var(--accent-blue)' }}>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          🔒 <strong style={{ color: 'var(--text-primary)' }}>Security Note:</strong> User passwords are never
          returned by the API — they are hashed with BCrypt and only stored in the database.
          This page is protected at both the frontend (ProtectedRoute) and backend
          (Spring Security ROLE_ADMIN check) levels.
        </p>
      </div>
    </div>
  );
}

export default AdminUsers;
