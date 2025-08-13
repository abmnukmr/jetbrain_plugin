import React, { useState, useEffect } from 'react';
import { useSelectTheme } from '@/app/context/SelectThemeContext';
import { X } from 'lucide-react';

type FileContentDetails = {
  id?: string;
  fileName?: string;
  fileContent?: string;
};

type MonacoTabsProps = {
  tabContent: { [key: string]: FileContentDetails };
  onTabSelect: (tabId: string) => void;
  onCloseTab: (tabId: string) => void;
  activeTabId: string;
};

const MonacoTabs = ({ tabContent, onTabSelect, onCloseTab, activeTabId }: MonacoTabsProps) => {
  const { theme } = useSelectTheme();
  
  // Auto-select the first tab
  useEffect(() => {
    const keys = Object.keys(tabContent);
    if (keys.length && !activeTabId) {
      onTabSelect(keys[0]);
    }
  }, [tabContent]);

  const handleTabClick = (id: string) => {
    onTabSelect(id);
  };

  const isDark = theme === 'dark';

  return (
    <div
      className={`flex overflow-x-auto border-b text-sm font-mono ${
        isDark ? 'border-gray-700 bg-[#1e1e1e]' : 'border-gray-300 bg-gray-50'
      }`}
    >
      <div className="flex">
        {tabContent && Object.entries(tabContent).map(([id, tab]) => {
          const isActive = activeTabId === id;

          return (
            <div
              key={id}
              className={`flex items-center px-4 py-2 mr-1 rounded-t-md cursor-pointer whitespace-nowrap flex-shrink-0
                ${
                  isActive
                    ? isDark
                      ? 'bg-[#0d1117] text-white border-t border-l border-r border-blue-500'
                      : 'bg-white text-black border-t border-l border-r border-blue-500'
                    : isDark
                      ? 'text-gray-400 hover:bg-[#2e2e2e]'
                      : 'text-gray-600 hover:bg-gray-200'
                }`}
              onClick={() => handleTabClick(id)}
            >
              <span className="truncate max-w-[150px]">{tab.fileName || 'Untitled'}</span>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onCloseTab(id);

                  // Auto-select another tab if the active one is closed
                  if (activeTabId === id) {
                    const remainingTabs = Object.keys(tabContent).filter((key) => key !== id);
                    if (remainingTabs.length) {
                      const nextTabId = remainingTabs[0];
                      onTabSelect(nextTabId);
                    } else {
                    }
                  }
                }}
                className={`ml-2 hover:text-red-500 ${isDark ? 'text-gray-500' : 'text-gray-400'}`}
                title="Close tab"
              >
                <X size={14} />
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default MonacoTabs;
