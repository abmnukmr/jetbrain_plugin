export const strToUint8 = (s: string) => new TextEncoder().encode(s);
export const uint8ToStr = (u: Uint8Array) => new TextDecoder().decode(u);
export const isValidJson = (str: string) => {
  try {
    JSON.parse(str);
    return true;
  } catch (e) {
    return false;
  }
};

export const isValidYaml = (str: string) => {
  try {
    // A simple check for YAML validity
    return str.trim().length > 0 && !str.includes("undefined");
  } catch (e) {
    return false;
  }
};

export const isTreeStructure = (code: string) =>
  code.includes("├──") || code.includes("│") || code.includes("└──");

/**
 * Extracts the first file path from a code string.
 * @param code - The code string to search.
 * @returns The first file path found, or null if none is found.
 */
export const extractFirstFilePath = (code: string): string | null => {
  const match = code.match(/\/(?:[^\s*]+\/)*[^\s*]+\.\w+/);
  return match ? match[0] : null;
};

export const isFilePath = (code: string): boolean => {
  const filePathRegex = /^\s*\/(?:[^\/\s]+\/)*[^\/\s]+\.\w+\s*$/;
  return filePathRegex.test(code);
};


type TreeNode = {
  [key: string]: TreeNode | null; // null means it's a file, not a folder
};

export const buildFileTree = (paths: string[]): TreeNode => {
  const root: TreeNode = {};

  paths.forEach(path => {
    const parts = path.split("/");
    let current = root;

    parts.forEach((part, i) => {
      const isFile = i === parts.length - 1;
      if (!current[part]) {
        current[part] = isFile ? null : {};
      }
      if (!isFile && current[part]) {
        current = current[part] as TreeNode;
      }
    });
  });

  return root;
}

export const  renderTree = (node: TreeNode, prefix = ""): string => {
  const entries = Object.entries(node).sort(([a], [b]) => a.localeCompare(b));
  return entries
    .map(([name, child], index) => {
      const isLast = index === entries.length - 1;
      const branch = `${prefix}${isLast ? "└── " : "├── "}${name}`;
      const children =
        child && renderTree(child, prefix + (isLast ? "    " : "│   "));
      return children ? `${branch}\n${children}` : branch;
    })
    .join("\n");
}




