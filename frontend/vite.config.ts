/// <reference types="vitest/config" />
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "node:path";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(import.meta.dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": process.env.API_PROXY_TARGET ?? "http://localhost:8080",
    },
  },
  build: {
    rolldownOptions: {
      output: {
        // Keep the big chart library out of the first load; it is only needed on the dashboard.
        manualChunks: (id: string) => (id.includes("node_modules/recharts") || id.includes("node_modules/d3-") ? "charts" : undefined),
      },
    },
  },
  test: {
    environment: "jsdom",
    setupFiles: ["./src/test/setup.ts"],
    css: false,
  },
});
