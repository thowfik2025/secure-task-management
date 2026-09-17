import axios from 'axios';

/**
 * api.js - Centralized Axios HTTP client configuration.
 *
 * INTERVIEW NOTES:
 * - Why a centralized API file?
 *   Instead of using axios.get('http://localhost:8080/api/...') in every component,
 *   we create ONE configured Axios instance here. Every component imports this instance.
 *   Benefits: one place to change the base URL, one place to manage auth headers.
 *
 * - baseURL: Every request made with this instance automatically gets this prefix.
 *   For example: api.get('/tasks') sends GET http://localhost:8080/api/tasks
 *
 * - Request Interceptor: This function runs BEFORE every outgoing request.
 *   It reads the JWT token from localStorage and AUTOMATICALLY attaches it
 *   to the 'Authorization' header as "Bearer <token>".
 *   This means we NEVER have to manually add the auth header in individual components.
 *
 * - Why localStorage?
 *   We store the JWT in localStorage when the user logs in.
 *   On every subsequent API call, the interceptor reads it and attaches it.
 *   On logout, we simply remove it from localStorage.
 *   (Note: For higher security, HttpOnly cookies are better, but localStorage
 *   is standard for beginner/intermediate full-stack React projects.)
 *
 * - Response Interceptor: Handles 401 Unauthorized globally.
 *   If the backend returns 401 (token expired or missing), we automatically
 *   clear localStorage and redirect to the login page.
 */

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// ============================================================
// Request Interceptor — Attach JWT to every request
// ============================================================
api.interceptors.request.use(
  (config) => {
    // Read the JWT token stored in localStorage after login
    const token = localStorage.getItem('token');

    // If a token exists, add it to the Authorization header
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    // If the request itself failed (e.g., network error), reject the promise
    return Promise.reject(error);
  }
);

// ============================================================
// Response Interceptor — Handle 401 Unauthorized globally
// ============================================================
api.interceptors.response.use(
  (response) => {
    // Request was successful — pass the response through unchanged
    return response;
  },
  (error) => {
    // If the server responded with 401, the token is expired or invalid
    if (error.response && error.response.status === 401) {
      // Clear auth data and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
