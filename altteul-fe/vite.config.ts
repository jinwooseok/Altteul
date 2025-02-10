import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
  plugins: [react(), tsconfigPaths()],
  define: {
    global: {}
  },
  server: {
    watch: {
      usePolling: true,
      interval: 100
    },
    host: true,
    strictPort: true,
    port: 5173,
    allowedHosts: [
      'i12c203.p.ssafy.io',
      'localhost',
      'localhost:5173'
    ]
  }
});