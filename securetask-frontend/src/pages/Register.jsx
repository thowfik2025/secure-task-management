import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

/**
 * Register.jsx - New user registration page.
 *
 * Flow: Form → api.post('/auth/register') → auto-login with credentials → navigate to /dashboard.
 */
function Register() {
  const [name, setName]                     = useState('');
  const [email, setEmail]                   = useState('');
  const [password, setPassword]             = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError]                   = useState('');
  const [success, setSuccess]               = useState('');
  const [loading, setLoading]               = useState(false);

  const navigate = useNavigate();
  const auth     = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Frontend validation: passwords must match
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    setLoading(true);

    try {
      // Step 1: POST /api/auth/register — send name, email, password
      await api.post('/auth/register', { name, email, password });

      setSuccess('Account created successfully! Logging you in...');

      // Step 2: Auto-login immediately after registration for seamless UX
      const loginResponse = await api.post('/auth/login', { email, password });
      auth.login(loginResponse.data);

      // Step 3: Redirect to dashboard
      setTimeout(() => navigate('/dashboard'), 800);
    } catch (err) {
      const data = err.response?.data;
      if (data?.errors) {
        // Field-level validation errors: { errors: { field: "message" } }
        const msgs = Object.values(data.errors).join(', ');
        setError(msgs);
      } else if (data?.message) {
        setError(data.message);
      } else if (!err.response) {
        setError('Cannot connect to server. Make sure the backend is running on port 8080.');
      } else {
        setError(`Registration failed (${err.response?.status || 'unknown error'}). Please try again.`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      background: 'var(--bg-primary)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '1rem',
    }}>
      <div style={{ width: '100%', maxWidth: '400px' }}>
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{
            width: '56px', height: '56px',
            background: 'var(--accent-green)',
            borderRadius: '16px',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '1.75rem', margin: '0 auto 1rem',
          }}>✨</div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', marginBottom: '0.5rem' }}>
            Create Account
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Join SecureTask to manage your work
          </p>
        </div>

        {/* Register Form Card */}
        <div className="card">
          <form onSubmit={handleSubmit}>
            {error   && <div className="alert-error"   style={{ marginBottom: '1rem' }}>{error}</div>}
            {success && <div className="alert-success" style={{ marginBottom: '1rem' }}>{success}</div>}

            <div className="form-group">
              <label className="label" htmlFor="name">Full Name</label>
              <input id="name" type="text" className="input" placeholder="John Doe"
                value={name} onChange={(e) => setName(e.target.value)} required />
            </div>

            <div className="form-group">
              <label className="label" htmlFor="email">Email Address</label>
              <input id="email" type="email" className="input" placeholder="you@example.com"
                value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>

            <div className="form-group">
              <label className="label" htmlFor="password">Password</label>
              <input id="password" type="password" className="input" placeholder="Min 6 characters"
                value={password} onChange={(e) => setPassword(e.target.value)} required />
            </div>

            <div className="form-group">
              <label className="label" htmlFor="confirmPassword">Confirm Password</label>
              <input id="confirmPassword" type="password" className="input" placeholder="Repeat password"
                value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
            </div>

            <button type="submit" className="btn-primary"
              style={{ width: '100%', padding: '0.75rem', marginTop: '0.5rem', fontSize: '1rem' }}
              disabled={loading}>
              {loading ? 'Creating Account...' : 'Create Account'}
            </button>
          </form>

          <div style={{ textAlign: 'center', marginTop: '1.25rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: 'var(--accent-blue)', textDecoration: 'none', fontWeight: '500' }}>
              Sign in
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Register;
