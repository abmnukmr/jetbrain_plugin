import { CheckIcon, Copy, Play } from "lucide-react";
import { useState } from "react";

export const CodeExecute = ({
  code,
  onClick,
}: {
  code?: string;
  onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void;
}) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {};

  return (
    <button
      onClick={(e) => {
        onClick?.(e);
      }}
      className="absolute cursor-pointer top-2 right-1 text-xs bg-gray-700 text-white px-2 py-1 rounded hover:bg-gray-600"
    >
      {copied ? (
        <Play strokeWidth={1} className="h-4 w-4 text-green-400" />
      ) : (
        <Play
         strokeWidth={1} className="h-4 w-4" />
      )}
    </button>
  );
};
