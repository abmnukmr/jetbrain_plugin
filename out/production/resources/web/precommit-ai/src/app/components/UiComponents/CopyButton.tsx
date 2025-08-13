import { useSendToVsCode } from "@/app/hooks/useSentoVsCode";
import { CheckIcon, Copy } from "lucide-react";
import { useState } from "react";

export const CopyButton = ({
  code,
  onClick,
}: {
  code: string;
  onClick: (e: React.MouseEvent<HTMLButtonElement>) => void;
}) => {
  const [copied, setCopied] = useState(false);
  const sendToVscode = useSendToVsCode();

  const handleCopy = () => {
    // navigator.clipboard.writeText(code).then(() => {
    //   setCopied(true);
    //   setTimeout(() => setCopied(false), 2000);
    // });
    setCopied(true);
    sendToVscode("copy", code);
  };

  return (
    <button
      onClick={(e) => {
        handleCopy();
        onClick(e);
      }}
      className="cursor-pointer top-2 text-xs bg-gray-700 text-white px-2 py-1 rounded hover:bg-gray-600"
    >
      {copied ? (
        <CheckIcon className="h-4 w-4 text-green-400" />
      ) : (
        <Copy strokeWidth={1} className="h-4 w-4" />
      )}
    </button>
  );
};
