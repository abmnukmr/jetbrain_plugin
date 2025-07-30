import { GitCompare } from "lucide-react";

export const  GitCompareButton = ({ onClick }: { onClick: () => void }) => {
  return (
    <button
      onClick={onClick}
      className="cursor-pointer top-2 text-xs bg-gray-700 text-white px-2 py-1 rounded hover:bg-gray-600"
    >
      <GitCompare className="h-4 w-4" strokeWidth={1} />
    </button>
  );
};