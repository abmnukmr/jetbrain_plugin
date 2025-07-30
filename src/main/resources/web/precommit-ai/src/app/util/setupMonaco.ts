// utils/setupMonaco.ts
//import * as monaco from 'monaco-editor';
import { createHighlighter } from 'shiki';
import { shikiToMonaco } from '@shikijs/monaco';
import { useRef } from 'react';

const monaco = useRef<typeof import('monaco-editor') | null>(null);
/**
 * Monaco Environment override for worker loading (fixes dynamic import error)
 */
export function setupMonacoWorkers() {
  // @ts-ignore
  self.MonacoEnvironment = {
    getWorker(_: any, label: string) {
      switch (label) {
        case 'json':
          return new Worker(new URL('monaco-editor/esm/vs/language/json/json.worker?worker', import.meta.url), { type: 'module' });
        case 'css':
        case 'scss':
        case 'less':
          return new Worker(new URL('monaco-editor/esm/vs/language/css/css.worker?worker', import.meta.url), { type: 'module' });
        case 'html':
        case 'handlebars':
        case 'razor':
          return new Worker(new URL('monaco-editor/esm/vs/language/html/html.worker?worker', import.meta.url), { type: 'module' });
        case 'typescript':
        case 'javascript':
          return new Worker(new URL('monaco-editor/esm/vs/language/typescript/ts.worker?worker', import.meta.url), { type: 'module' });
        default:
          return new Worker(new URL('monaco-editor/esm/vs/editor/editor.worker?worker', import.meta.url), { type: 'module' });
      }
    }
  };
}

/**
 * Load all Shiki-supported languages and apply to Monaco
 */
export async function setupShikiForAllLanguages(theme: 'github-light' | 'github-dark' = 'github-light') {
  const allLangs =  ['javascript', 'typescript', 'python', 'java', 'csharp', 'go', 'ruby', 'php', 'html', 'css', 'json', 'bash', 'xml', 'txt', 'plaintext', 'properties','dockerfile']
       // ~100+ languages supported by Shiki

  for (const lang of allLangs) {
    
  // if (monaco) {
  //     monaco.languages.register({ id: lang });
  // }
  }

  const highlighter = await createHighlighter({
    langs: allLangs,
    themes: [theme],
  });

  await shikiToMonaco(highlighter, monaco);
}
