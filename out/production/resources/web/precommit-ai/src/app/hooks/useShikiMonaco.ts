import { useEffect } from 'react';
import { setupMonacoWorkers, setupShikiForAllLanguages } from '../util/setupMonaco';

export function useShikiMonaco(theme: 'github-dark' | 'github-light') {
  useEffect(() => {
    setupMonacoWorkers();
    setupShikiForAllLanguages(theme);
  }, [theme]);
}
