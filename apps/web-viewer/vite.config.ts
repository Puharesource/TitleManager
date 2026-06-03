import { resolve } from "node:path";
import { defineConfig } from "vite";

export default defineConfig({
  resolve: {
    alias: {
      "@titlemanager/core": resolve(__dirname, ".generated/titlemanager-core")
    }
  }
});
