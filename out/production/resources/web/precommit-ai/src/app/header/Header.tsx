"use client";
import React from "react";
import { FolderSymlink, Moon, Sun } from "lucide-react";
import { open } from "@tauri-apps/plugin-dialog";
import { useSelectDirectory } from "../context/SelectDirectoryContext";
import { useSelectTheme } from "../context/SelectThemeContext";

type Theme = "light" | "dark";

const Header: React.FC = () => {
  /* ----------- grab context values ----------- */
  const { selectedDirectory, setSelectedDirectory } = useSelectDirectory();
  const { theme, setTheme } = useSelectTheme();

  /* ----------- handlers ----------- */
  const pickDirectory = async () => {
    const path = await open({ directory: true, multiple: false });
    if (typeof path === "string") setSelectedDirectory(path);
  };

  const toggleTheme = () => {
    const newTheme: Theme = theme === "dark" ? "light" : "dark";
    setTheme(newTheme);
  };

  /* ----------- UI ----------- */
  return (
    <header
      className={`sticky top-0 z-50 w-full  ${
        theme === "dark" ? "transparent" : ""
      } px-4 py-3 flex items-center justify-between`}
    >
      {/* Brand */}
      <div className="flex items-center gap-2">
        <span
          className={`text-xl font-semibold ${
            theme === "dark" ? "text-white" : "text-gray-800"
          }`}
        ></span>
      </div>

      {/* Right‑hand controls */}
      <div className="flex items-center gap-4">
        {/* Theme toggle */}
        <button
          onClick={toggleTheme}
          className="p-2 rounded  transition"
          title="Toggle theme"
        >
          {theme === "dark" ? (
            <Sun className="h-5 w-5 text-yellow-300" />
          ) : (
            <Moon className="h-5 w-5 text-gray-900" />
          )}
        </button>

        {/* Avatar placeholder */}
        <div
          className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 text-white flex items-center justify-center text-sm font-medium shadow-md"
          title="User profile"
        >
          AK
        </div>
      </div>
    </header>
  );
};

export default Header;
