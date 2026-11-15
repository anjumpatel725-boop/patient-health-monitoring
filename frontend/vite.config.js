import { defineConfig } from "vite";
import { resolve } from "path";

export default defineConfig({
  base: "/patient-health-monitoring/",
  build: {
    rollupOptions: {
      input: {
        main: resolve(__dirname, "index.html"),
        login: resolve(__dirname, "login.html"),
        register: resolve(__dirname, "register.html"),
        patient: resolve(__dirname, "patient-dashboard.html"),
        doctor: resolve(__dirname, "doctor-dashboard.html"),
        admin: resolve(__dirname, "admin-dashboard.html")
      }
    }
  }
});
