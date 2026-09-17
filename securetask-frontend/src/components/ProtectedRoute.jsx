import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * ProtectedRoute - Guards routes that require authentication.
 *
 * INTERVIEW NOTES:
 * - How does React route protection work?
 *   React Router renders components based on the URL. Without protection,
 *   anyone could navigate to /dashboard even without logging in.
 *   ProtectedRoute checks if the user is authenticated BEFORE rendering the page.
 *   If not authenticated → redirect to /login.
 *   If authenticated → render the actual page.
 *
 * - Props:
 *   children: The protected page component to render if access is allowed.
 *   adminOnly (optional): If true, only ROLE_ADMIN users can access this route.
 *
 * - <Navigate to="/login" replace />:
 *   Redirects to /login without adding the current URL to browser history
 *   ('replace' means the user can't hit Back to get back to the protected page).
 */
function ProtectedRoute({ children, adminOnly = false }) {
  const { user, isAdmin } = useAuth();

  // Not logged in → redirect to login
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Admin-only route, but user is not ADMIN → redirect to dashboard
  if (adminOnly && !isAdmin()) {
    return <Navigate to="/dashboard" replace />;
  }

  // Authentication checks passed → render the protected page
  return children;
}

export default ProtectedRoute;
