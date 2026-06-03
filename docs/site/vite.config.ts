import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { resolve } from "node:path";

export default defineConfig({
  plugins: [react()],
  publicDir: "static",
  resolve: {
    alias: {
      "@titlemanager/core": resolve(__dirname, ".generated/titlemanager-core"),
      "@site": __dirname,
    },
  },
  build: {
    outDir: "vite-dist",
    emptyOutDir: true,
    rollupOptions: {
      output: {
        advancedChunks: {
          groups: [
            { name: "react", test: /node_modules\/(?:react|react-dom)\// },
            { name: "titlemanager-core", test: /\.generated\/titlemanager-core\// },
          ],
        },
      },
    },
  },
});
