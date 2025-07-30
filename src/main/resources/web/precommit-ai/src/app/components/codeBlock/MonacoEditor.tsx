'use client';

import React, { useRef, useEffect, useState, useCallback } from 'react';
import Editor, { DiffEditor, OnMount, BeforeMount, useMonaco, } from '@monaco-editor/react';
import { useSelectTheme } from '@/app/context/SelectThemeContext';
import { useFileReaderWriter } from '@/app/context/FileReadWriteContext';
import MonacoTabs from './MonacoTabs';

type fileContentDetails = {

    fileName?: string,
    fileContent?: string

}

export const MonacoEditor = () => {

    const monaco = useMonaco();
    const [code, setCode] = useState<string>('')

    const [tabContent, setTabContent] = useState<{ [key: string]: fileContentDetails }>({})
    const [activeId, setActiveId] = useState<string>('')
    const { fileContent, lastSavedPath, writeFile } = useFileReaderWriter();
    const [tabHistory, setTabHistory] = useState<string[]>([]);


    const { theme } = useSelectTheme();
    useEffect(() => {
        if (monaco) {

            // Enable real-time updates for JS/TS
            monaco.languages.typescript.javascriptDefaults.setEagerModelSync(true);
            monaco.languages.typescript.typescriptDefaults.setEagerModelSync(true);

            monaco.languages.typescript.javascriptDefaults.setCompilerOptions({
                target: monaco.languages.typescript.ScriptTarget.ES2020,
                allowNonTsExtensions: true,
            });

            monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
                validate: true,
                allowComments: true,
            });

            monaco.languages.css.cssDefaults.setDiagnosticsOptions({
                validate: true,
            });



            monaco.editor.defineTheme('github-light', {
                base: 'vs',
                inherit: true,
                rules: [
                    { token: '', foreground: '24292e' },
                    { token: 'comment', foreground: '6a737d', fontStyle: 'italic' },
                    { token: 'keyword', foreground: 'd73a49' },
                    { token: 'identifier', foreground: '24292e' },
                    { token: 'string', foreground: '032f62' },
                    { token: 'number', foreground: '005cc5' },
                    { token: 'type', foreground: '005cc5' },
                    { token: 'function', foreground: '6f42c1' },
                ],
                colors: {
                    'editor.background': '#ffffff',
                    'editor.foreground': '#24292e',
                    'editor.lineHighlightBackground': '#f6f8fa',
                    'editorCursor.foreground': '#24292e',
                    'editorLineNumber.foreground': '#d1d5da',
                    'editorLineNumber.activeForeground': '#24292e',
                    'editorIndentGuide.background': '#e1e4e8',
                    'editor.selectionBackground': '#c8e1ff',
                },
            });

            monaco.editor.defineTheme('github-dark', {
                base: 'vs-dark',
                inherit: true,
                rules: [
                    { token: '', foreground: 'c9d1d9' },
                    { token: 'comment', foreground: '8b949e', fontStyle: 'italic' },
                    { token: 'keyword', foreground: 'ff7b72' },
                    { token: 'identifier', foreground: 'c9d1d9' },
                    { token: 'string', foreground: 'a5d6ff' },
                    { token: 'number', foreground: '79c0ff' },
                    { token: 'type', foreground: 'ffa657' },
                    { token: 'function', foreground: 'd2a8ff' },
                ],
                colors: {
                    'editor.background': '#0d1117',
                    'editor.foreground': '#c9d1d9',
                    'editor.lineHighlightBackground': '#161b22',
                    'editorCursor.foreground': '#58a6ff',
                    'editorLineNumber.foreground': '#484f58',
                    'editorLineNumber.activeForeground': '#c9d1d9',
                    'editorIndentGuide.background': '#21262d',
                    'editor.selectionBackground': '#264f78',
                },
            });
            const activeTheme = theme == 'dark' ? 'github-dark' : 'github-light';
            monaco.editor.setTheme(activeTheme);

        }
    }, [monaco, theme]);

    useEffect(() => {

        setCode(fileContent ?? '');
    }, [fileContent]);

    useEffect(() => {
        setActiveId(lastSavedPath!);

        setTabContent(prev => ({
            ...prev,
            [lastSavedPath!]: {
                fileContent,
                fileName: lastSavedPath?.split('/').pop() || 'Untitled',
            }
        }));
    }, [lastSavedPath])

    const saveFile = useCallback(async () => {
        if (!lastSavedPath) return;
        await writeFile(lastSavedPath, code);
    }, [lastSavedPath, code, writeFile]);

    /* Ctrl/⌘+S keyboard shortcut */
    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
                e.preventDefault();
                saveFile();
            }
        };
        window.addEventListener('keydown', handler);
        return () => window.removeEventListener('keydown', handler);
    }, [saveFile]);

    const handleEditorWillMount: BeforeMount = (monaco) => {
        // Configure before the editor is mounted
        // monaco.languages.typescript.javascriptDefaults.setEagerModelSync(true);
    };

    const handleEditorDidMount: OnMount = (editor, monaco) => {
        // Store Monaco instance for later use
        //monacoRef.current = monaco;
    };
    const tabSelectHandler = (id: string) => {
        setActiveId(id);
        setCode(tabContent[id]?.fileContent || '');

        setTabHistory((prev) => {
            const newHistory = prev.filter((tabId) => tabId !== id);
            return [...newHistory, id];
        });
    };


const closeTabHandler = (id: string) => {
    setTabContent((prevTabs) => {
        const newTabs = { ...prevTabs };
        delete newTabs[id];

        setTabHistory((prevHistory) => {
            const newHistory = prevHistory.filter((tabId) => tabId !== id);
            const nextActiveId = newHistory[newHistory.length - 1] || '';

            setActiveId(nextActiveId);
            setCode(nextActiveId ? newTabs[nextActiveId]?.fileContent || '' : '');

            return newHistory;
        });

        return newTabs;
    });
};



    return (
        <>
            <MonacoTabs tabContent={tabContent} activeTabId={activeId} onCloseTab={closeTabHandler} onTabSelect={tabSelectHandler} />
            <Editor
                height="100vh"
                defaultLanguage="java"
                value={code}
                onChange={(value) => setCode(value || '')}
                beforeMount={handleEditorWillMount}
                onMount={handleEditorDidMount}
            />
            {/* <DiffEditor
      height="90vh"
      original='original'
      modified="oo"
    
      beforeMount={handleEditorWillMount}
      onMount={handleEditorDidMount}
    
    /> */}
        </>
    );
}
