import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

/**
 * Login.jsx - User login page.
 *
 * INTERVIEW NOTES — "What happens when a user clicks Login?":
 * 1. User fills in email + password in the form.
 * 2. handleSubmit() is called when the form is submitted.
 * 3. We call: api.post('/auth/login', { email, password })
 *    → api.js Axios instance → HTTP POST http://localhost:8080/api/auth/login
 * 4. Spring Boot receives it → AuthController → AuthService → verify credentials
 * 5. If valid: AuthService calls JwtService.generateToken() → returns JWT
 * 6. Backend response: { token, userId, name, email, role }
 * 7. We call auth.login(data) → stores token in localStorage, sets user state
 * 8. navigate('/dashboard') → React Router redirects to the dashboard
 * 9. ProtectedRoute now sees user !== null → allows access
 *
 * - useState: React hook for managing form input values and error/loading state.
 * - useNavigate: React Router hook to programmatically navigate to another page.
 * - try/catch: Handles API errors — shows error message if login fails.
 * - e.target.value: Reads the current value of the input field on each keystroke.
 */
function Login() {
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  const navigate = useNavigate();
  const auth     = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevents the default browser form submission behaviour
    setError('');
    setLoading(true);

    try {
      // Send login credentials to the backend
      const response = await api.post('/auth/login', { email, password });

      // Store the JWT and user info in AuthContext + localStorage
      auth.login(response.data);

      // Redirect to dashboard on success
      navigate('/dashboard');
    } catch (err) {
      // Show the backend error message, or a generic fallback
      if (!err.response) {
        setError('Cannot connect to server. Make sure the backend is running on port 8080.');
      } else {
        setError(err.response?.data?.message || 'Invalid email or password. Please try again.');
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
            background: 'var(--accent-blue)',
            borderRadius: '16px',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '1.75rem', margin: '0 auto 1rem',
          }}>🔒</div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', marginBottom: '0.5rem' }}>
            Welcome back
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Sign in to your SecureTask account
          </p>
        </div>

        {/* Login Form Card */}
        <div className="card">
          <form onSubmit={handleSubmit}>
            {/* Error Alert */}
            {error && <div className="alert-error" style={{ marginBottom: '1rem' }}>{error}</div>}

            {/* Email Field */}
            <div className="form-group">
              <label className="label" htmlFor="email">Email address</label>
              <input
                id="email"
                type="email"
                className="input"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>

            {/* Password Field */}
            <div className="form-group">
              <label className="label" htmlFor="password">Password</label>
              <input
                id="password"
                type="password"
                className="input"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              className="btn-primary"
              style={{ width: '100%', padding: '0.75rem', marginTop: '0.5rem', fontSize: '1rem' }}
              disabled={loading}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          {/* Register Link */}
          <div style={{ textAlign: 'center', marginTop: '1.25rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            Don't have an account?{' '}
            <Link to="/register" style={{ color: 'var(--accent-blue)', textDecoration: 'none', fontWeight: '500' }}>
              Create one
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
