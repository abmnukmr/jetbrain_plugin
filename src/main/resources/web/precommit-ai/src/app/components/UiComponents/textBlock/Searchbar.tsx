import { useSelectTheme } from "@/app/context/SelectThemeContext";
import { CirclePlus } from "lucide-react";
import { useState } from "react";

interface SearchBarProps {
  onClick: () => void;
}

const SearchBar: React.FC<SearchBarProps> = ({ onClick }) => {
  const { theme } = useSelectTheme();
  const [showSearchbar, setShowSearchbar] = useState(false);

  const isDark = theme === "dark";

  return (
    <>
      <button
        type="button"
        onClick={() => {
          onClick();
          setShowSearchbar(!showSearchbar);
        }}
        className={`flex cursor-pointer items-center justify-center rounded-full w-5 h-5 transition-transform duration-100 ease-in-out transform ${
          showSearchbar ? "rotate-45" : "rotate-0"
        } ${
          isDark
            ? "bg-white hover:bg-gray-200 text-black"
            : "bg-gray-700 hover:bg-gray-800 text-white"
        }`}
        title="Send"
      >
        <CirclePlus size={16} />
      </button>

      {false && (
        <div
          className={`mt-2 p-1 rounded-md shadow-md w-[50vw] border ${
            isDark
              ? "bg-gray-800 text-white placeholder-gray-400 border-gray-600"
              : "bg-gray-100 text-black placeholder-gray-600 border-gray-300"
          }`}
        >
          <input
            type="text"
            placeholder="Search..."
            className={`w-full text-sm resize-none focus:outline-none ${
              isDark
                ? "bg-gray-800 text-white placeholder-gray-400"
                : "bg-gray-100 text-black placeholder-gray-600"
            }`}
          />
        </div>
      )}
    </>
  );
};

export default SearchBar;
