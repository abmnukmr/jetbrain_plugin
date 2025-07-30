import React, { createContext, useContext, useState, ReactNode } from "react";
import  {useTauriFileSystem}  from "../hooks/useTauriFileSystem";

type FileWriteContextType = {
  writeFile: (filePath: string, content: string) => Promise<void>;
  openFile:(filePath: string) => Promise<string|undefined>;
  fileContent: string;
  isSaving: boolean;
  error: string | null;
  lastSavedPath: string | null;
};

const FileWriteContext = createContext<FileWriteContextType | undefined>(undefined);

export const FileWriteProvider = ({ children }: { children: ReactNode }) => {
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastSavedPath, setLastSavedPath] = useState<string | null>(null);
  const [fileContent, setFileContent] = useState<string>(String(''));

  const { writeToFile, readFromFile } = useTauriFileSystem();

  const writeFile = async (filePath: string, content: string) => {
    setIsSaving(true);
    setError(null);
    try {
      await writeToFile({
        path: filePath,
        contents: content,
      })
      setFileContent(String(content));
      setLastSavedPath(filePath);
    } catch (err: any) {
      setError(err.message || "Unknown error");
    } finally {
      setIsSaving(false);
    }
  };
  const openFile = async (filePath: string) => {
    console.log(filePath)
    setError(null);
    try {
      const {contents}= await readFromFile ({
        path: filePath,
      })
      console.log(contents)
      setFileContent(String(contents));
      setLastSavedPath(filePath);
      return contents;
    } catch (err: any) {
      setError(err.message || "Unknown error");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <FileWriteContext.Provider value={{ writeFile, openFile, fileContent, isSaving, error, lastSavedPath }}>
      {children}
    </FileWriteContext.Provider>
  );
};

export const useFileReaderWriter = () => {
  const context = useContext(FileWriteContext);
  if (!context) {
    throw new Error("useFileReaderWriter must be used within a FileWriteProvider");
  }
  return context;
};
