import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

/**
 * vite.config.js - Vite build tool configuration.
 * 
 * - @vitejs/plugin-react: Enables React JSX and Fast Refresh (hot reload).
 * - @tailwindcss/vite: Integrates Tailwind CSS directly into the Vite build pipeline.
 *   No separate tailwind.config.js needed with this newer plugin approach.
 */
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
})
