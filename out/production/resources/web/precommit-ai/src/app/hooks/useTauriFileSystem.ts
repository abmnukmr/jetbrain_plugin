// src/hooks/useTauriFileSystem.ts
import { writeFile, readTextFile, BaseDirectory } from '@tauri-apps/plugin-fs';
import{strToUint8} from '../util/util';

interface WriteOptions {
  path: string;
  contents:  string;
  directory?: BaseDirectory;
}

interface ReadOptions {
  path: string;
  directory?: BaseDirectory;
}

interface FSResult<T = undefined> {
  success: boolean;
  contents?: T;
  error?: unknown;
}

export const useTauriFileSystem = () => {
  /**
   * Write UTF‑8 text to a file in the given directory
   */
  const writeToFile = async ({
    path,
    contents,
    directory = BaseDirectory.Document,
  }: WriteOptions): Promise<FSResult> => {
    try {
      await writeFile(path, strToUint8(contents), { baseDir: directory });
      return { success: true };
    } catch (error) {
      console.error('Write error:', error);
      return { success: false, error };
    }
  };

  /**
   * Read UTF‑8 text from a file
   */
  const readFromFile = async ({
    path,
    directory = BaseDirectory.Document,
  }: ReadOptions): Promise<FSResult<string>> => {
    try {
      const contents = await readTextFile(path, { baseDir: directory });
      return { success: true, contents };
    } catch (error) {
      console.error('Read error:', error);
      return { success: false, error };
    }
  };

  return { writeToFile, readFromFile };
};
