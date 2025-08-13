'use client';

import { useEffect, useRef, useMemo } from 'react';
import 'xterm/css/xterm.css';
import { listen } from '@tauri-apps/api/event';
import { invoke } from '@tauri-apps/api/core';
import { useSelectDirectory } from '@/app/context/SelectDirectoryContext';
import { useSelectTheme } from '@/app/context/SelectThemeContext';

export const TerminalLayout = () => {
    const terminalRef = useRef<HTMLDivElement | null>(null);
    const termRef = useRef<any>(null);
    const fitRef = useRef<any>(null);
    const observerRef = useRef<ResizeObserver | null>(null);
    const unlistenRef = useRef<() => void | null>(null);

    const { selectedDirectory } = useSelectDirectory();
    const { theme } = useSelectTheme();

    const terminalTheme = useMemo(() => {
        return theme === 'dark'
            ? {
                background: '#000000',
                foreground: '#ffffff',
                cursor: '#ffffff',
                black: '#000000',
                red: '#ff0000',
                green: '#33ff00',
                yellow: '#ffff00',
                blue: '#0066ff',
                magenta: '#cc00ff',
                cyan: '#00ffff',
                white: '#d0d0d0',
                brightBlack: '#808080',
                brightRed: '#ff0000',
                brightGreen: '#33ff00',
                brightYellow: '#ffff00',
                brightBlue: '#0066ff',
                brightMagenta: '#cc00ff',
                brightCyan: '#00ffff',
                brightWhite: '#ffffff',
            }
            : {
                background: '#000000',
                foreground: '#fffff',
                cursor: '#ffffff',
            };
    }, [theme]);

    useEffect(() => {
        let mounted = true;

        const initTerminal = async () => {
            const [{ Terminal }] = await Promise.all([
                import('xterm'),
                
            ]);

            const term = new Terminal({
                theme: terminalTheme,
                fontFamily: 'Menlo, monospace',
                fontSize: 13,
                lineHeight: 1,
                cursorBlink: true,
                cursorStyle: 'bar',
                convertEol: false,
                windowsMode: true,
            });

            term.open(terminalRef.current!);
           
            termRef.current = term;
           

            // Output
            const unlisten = await listen<string>('terminal-output', (event) => {
                term.write(event.payload);
                if (event.payload.includes('\n')) {
                    term.scrollToBottom();
                }
            });
            unlistenRef.current = unlisten;

            // Input
            term.onData((data) => {
                invoke('terminal_input', { input: data });
            });

            // Initial working directory
            if (selectedDirectory) {
                invoke('terminal_input', {
                    input: `cd "${selectedDirectory}"`,
                });
            }
        };

        initTerminal();

        const handleResize = () => {
            requestAnimationFrame(() => {
                fitRef.current?.fit();
            });
        };

        window.addEventListener('resize', handleResize);

        return () => {
            termRef.current?.dispose();
            fitRef.current = null;
            termRef.current = null;
            observerRef.current?.disconnect();
            unlistenRef.current?.();
           /// window.removeEventListener('resize', handleResize);
        };
    }, []);


    // Update directory when selectedDirectory changes
    useEffect(() => {
        if (selectedDirectory && termRef.current) {
            invoke('terminal_input', {
                input: `cd "${selectedDirectory}" && clear\n`,
            });
        }
    }, [selectedDirectory]);

    return (
        <div className="flex flex-col flex-1 h-full w-full overflow-hidden bg-black" style={{ minHeight: '160px', width: '100%', height: '370px', overflow: 'scroll' , bottom:'0px'}}>
            <div ref={terminalRef}  className="flex-1 w-full h-full overflow-hidden" style={{height:'120px'}}  />
        </div>
    );
};
