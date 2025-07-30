import { useSelectTheme } from "@/app/context/SelectThemeContext";
import { CirclePlus, CircleX } from "lucide-react";
import { useState } from "react";

const SearchBar = () => {
  const { theme } = useSelectTheme();
  const [showSearchbar, setShowSearchbar] = useState<boolean>(false);

  return (
    <>
      <button
        type="button"
        onClick={() => {
          setShowSearchbar(!showSearchbar);
        }}
        className={`flex h-2 transform transition-transform duration-100 ease-in-out
          ${
            showSearchbar ? "rotate-45" : "rotate-0"
          } items-center justify-center rounded-full w-5 h-5 transition ${
          theme === "dark"
            ? "bg-white hover:bg-gray-200 text-black"
            : "bg-gray-700 hover:bg-gray-800 text-white"
        }`}
        title="Send"
      >
        <CirclePlus />
      </button>
      {false && (
        <div
          className={`border shadow-md border-gray-70 w-[0vw]text-white p-1 rounded-md w-[50vw]${
            theme === "dark"
              ? "bg-gray-800 text-white placeholder-gray-400 border-gray-600"
              : "bg-gray-100 text-black placeholder-gray-600 border-gray-300"
          }`}
        >
          <input
            type="text"
            placeholder="Search..."
            className={`flex-1 focus:outline-none focus:ring-0 w-[50vw] resize-none max-h-48   text-sm ${
              theme === "dark"
                ? "bg-gray-800 text-white placeholder-gray-400 border-gray-600"
                : "bg-gray-100 text-black placeholder-gray-600 border-gray-300"
            }`}
          />
        </div>
      )}
    </>
  );
};

export default SearchBar;
