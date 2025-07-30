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
