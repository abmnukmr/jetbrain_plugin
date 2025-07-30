'use client';

import React, { useEffect, useState } from 'react';
import DiffViewer from 'react-diff-viewer-continued';
import { createHighlighter } from 'shiki';
import type { Highlighter } from 'shiki';
import { useSelectTheme } from '../context/SelectThemeContext';




  const oldCode = `package com.example.springbootmysqlrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SpringbootMysqlRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootMysqlRestApplication.class, args);
    }

}`;

  const newCode = `package com.example.springbootmysqlrest;
    package com.example.springbootmysqlrest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringbootMysqlRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootMysqlRestApplication.class, args);
    }

}`;

const CodeDiffWithShiki = ({oldCode, newCode}:{oldCode:string, newCode:string}) => {
  const [oldHighlighted, setOldHighlighted] = useState('');
  const [newHighlighted, setNewHighlighted] = useState('');
  const [loading, setLoading] = useState(true);

  const { theme  } = useSelectTheme(); // ← grab current theme


  useEffect(() => {
    const loadHighlighter = async () => {
      const highlighter: Highlighter = await createHighlighter({
        themes: ['github-dark-high-contrast', 'github-light-high-contrast'],
        langs: ['javascript', 'typescript', 'python', 'java', 'csharp', 'go', 'ruby', 'php', 'html', 'css', 'json', 'bash', 'xml', 'txt', 'plaintext', 'properties', 'dockerfile'],
      });

      const oldHighlighted = highlighter.codeToHtml(oldCode, {
        lang: 'javascript',
        theme: theme === 'dark' ? 'github-dark-high-contrast' : 'github-light-high-contrast',
      });

      const newHighlighted = highlighter.codeToHtml(newCode, {
        lang: 'javascript',
        theme: theme === 'dark' ? 'github-dark-high-contrast' : 'github-light-high-contrast',
      });

      setOldHighlighted(oldHighlighted);
      setNewHighlighted(newHighlighted);
      setLoading(false);
    };

    loadHighlighter();
  }, [theme]);


  const defaultStyles = {
    variables: {
      dark: {
        diffViewerBackground: '#0a0c10',
        diffViewerColor: '#c9d1d9',

        addedBackground: '#033a16',
        addedColor: '#56d364',
        removedBackground: '#340c0c',
        removedColor: '#f85149',
        wordAddedBackground: '#0f5323',
        wordRemovedBackground: '#7d1a1a',

        addedGutterBackground: '#161b22',
        removedGutterBackground: '#161b22',
        gutterBackground: '#0d1117',
        gutterBackgroundDark: '#161b22',
        gutterColor: '#8b949e',
        addedGutterColor: '#56d364',
        removedGutterColor: '#f85149',

        codeFoldGutterBackground: '#21262d',
        codeFoldBackground: '#161b22',
        codeFoldContentColor: '#8b949e',

        highlightBackground: '#264f78',
        highlightGutterBackground: '#2d4077',

        diffViewerTitleBackground: '#161b22',
        diffViewerTitleColor: '#c9d1d9',
        diffViewerTitleBorderColor: '#30363d',

        emptyLineBackground: '#000',
      },

      light: {
        diffViewerBackground: '#ffffff',
        diffViewerColor: '#24292e',

        addedBackground: '#e6ffed',
        addedColor: '#22863a',
        removedBackground: '#ffeef0',
        removedColor: '#cb2431',
        wordAddedBackground: '#acf2bd',
        wordRemovedBackground: '#fdb8c0',

        addedGutterBackground: '#cdffd8',
        removedGutterBackground: '#ffdce0',
        gutterBackground: '#f6f8fa',
        gutterBackgroundDark: '#e1e4e8',
        gutterColor: '#6a737d',
        addedGutterColor: '#22863a',
        removedGutterColor: '#cb2431',

        codeFoldGutterBackground: '#f6f8fa',
        codeFoldBackground: '#e1e4e8',
        codeFoldContentColor: '#586069',

        highlightBackground: '#fffbdd',
        highlightGutterBackground: '#fff5b1',

        diffViewerTitleBackground: '#f6f8fa',
        diffViewerTitleColor: '#24292e',
        diffViewerTitleBorderColor: '#d1d5da',

        emptyLineBackground: '#ffffff',
      },
    },

    gutter: {
      position: 'sticky',
      left: 0,
      zIndex: 2,
    },
  };


  if (loading) return <div>Loading...</div>;

  return (
    <div className={`relative w-full my-2 rounded-lg border ${theme === 'dark' ? 'border-gray-700' : 'border-gray-300'}`}>
      <div className="overflow-x-auto w-full rounded-lg">
        <DiffViewer
          styles={defaultStyles}
          oldValue={oldHighlighted}
          extraLinesSurroundingDiff={0}
          newValue={newHighlighted}
          splitView={false}
          useDarkTheme={theme === 'dark'}
          showDiffOnly={false}
          disableWordDiff={true}
          renderContent={(str) => (
            <div
              className="shiki-render min-w-max"
              dangerouslySetInnerHTML={{ __html: str }}
            />
          )}
        />
      </div>
    </div>


  );
};

export default CodeDiffWithShiki;
