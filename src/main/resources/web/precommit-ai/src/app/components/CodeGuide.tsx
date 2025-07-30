"use client";
import React, { useEffect, useRef, useState } from "react";
import Message from "./Message";
import { useSelectTheme } from "../context/SelectThemeContext";
import {
  ArrowUpFromDot,
  CirclePlus,
  EllipsisVertical,
  MoveUp,
} from "lucide-react";
import SearchBar from "./UiComponents/textBlock/Searchbar";
import { useSentToJetBrain } from "../hooks/useSentToJetBrain"

type MessageType = { role: "user" | "assistant"; text: string };

const CodeGuide: React.FC = () => {
  const { theme } = useSelectTheme();
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [input, setInput] = useState("");

  const inputRef = useRef<HTMLTextAreaElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const [streamCompleted, setStreamCompleted] = useState(false);
  const streamRef = useRef(""); // running string from stream


  const [pluginReady, setPluginReady] = useState(false);

  const sendToPlugin = useSentToJetBrain(pluginReady)


  const appendToLastAssistant = (chunk: string) => {
    setMessages((prev) => {
      if (prev.length === 0) return prev;

      const lastIndex = prev.length - 1;
      const last = prev[lastIndex];

      if (last.role === "assistant") {
        const updated = [...prev];
        updated[lastIndex] = {
          ...last,
          text: last.text + chunk,
        };
        return updated;
      } else {
        return [...prev, { role: "assistant", text: chunk }];
      }
    });
  };

 useEffect(() => {
    if (containerRef.current) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight;
    }
  }, [messages]);


useEffect(() => {
  const handleMessage = (event: MessageEvent) => {
    const msg = event.data;

    switch (msg?.command) {
      case "pluginReady":
        console.log("Plugin is ready");
        setPluginReady(true);
        break;

      case "response":
        const chunk = msg.chunk ?? "";
        setStreamCompleted(false);

        // Accumulate the stream
        streamRef.current += chunk;

        // Append to last assistant message or create new
        setMessages((prev) => {
          const last = prev[prev.length - 1];

          if (last?.role === "assistant") {
            const updated = [...prev];
            updated[updated.length - 1] = {
              ...last,
              text: last.text + chunk,
            };
            return updated;
          }

          return [...prev, { role: "assistant", text: chunk }];
        });
        break;

      case "responseCompleted":
        setStreamCompleted(true);
        break;

      case "error":
        const errorText = msg.error ?? "Unknown error";
        setMessages((prev) => [
          ...prev,
          { role: "assistant", text: errorText },
        ]);
        streamRef.current = "";
        break;

      default:
        break; // Ignore unknown commands
    }
  };

  window.addEventListener("message", handleMessage);
  return () => window.removeEventListener("message", handleMessage);
}, []);


  const handleSend = () => {
    if (!input.trim()) return;

    const payload = input.trim();
    setMessages((prev) => [...prev, { role: "user", text: payload }]);
    setInput("");
    streamRef.current = "";
    sendToPlugin("generate", payload)
  };





   useEffect(() => {
      if (!pluginReady || typeof window.cefQuery !== "function") return;

      window.cefQuery({
        request: JSON.stringify({
          type: "preCommit-ai",
          payload: "Hi plugin ",
        }),
        onSuccess: (response: string) => {
          console.log("Plugin responded: ", response);
        },
        onFailure: (code: number, msg: string) => {
          console.error(`Plugin error (${code}):`, msg);
        },
      });
    }, [pluginReady]);

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInput(e.target.value);
    if (inputRef.current) {
      inputRef.current.style.height = "auto";
      inputRef.current.style.height = `${inputRef.current.scrollHeight}px`;
    }
  };

  return (
    <div
      className={`flex flex-col h-[calc(100vh-60px)] ${
        theme === "dark" ? "bg-red text-white" : "bg-white text-black"
      }`}
    >
      <div
        ref={containerRef}
        className="flex-1 overflow-y-auto px-2 py-4 space-y-4"
      >
        {messages.map((msg, idx) => (
          <div
            key={idx}
            className={`flex ${
              msg.role === "user" ? "justify-end" : "justify-start"
            }`}
          >

            <div
              className={
                msg.role === "user"
                  ? `rounded-xl shadow-xl text-white mx-4  border ${
                      theme === "dark" ? "border-gray-700" : "border-gray-300"
                    }`
                  : "w-full  p-2 rounded-md"
              }
            >
              <Message role={msg.role} content={msg.text} />
            </div>
          </div>
        ))}

      </div>

      {/* Input area */}
      <div
        className={`border shadow-md border-gray-70 text-white py-1 px-2 rounded-xl mx-2 my-4 ${
          theme === "dark"
            ? "bg-[#212121] text-white placeholder-gray-400 border-gray-600"
            : "bg-gray-100 text-black placeholder-gray-600 border-gray-300"
        }`}
      >
        <div className="w-full">
          <textarea
            ref={inputRef}
            rows={1}
            value={input}
            onChange={handleInputChange}
            onPaste={() => {}}
            placeholder=""
            className={`flex-1 focus:outline-none focus:ring-0 resize-none max-h-48  p-2 text-sm outline-none w-full  ${
              theme === "dark"
                ? "bg-[#212121] text-white placeholder-gray-400 border-gray-600"
                : "bg-gray-100 text-black placeholder-gray-600 border-gray-300"
            }`}
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                handleSend();
              }
            }}
          />
          <div className="flex items-end gap-2 justify-between">
            <div className="flex items-center gap-1">
              <SearchBar />

              <button
                type="button"
                onClick={handleSend}
                className={`flex items-center justify-center rounded-full w-5 h-5 transition ${
                  theme === "dark"
                    ? "bg-white hover:bg-gray-200 text-black"
                    : "bg-gray-700 hover:bg-[#212121] text-white"
                }`}
                title="Send"
              >
                <EllipsisVertical />
              </button>
            </div>

            <button
              type="button"
              onClick={() => {}}
              className={`flex items-center justify-center rounded-full w-10 h-10 transition cursor pointer ${
                theme === "dark"
                  ? "bg-white hover:bg-gray-200 text-black"
                  : "bg-gray-700 hover:bg-[#212121] text-white"
              }`}
              title="Send"
            >
              <ArrowUpFromDot />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CodeGuide;
