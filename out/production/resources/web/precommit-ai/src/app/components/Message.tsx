"use client";

import React, { useEffect, useMemo, useState } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { useTauriFileSystem } from "../hooks/useTauriFileSystem";
import { ContentBlock } from "./ContentBlock";
import { useSelectTheme } from "../context/SelectThemeContext";

const Message = React.memo(function Message({
  role,
  content,
}: {
  role: string;
  content: string;
}) {
  const { theme } = useSelectTheme();

  const isUser = role === "user";
  const messageClasses = isUser
    ? theme === "dark"
      ? "bg-[transparent] text-white"
      : "text-gray-900 bg-[transparent]"
    : theme === "dark"
    ? "bg-[transparent] text-white"
    : "text-gray-900 bg-[transparent]";

  return (
    <div className={`flex ${isUser ? "justify-end" : ""} relative`}>
      <div className={`p-2  w-full ${messageClasses}`}>
        <ReactMarkdown remarkPlugins={[remarkGfm]} components={ContentBlock}>
          {content}
        </ReactMarkdown>
      </div>
    </div>
  );
});

export default Message;
