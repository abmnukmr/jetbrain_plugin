"use client";
import CodeGuide from "../components/CodeGuide";
import CodeDiff from "../components/CodeDiff";
import Header from "../header/Header";
import { useSelectTheme } from "../context/SelectThemeContext";
import { useSelectDirectory } from "../context/SelectDirectoryContext";
import { FolderTree } from "../components/UiComponents/FolderTree";
import { FileWriteProvider } from "../context/FileReadWriteContext";
import ProjectDirectoryExplorer from "../components/UiComponents/ProjectDirectoryExplorer";
import { MonacoEditor } from "../components/codeBlock/MonacoEditor";
import { ResizableLayout } from "../components/view/ResizableLayout";
import { useEffect, useState } from "react";

export const CodeView = () => {
  const { theme } = useSelectTheme();

  const { selectedDirectory } = useSelectDirectory();
  return (
    <FileWriteProvider>
      <div
        className={`h-screen w-full ${
          theme === "dark"
            ? "text-gray-100 bg-[#171717]"
            : "text-gray-900 bg-gray-50"
        }`}
      >
        <Header />

        <div
          style={{
            maxHeight: "1000px",
            maxWidth: "400px",
            overflowY: "auto",
            overflowX: "auto",
            whiteSpace: "pre-wrap",
          }}
        ></div>

        <ResizableLayout />
      </div>
    </FileWriteProvider>
  );
};
