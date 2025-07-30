'use client';
import { NotificationProvider } from "./context/NotificationContext";
import { SelectDirectoryProvider } from "./context/SelectDirectoryContext";
import { SelectThemeProvider } from "./context/SelectThemeContext";
import { CodeView } from "./page/codeView";

export default function Home() {
  return (
    <NotificationProvider>
      <SelectThemeProvider>
        <SelectDirectoryProvider>
          <CodeView />
        </SelectDirectoryProvider>
      </SelectThemeProvider>
    </NotificationProvider>
  );
}
