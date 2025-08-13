import React from "react";

type GradientFileIconProps = {
  filename: string;
  gradient?: string; // Optional custom gradient
  size?: string;     // Font size (e.g., '1rem', '2xl')
};

const GradientFileIcon: React.FC<GradientFileIconProps> = ({
  filename,
  gradient = "linear-gradient(to right, #4facfe, #00f2fe)",
  size = "0.75rem",
}) => {
  return (
    <span
      style={{
        padding:"8px",
        fontSize: size,
        fontWeight: "bold",
        background: gradient,
        WebkitBackgroundClip: "text",
        WebkitTextFillColor: "transparent",
        display: "inline-block",
      }}
    >
      📄 {filename}
    </span>
  );
};

export default GradientFileIcon;
