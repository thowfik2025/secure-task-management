import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Tasks from './pages/Tasks';
import Profile from './pages/Profile';
import AdminUsers from './pages/AdminUsers';

/**
 * App.jsx - Root component that sets up routing.
 *
 * INTERVIEW NOTES:
 * - BrowserRouter: Enables React Router's URL-based navigation.
 *   Uses the browser's History API (no # in the URL).
 *
 * - AuthProvider: Wraps the ENTIRE app so every component has access to
 *   auth state via useAuth() without prop drilling.
 *
 * - Routes / Route: Define which component renders at which URL path.
 *
 * - Public routes (/login, /register): Visible without authentication.
 *   If a user who is already logged in visits /login, we redirect them to /dashboard.
 *
 * - Protected routes: Wrapped in <ProtectedRoute> which checks authentication.
 *   If not logged in → redirect to /login.
 *
 * - AdminLayout: For logged-in users, the sidebar and navbar are shown.
 *   We create a simple wrapper here to avoid repeating them in every page.
 *
 * - Navigate to="/dashboard": Default redirect — visiting "/" goes to /dashboard.
 */

// Layout wrapper for authenticated pages (shows Navbar + Sidebar)
function AuthenticatedLayout({ children }) {
  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <Navbar />
        <main className="page-content">
          {children}
        </main>
      </div>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      {/* AuthProvider wraps everything — all child components can use useAuth() */}
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Routes — require authentication */}
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <AuthenticatedLayout>
                <Dashboard />
              </AuthenticatedLayout>
            </ProtectedRoute>
          } />

          <Route path="/tasks" element={
            <ProtectedRoute>
              <AuthenticatedLayout>
                <Tasks />
              </AuthenticatedLayout>
            </ProtectedRoute>
          } />

          <Route path="/profile" element={
            <ProtectedRoute>
              <AuthenticatedLayout>
                <Profile />
              </AuthenticatedLayout>
            </ProtectedRoute>
          } />

          {/* Admin-only route */}
          <Route path="/admin/users" element={
            <ProtectedRoute adminOnly={true}>
              <AuthenticatedLayout>
                <AdminUsers />
              </AuthenticatedLayout>
            </ProtectedRoute>
          } />

          {/* Default redirect: visiting "/" goes to /dashboard */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* Catch-all: unknown routes go to /dashboard */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
