import { useAuth } from '../context/AuthContext';

/**
 * Navbar - Top navigation bar.
 * Shows: App name, current user info, and Logout button.
 */
function Navbar() {
  const { user, logout } = useAuth();

  return (
    <nav style={{
      background: 'var(--bg-secondary)',
      borderBottom: '1px solid var(--border-color)',
      padding: '0.875rem 1.5rem',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      position: 'sticky',
      top: 0,
      zIndex: 50,
    }}>
      {/* App logo / name */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <div style={{
          width: '32px', height: '32px',
          background: 'var(--accent-blue)',
          borderRadius: '8px',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '1rem',
        }}>🔒</div>
        <span style={{ fontWeight: '700', fontSize: '1rem', color: 'var(--text-primary)' }}>
          SecureTask
        </span>
      </div>

      {/* User info + logout */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '0.875rem', fontWeight: '500', color: 'var(--text-primary)' }}>
            {user?.name}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
            {user?.role === 'ROLE_ADMIN' ? '🛡️ Admin' : '👤 User'}
          </div>
        </div>

        <button
          onClick={logout}
          className="btn-ghost"
          style={{ fontSize: '0.8rem' }}
        >
          Logout
        </button>
      </div>
    </nav>
  );
}

export default Navbar;
