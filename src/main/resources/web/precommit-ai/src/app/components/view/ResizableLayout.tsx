'use client';

import {
    Panel,
    PanelGroup,
    PanelResizeHandle,
    
} from 'react-resizable-panels';

import { FolderTree } from '../../components/UiComponents/FolderTree';
import { MonacoEditor } from '../../components/codeBlock/MonacoEditor';
import CodeGuide from '../../components/CodeGuide';
import { useSelectDirectory } from '../../context/SelectDirectoryContext';
import { TerminalLayout } from './TerminalLayout';
import { useEffect, useState } from 'react';
import FloatingIcon from '../UiComponents/FloatingIcon';

export const ResizableLayout = () => {
    const { selectedDirectory } = useSelectDirectory();
  const [selectedText, setSelectedText] = useState("");
  const [position, setPosition] = useState<{ x: number; y: number } | null>(
    null
  );


    function saveSelection() {
      const sel = window.getSelection();
      if (sel && sel.rangeCount > 0) {
        return sel.getRangeAt(0).cloneRange();
      }
      return null;
    }
    
    function restoreSelection(range: Range | null) {
      if (range) {
        const sel = window.getSelection();
        sel?.removeAllRanges();
        sel?.addRange(range);
      }
    }
    
    
      const onSelectstart =()=>{
         setSelectedText("");
      }  
    
      const onSelectEnd = () => {
        const savedRange = saveSelection();
        const selection = window.getSelection();
        if (!selection || selection.isCollapsed) return;
    
        const text = selection.toString();
        const range = selection.getRangeAt(0);
        const rect = range.getBoundingClientRect();
        if (!text || !rect) return;
    
        setSelectedText(text);
        setPosition({
          x: rect.left + rect.width / 2 - 80 / 2,
          y: rect.top + window.scrollY,
        });
        setTimeout(() => {
          restoreSelection(savedRange);
        }, 0);
      };
    
      useEffect(() => {
        document.addEventListener("selectstart", onSelectstart);
        document.addEventListener("mouseup", onSelectEnd);
        
    
        return () =>{ document.removeEventListener("selectstart", onSelectstart)
           document.removeEventListener("mouseup", onSelectEnd);}
      }, []);
    

    return (
      <div className="h-screen w-full flex">
        {selectedText && position && (
          <div
            className="absolute z-[1000] w-[80px] h-[30px] bg-black text-white text-xs rounded flex items-center justify-center"
            style={{
              zIndex: 1200,
              transform: `translate3d(${position.x}px, ${position.y}px, 0)`,
            }}
          >
            <FloatingIcon onClick={()=>{}} />
          </div>
        )}
        <PanelGroup
          direction="horizontal"
          autoSaveId="persistence"
          className="w-full h-full"
        >
          {/* Left Panel - File Tree */}
          {/* <Panel defaultSize={15} minSize={10} className="h-full">
                    <div className="h-full overflow-y-auto border-r border-[1px] border-gray-400/10">
                        <FolderTree root={selectedDirectory} />
                    </div>
                </Panel>

                <PanelResizeHandle className="w-[1px] bg-gray-200/30 hover:bg-gray-300 transition-colors duration-150 cursor-col-resize" /> */}

          {/* Center Panel - Vertical Group (Editor + Terminal) */}

          {/* <Panel defaultSize={60} minSize={30} className="h-full">
                    <PanelGroup autoSaveId="persistence"  direction="vertical" className="h-full w-full">
                        <Panel defaultSize={65} minSize={20}>
                            <div className="h-full overflow-hidden">
                                <MonacoEditor />
                            </div>
                        </Panel>

                        <PanelResizeHandle className="h-[1px] bg-gray-200/30 hover:bg-gray-300 transition-colors duration-150 cursor-row-resize"  />

                        <Panel defaultSize={35}>
                            <div className="flex overflow-hidden">
                                 <TerminalLayout /> 
                            </div>
                        </Panel>

                    </PanelGroup>
                </Panel>

                <PanelResizeHandle className="w-[1px] bg-gray-200/30 hover:bg-gray-300 transition-colors duration-150 cursor-col-resize" />
 */}

          {/* Right Panel - Code Guide */}
          <Panel defaultSize={25} minSize={10}>
            <div className="h-full overflow-y-auto border-l border-[1px] border-gray-400/10">
              <CodeGuide />
            </div>
          </Panel>
        </PanelGroup>
      </div>
    );
};
