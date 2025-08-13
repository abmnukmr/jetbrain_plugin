 import { Play } from 'lucide-react';

const FilePathWithPlay = ({ path }: { path: string}) => {
 
  return (
    <span
      className="bg-gray-700 px-2 py-0.5 rounded font-mono text-sm text-white overflow-x-auto whitespace-nowrap inline-block max-w-full"
      style={{ direction: 'rtl', textAlign: 'left' }}
      tabIndex={0}
    >
      <span style={{ direction: 'ltr', display: 'inline-block', minWidth: '100%' }}>
      {path}
      </span>
    </span>
  );
};


export default FilePathWithPlay;
