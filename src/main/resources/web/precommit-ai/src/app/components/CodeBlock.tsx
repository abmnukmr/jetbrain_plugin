"use client";

import React from "react";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import {
  vscDarkPlus,
  duotoneLight,
  oneDark,
  prism,
} from "react-syntax-highlighter/dist/esm/styles/prism";

interface CodeBlockProps {
  code: string;
  language: string;
  theme?: string; // "dark" or "light"
}

const CodeBlock: React.FC<CodeBlockProps> = ({ code, language, theme }) => {
  const githubDarkTheme = {
    'code[class*="language-"]': {
      color: "#c9d1d9",
      background: "#0d1117",
      fontFamily: 'SFMono-Regular,Consolas,"Liberation Mono",Menlo,monospace',
      fontSize: "14px",
    },
    'pre[class*="language-"]': {
      color: "#c9d1d9",
      background: "#0d1117",
      padding: "1em",
      borderRadius: "8px",
      overflow: "auto",
    },
    // Fix white background issue:
    ".token": {
      background: "transparent !important",
    },
    ".token.comment": { color: "#8b949e", background: "transparent" },
    ".token.keyword": { color: "#ff7b72", background: "transparent" },
    ".token.string": { color: "#a5d6ff", background: "transparent" },
    ".token.function": { color: "#d2a8ff", background: "transparent" },
    ".token.class-name": { color: "#ffa657", background: "transparent" },
  };
  return (
    <div className="overflow-x-scroll text-sm rounded-md p-1">
      <SyntaxHighlighter
        language={language}
        style={theme === "dark" ? oneDark : prism}
        customStyle={{
          margin: 0,
          background: `${theme === "dark" ? "transparent" : "transparent"}`,
          borderRadius: "0.75rem",
          padding: "0.5rem",
          fontSize: "13px",
        }}
      >
        {code}
      </SyntaxHighlighter>
    </div>
  );
};

export default CodeBlock;
