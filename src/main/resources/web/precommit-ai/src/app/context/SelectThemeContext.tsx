'use client';

import React,{useContext, useEffect} from 'react';

const SelectThemeContext = React.createContext({
  theme: 'light' as 'light' | 'dark',
  setTheme: (theme: 'light' | 'dark') => {}
});

export const useSelectTheme = () => {

    const context = useContext(SelectThemeContext);
      if (!context) {
        throw new Error('useSelectTheme must be used within a SelectThemeProvider');
      }
      return context;
};

export const SelectThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [theme, setTheme] = React.useState<'light' | 'dark'>('light');

  useEffect(() => {
    const savedTheme = localStorage.getItem('theme') as 'light' | 'dark';
    if (savedTheme) {
      setTheme(savedTheme);
    }
  }, []);

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark');
    localStorage.setItem('theme', theme);
  }, [theme]);

  return (
    <SelectThemeContext.Provider value={{ theme, setTheme }}>
      {children}
    </SelectThemeContext.Provider>
  );
};