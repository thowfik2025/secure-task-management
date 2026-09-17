import { createContext, useContext, useState } from 'react';
import { jwtDecode } from 'jwt-decode';

/**
 * AuthContext - React Context for managing authentication state globally.
 *
 * INTERVIEW NOTES:
 * - React Context: A way to share data (state) across many components without
 *   passing props down through every level. It's essentially a global store.
 *
 * - Why AuthContext?
 *   Multiple components need to know: "Is the user logged in? What is their role?"
 *   Without context, we'd have to pass this as props through every component.
 *   With context, any component can call useAuth() to get user info directly.
 *
 * - createContext(): Creates the Context object with a default value of null.
 * - useContext(AuthContext): The hook any component uses to read the context value.
 *
 * - jwtDecode(token): Decodes the JWT payload WITHOUT verifying the signature.
 *   We only use this on the frontend to extract user info (role, name, etc.)
 *   from the token. The backend ALWAYS verifies the signature before trusting a token.
 *
 * - What gets stored in 'user' state:
 *   { userId, name, email, role } — extracted from the login response.
 *   We also keep the raw token in localStorage for the API interceptor.
 *
 * Flow:
 *   1. User logs in → backend returns { token, userId, name, email, role }
 *   2. login(data) stores token in localStorage, sets user state
 *   3. All components can now call useAuth().user to access logged-in user info
 *   4. logout() clears everything and redirects to /login
 */

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    // On page refresh, try to restore user from localStorage
    // This keeps the user logged in across browser refreshes
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  /**
   * login(data) - Called after a successful POST /api/auth/login response.
   * @param {Object} data - { token, userId, name, email, role } from the backend
   */
  const login = (data) => {
    // Store the raw JWT token for the Axios request interceptor
    localStorage.setItem('token', data.token);

    // Store user info (without the token) for easy access across the app
    const userInfo = {
      userId: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    };
    localStorage.setItem('user', JSON.stringify(userInfo));
    setUser(userInfo);
  };

  /**
   * logout() - Clears all authentication data and resets the user state.
   * After calling this, the user is unauthenticated and ProtectedRoute
   * will redirect them to the login page.
   */
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  /**
   * isAdmin() - Convenience function to check if the current user is an ADMIN.
   * Used to conditionally render admin-only UI elements (e.g., Admin Users page link).
   */
  const isAdmin = () => {
    return user && user.role === 'ROLE_ADMIN';
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * useAuth() - Custom hook to access AuthContext.
 * Usage in any component: const { user, login, logout, isAdmin } = useAuth();
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside an AuthProvider component');
  }
  return context;
}
