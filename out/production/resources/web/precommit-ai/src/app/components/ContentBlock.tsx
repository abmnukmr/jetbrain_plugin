import { ReactNode, useCallback, useEffect, useState } from "react";
import type { Components } from "react-markdown";
import dynamic from "next/dynamic";
import FilePathWithPlay from "./FilePathViewer";
import { useTauriFileSystem } from "../hooks/useTauriFileSystem";
import { CopyButton } from "./UiComponents/CopyButton";
import { GitCompareButton } from "./UiComponents/GitCompareButton";
import {
  extractFirstFilePath,
  isFilePath,
  isTreeStructure,
} from "../util/util";
import { useSelectTheme } from "../context/SelectThemeContext";
import { useSelectDirectory } from "../context/SelectDirectoryContext";
import { useNotification } from "../context/NotificationContext";
import { useFileReaderWriter } from "../context/FileReadWriteContext";
import { Play } from "lucide-react";
import { CodeExecute } from "./UiComponents/CodeExecute";
import { useSendToVsCode } from "../hooks/useSentoVsCode";
import CodeBlock from "./CodeBlock";

///const ShikiBlock = dynamic(() => import("./ShikiBlock"), { ssr: false });

export const ContentBlock: Components = {
  code({
    inline,
    className,
    children,
    ...props
  }: {
    inline?: boolean;
    className?: string;
    children?: ReactNode;
    [key: string]: any;
  }) {
    const { writeFile } = useFileReaderWriter();
    const { theme } = useSelectTheme();
    const { selectedDirectory, setSelectedDirectory, selectedFiles, setSelectedFiles } = useSelectDirectory();
    const { showNotification } = useNotification();



    const [fileContent, setFileContent] = useState<string>("");
    const sendToVscode = useSendToVsCode();
    const [debug, setDebug] = useState("");

    useEffect(() => {
      const handleMessage = (event: MessageEvent) => {



        if (event.data.command === "getWorkspaceData") {
        }

        if (event.data.command === "VScodeFileContent") {
          setFileContent(event?.data?.content);
        }


        if (event.data.command === "writeFile") {
        }
      };

      window.addEventListener("message", handleMessage);
      sendToVscode("getWorkspaceData", "Hello from React iframe! new");


      return () => window.removeEventListener("message", handleMessage);
    }, []);

    const readVSFileHandler = useCallback(
      (filePath: string) => {

      },
      [selectedDirectory]
    );

    const writeVSFileHandler = useCallback(
      (filePath: string, content: string) => {

      },
      [selectedDirectory]
    );

    const handleCopy = (e: React.FormEvent, path: string, code: string) => {
      e.preventDefault();


      // Check if path exists and is outside the project root
      if (path && !path.includes(String(selectedDirectory))) {
        // Remove leading forward slash if present
        path = path.replace(/^\/+/, "");

        // Optionally prepend the correct root path
        path = path.startsWith(String(selectedDirectory).split("/")[0])
          ? path
          : `${selectedDirectory}/${path}`;
      }

      writeVSFileHandler(path, code);
    };
    const code = String(children).replace(/\n$/, "");
    const match = /language-(\w+)/.exec(className || "");
    const lang = match?.[1] || (isTreeStructure(code) ? "txt" : "plaintext");

    const pathRegex =
      /(?<!https?:)(?<!:\/\/)(?:\.\/|\.\.\/|\/|[a-zA-Z]:\\)[\w\-./\\]+(?:\.\w+)?/g;
    const paths = code.match(pathRegex);
    if (paths && paths.length > 0 && (lang === "txt" || lang === "plaintext")) {
      return <FilePathWithPlay path={code} />;
    }

    if (!isTreeStructure(code) && (inline || !className)) {
      const bgColor = theme === "dark" ? "bg-gray-800" : "bg-gray-100";
      const textColor = theme === "dark" ? "text-red-200" : "text-red-800";

      return (
        <code
          className={`${bgColor} ${textColor} px-1 py-0.5 rounded text-[12px] font-mono whitespace-pre-wrap`}
        >
          {code}
        </code>
      );
    }

    const filePath = extractFirstFilePath(code); // Your function to extract path

    let path = filePath || "";
    if (path && !path.includes(String(selectedDirectory))) {
      // Remove leading forward slash if present
      path = path.replace(/^\/+/, "");

      // Optionally prepend the correct root path
      path = path.startsWith(String(selectedDirectory))
        ? path
        : `${String(selectedDirectory)}/${path}`;
    }
    const isFilePathDetected = isFilePath(code);
    const codeWithoutPath =
      filePath && !isFilePathDetected
        ? code.replace(filePath, "").trimStart()
        : code;

    return (
      <div
        className={`relative my-2 border p-2 ${
          theme === "dark" ? "border-gray-700 bg-[#0a0c10]" : "border-gray-300"
        } rounded-xl`}
      >
        <div className="flex items-center gap-2 justify-between">
          <div className={`text-[8px]  py-2 font-mono`}>
            {} {lang ? ` • ${lang.toLocaleLowerCase()}` : ""}
          </div>
          <div
            className={`flex items-center justify-center gap-2 py-1 px-1 ${
              theme === "dark" ? "bg-[#0a0c10]" : ""
            }`}
          >
            {lang !== "bash" ? (
              <>
                <CopyButton
                  code={codeWithoutPath}
                  onClick={(e: any) => handleCopy(e, path, codeWithoutPath)}
                />
                <GitCompareButton
                  onClick={() => {
                    readVSFileHandler(
                      "/Users/abhimanyu.kumar/Documents/vscdeExtn/precommit-ai_vscode/precommit-ai/package-lock.json"
                    );
                  }}
                />
              </>
            ) : (
              <CodeExecute />
            )}
          </div>
        </div>
        {isFilePath(code) ? (
          <FilePathWithPlay path={codeWithoutPath} />
        ) : (
          <CodeBlock code={codeWithoutPath} language={lang} theme={theme} />
        )}
      </div>
    );
  },

  // 🔥 Fixed theme handling using context inside the component
  p: ({ children }) => {
    const { theme } = useSelectTheme();
    return (
      <p
        className={`text-[14px] ${
          theme === "dark" ? "text-gray-100" : "text-gray-900"
        }`}
      >
        {children}
      </p>
    );
  },

  ul: ({ children }) => {
    const { theme } = useSelectTheme();
    return (
      <ul
        className={`list-disc list-outside pl-5 mb-2 ${
          theme === "dark" ? "text-gray-100 bg-[#141921]" : "text-gray-900"
        }`}
      >
        {children}
      </ul>
    );
  },

  ol: ({ children }) => {
    const { theme } = useSelectTheme();
    return (
      <ol
        className={`list-decimal list-outside pl-5 mb-2 ${
          theme === "dark" ? "text-gray-100 bg-[#141921]" : "text-gray-900"
        }`}
      >
        {children}
      </ol>
    );
  },

  li: ({ children }) => {
    const { theme } = useSelectTheme();
    return (
      <li
        className={`mb-1 leading-relaxed ${
          theme === "dark" ? "text-gray-100 bg-[#141921]" : "text-gray-900"
        }`}
      >
        {children}
      </li>
    );
  },
};
