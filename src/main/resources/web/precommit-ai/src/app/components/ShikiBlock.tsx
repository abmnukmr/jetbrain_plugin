"use client";

import React, { useEffect, useState } from "react";
import { createHighlighter } from "shiki";

interface ShikiBlockProps {
  code: string;
  language: string;
  theme?: string; // Optional, can be used to specify a different theme
}

const ShikiBlock: React.FC<ShikiBlockProps> = ({ code, language, theme }) => {
  const [html, setHtml] = useState('<pre class="shiki">Loading...</pre>');

  useEffect(() => {
    const load = async () => {
      try {
        const highlighter = await createHighlighter({
          themes: ["github-dark-high-contrast", "github-light-high-contrast"],
          langs: [
            "javascript",
            "typescript",
            "python",
            "java",
            "csharp",
            "go",
            "ruby",
            "php",
            "html",
            "css",
            "json",
            "bash",
            "xml",
            "txt",
            "plaintext",
            "properties",
            "dockerfile",
          ],
        });

        const htmlOutput = await highlighter.codeToHtml(code, {
          lang: language,
          theme:
            theme === "dark"
              ? "github-dark-high-contrast"
              : "github-light-high-contrast",
        });

        setHtml(htmlOutput);
      } catch (err) {
        console.error("Shiki error:", err);
        setHtml(`<pre class="">${code}</pre>`);
      }
    };

    load();
  }, [code, language, theme]);

  return (
    <div
      
      className={`shiki bg-transparent
        
       overflow-x-auto text-sm rounded-xl p-1`}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
};

export default ShikiBlock;
