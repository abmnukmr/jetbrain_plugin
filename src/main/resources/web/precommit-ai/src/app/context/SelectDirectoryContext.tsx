"use client";
import React, { createContext, useContext, useState, ReactNode } from 'react';

interface SelectDirectoryContextType {
  selectedDirectory: string | null;
  setSelectedDirectory: (path: string | null) => void;
}

const SelectDirectoryContext = createContext<SelectDirectoryContextType | undefined>(undefined);

export const SelectDirectoryProvider = ({ children }: { children: ReactNode }) => {
  const [selectedDirectory, setSelectedDirectory] = useState<string | null>(null);

  return (
    <SelectDirectoryContext.Provider value={{ selectedDirectory, setSelectedDirectory }}>
      {children}
    </SelectDirectoryContext.Provider>
  );
};

export const useSelectDirectory = () => {
  const context = useContext(SelectDirectoryContext);
  if (!context) {
    throw new Error('useSelectDirectory must be used within a SelectDirectoryProvider');
  }
  return context;
};
