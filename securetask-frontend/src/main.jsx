import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

/**
 * main.jsx - The entry point of the React application.
 *
 * INTERVIEW NOTES:
 * - createRoot: React 18's new rendering API. Replaces the old ReactDOM.render().
 * - document.getElementById('root'): Finds the <div id="root"> in index.html.
 *   React renders the entire app inside this single div.
 * - StrictMode: A React development tool that helps identify common mistakes.
 *   It runs certain checks twice in development only — has no effect in production.
 * - import './index.css': Loads the global CSS (Tailwind + our custom styles).
 */
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
