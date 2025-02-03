import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tsconfigPaths from "vite-tsconfig-paths";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tsconfigPaths()],
  // resolve: {
  //   alias: [
  //     { find: "@", replacement: "src" },
  //     { find: "@assets", replacement: "/src/assets" },
  //     { find: "@components", replacement: "/src/components" },
  //     { find: "@hooks", replacement: "/src/hooks" },
  //     { find: "@pages", replacement: "/src/pages" },
  //     { find: "@router", replacement: "/src/router" },
  //     { find: "@store", replacement: "/src/store" },
  //     { find: "@utils", replacement: "/src/utils" },
  //     { find: "@styles", replacement: "/src/styles" },
  //   ],
  // },
});
