import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Sidebar - Left navigation menu.
 * Shows links based on the user's role (admin sees extra link).
 */
function Sidebar() {
  const { isAdmin } = useAuth();

  const linkStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    padding: '0.7rem 1rem',
    borderRadius: '8px',
    textDecoration: 'none',
    color: 'var(--text-secondary)',
    fontSize: '0.875rem',
    fontWeight: '500',
    transition: 'all 0.2s',
    marginBottom: '0.25rem',
  };

  const activeStyle = {
    ...linkStyle,
    background: 'rgba(59, 130, 246, 0.15)',
    color: 'var(--accent-blue)',
  };

  return (
    <aside style={{
      width: '220px',
      minHeight: '100vh',
      background: 'var(--bg-secondary)',
      borderRight: '1px solid var(--border-color)',
      padding: '1.5rem 1rem',
      flexShrink: 0,
    }}>
      <nav>
        {/* NavLink from React Router applies active classes automatically */}
        <NavLink
          to="/dashboard"
          style={({ isActive }) => isActive ? activeStyle : linkStyle}
        >
          <span>📊</span> Dashboard
        </NavLink>

        <NavLink
          to="/tasks"
          style={({ isActive }) => isActive ? activeStyle : linkStyle}
        >
          <span>✅</span> Tasks
        </NavLink>

        <NavLink
          to="/profile"
          style={({ isActive }) => isActive ? activeStyle : linkStyle}
        >
          <span>👤</span> Profile
        </NavLink>

        {/* Admin-only link — only renders if user has ROLE_ADMIN */}
        {isAdmin() && (
          <NavLink
            to="/admin/users"
            style={({ isActive }) => isActive ? activeStyle : linkStyle}
          >
            <span>🛡️</span> Admin Users
          </NavLink>
        )}
      </nav>

      {/* Sidebar footer */}
      <div style={{
        position: 'absolute',
        bottom: '1.5rem',
        left: '1rem',
        right: '1rem',
        fontSize: '0.75rem',
        color: 'var(--text-secondary)',
        textAlign: 'center',
        padding: '0.5rem',
        borderTop: '1px solid var(--border-color)',
        paddingTop: '1rem',
      }}>
        SecureTask v1.0.0
      </div>
    </aside>
  );
}

export default Sidebar;
