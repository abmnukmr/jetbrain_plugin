// FloatingIcon.tsx
import { BetweenHorizontalStart } from "lucide-react";
import React from "react";

interface FloatingIconProps {
  top?: number;
  left?: number;
  text?: string;
  onClick: () => void;
}

const FloatingIcon: React.FC<FloatingIconProps> = ({
  top,
  left,
  text,
  onClick,
}) => {
  return (
    <button
      onClick={onClick}
      style={{
        top,
        left,
      }}
      className="cursor-pointer top-2 text-xs  text-white px-2 py-1 rounded "
    >
      <BetweenHorizontalStart />
    </button>
  );
};

export default FloatingIcon;
