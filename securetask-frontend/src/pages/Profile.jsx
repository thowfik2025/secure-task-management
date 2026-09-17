import { useAuth } from '../context/AuthContext';

/**
 * Profile.jsx - Shows the current user's account information.
 * A read-only view of the logged-in user's name, email, and role.
 * Data comes from AuthContext (parsed from the login response / localStorage).
 */
function Profile() {
  const { user } = useAuth();

  const fields = [
    { label: 'Full Name', value: user?.name },
    { label: 'Email Address', value: user?.email },
    { label: 'Account Role', value: user?.role === 'ROLE_ADMIN' ? '🛡️ Administrator' : '👤 User' },
    { label: 'User ID', value: `#${user?.userId}` },
  ];

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.6rem', fontWeight: '700', marginBottom: '0.25rem' }}>Profile</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Your account information</p>
      </div>

      <div className="card" style={{ maxWidth: '480px' }}>
        {/* Avatar */}
        <div style={{
          width: '72px', height: '72px',
          background: 'var(--accent-blue)',
          borderRadius: '50%',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '1.75rem',
          marginBottom: '1.5rem',
        }}>
          {user?.name?.charAt(0).toUpperCase()}
        </div>

        {/* Profile Fields */}
        {fields.map((field) => (
          <div key={field.label} style={{
            padding: '0.875rem 0',
            borderBottom: '1px solid var(--border-color)',
            display: 'flex',
            justifyContent: 'space-between',
          }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: '500' }}>
              {field.label}
            </span>
            <span style={{ fontSize: '0.875rem', fontWeight: '500' }}>{field.value}</span>
          </div>
        ))}

        {/* Security note */}
        <div style={{ marginTop: '1.25rem', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          🔒 Your password is securely hashed with BCrypt and is never stored or shown in plaintext.
        </div>
      </div>
    </div>
  );
}

export default Profile;
