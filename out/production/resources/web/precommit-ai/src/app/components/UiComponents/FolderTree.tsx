"use client";
import { useEffect, useState, useCallback, memo } from "react";
import { invoke } from "@tauri-apps/api/core";
import { ChevronDown, ChevronRight, FolderSymlink } from "lucide-react";
import { open } from '@tauri-apps/plugin-dialog';
import Image from "next/image";
import {
  getMaterialFolderIcon,
  getMaterialFileIcon,
} from "file-extension-icon-js";

import { useSelectTheme } from "@/app/context/SelectThemeContext";
import { useSelectDirectory } from "@/app/context/SelectDirectoryContext";
import { useFileReaderWriter } from "@/app/context/FileReadWriteContext";


type FsEntry = {
  name: string;
  path: string;
  is_dir: boolean;
  children?: FsEntry[];
};

/* ---------- FolderTree (container) ---------- */
export const FolderTree = ({ root }: { root: string | null }) => {
  const [tree, setTree] = useState<FsEntry[]>([]);
  const [error, setError] = useState<string | null>(null);
  /** map<Path, isOpen> — lives here so nodes keep their open/closed
      state even when the component re‑renders */
  const [openMap, setOpenMap] = useState<Record<string, boolean>>({});

  const sortTree = (nodes: FsEntry[]): FsEntry[] => {
    return nodes
      .slice()
      .sort((a, b) => {
        // 1. Folders before files
        if (a.is_dir !== b.is_dir) {
          return a.is_dir ? -1 : 1;
        }
        // 2. Alphabetical by name (case-insensitive)
        return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
      })
      .map((node) => ({
        ...node,
        children: node.children ? sortTree(node.children) : undefined,
      }));
  };

  /* toggle helper passed down to TreeNode */
  const toggle = useCallback((path: string) => {
    setOpenMap(m => ({ ...m, [path]: !m[path] }));
  }, []);

  const { isSaving } = useFileReaderWriter();

  useEffect(() => {
    if (!root) {
      setError("No root directory provided.");
      return;
    }

    setError(null);
    setOpenMap({});

    invoke<FsEntry[]>("read_dir_recursive", { start: root })
      .then((data) => {
        const sorted = sortTree(data);   // ✨ sort the tree
        setTree(sorted);
      })
      .catch((e) => {
        setError(String(e));
        console.error("Error fetching folder tree:", e);
      });
  }, [root, isSaving]);

  const { selectedDirectory, setSelectedDirectory } = useSelectDirectory();
  const { theme } = useSelectTheme();

  /* ----------- handlers ----------- */
  const pickDirectory = async () => {
    const path = await open({ directory: true, multiple: false });
    if (typeof path === 'string') setSelectedDirectory(path);
  };

  const SelectDirectory = () => <div>
    <button
      onClick={pickDirectory}
      className={`p-2 m-4 cursor-pointer ${theme === 'dark' ? 'text-gray-900 bg-gray-100' : 'bg-[#0a0a0a] text-white'
        } rounded text-black dark:text-gray-200 transition flex items-center gap-1`}
      title={selectedDirectory ?? 'Select project path'}
    >
      <FolderSymlink scale={1} color={theme === 'dark' ? 'black' : 'white'} />
      {selectedDirectory ? (
        <span className={`text-sm ${theme === 'dark' ? 'text-gray-800' : 'text-gray-200'}`}>{selectedDirectory}</span>
      ) : (
        <span className={`text-sm ${theme === 'dark' ? 'text-gray-800' : 'text-gray-200'}`}>Open Project</span>
      )}

    </button>
  </div>;

  const DirectoryNode: FsEntry = {
    name: selectedDirectory?.split('/').pop() || '',
    path: selectedDirectory || '',
    is_dir: true,
    children: tree
  }
  if (error) return <SelectDirectory />
  if (!tree.length) return <p>Loading&hellip;</p>;

  return (
    <div className="font-mono text-sm">
      <TreeNode node={DirectoryNode} depth={0} openMap={openMap} toggle={toggle} />

    </div>
  );
};

/* ---------- TreeNode (recursive) ---------- */
interface TreeNodeProps {
  node: FsEntry;
  depth: number;
  openMap: Record<string, boolean>;
  toggle: (path: string) => void;
}



/* memo prevents needless re‑renders when sibling nodes change */
const TreeNode = memo(({ node, depth, openMap, toggle }: TreeNodeProps) => {
  const isOpen = openMap[node.path] ?? depth === 0;   // root folders open by default
  const indent = { marginLeft: depth * 8 };

  const { openFile } = useFileReaderWriter();
  /* choose an icon that “rotates” when open/closed */
  const icon = node.is_dir ? (
    <span className="inline-flex items-center gap-1">
      {isOpen ? <ChevronDown size={15} /> : <ChevronRight size={15} />}
      <Image
        src={getMaterialFolderIcon(node.name)}
        alt={node.name}
        width={20}
        height={20}
        className="shrink-0"
      />
    </span>
  ) : (
    <Image
      src={getMaterialFileIcon(node.name)}
      alt={node.name}
      width={15}
      height={15}
      className="inline-block shrink-0"
    />
  );

  const onClickonNodeHandler = (node: FsEntry) => node.is_dir ? toggle(node.path) : openFile(node?.path)


  return (
    <div style={indent}>
      <div
        onClick={() => onClickonNodeHandler(node)}
        className={
          `cursor-pointer select-none flex items-center gap-1 font-sans ` +
          (node.is_dir ? '' : '')
        }
      >
        {icon} <span>{node.name}</span>
      </div>


      {isOpen && node.children?.map(child => (
        <TreeNode
          key={child.path}
          node={child}
          depth={depth + 1}
          openMap={openMap}
          toggle={toggle}
        />
      ))}
    </div>
  );
});
TreeNode.displayName = "TreeNode";

