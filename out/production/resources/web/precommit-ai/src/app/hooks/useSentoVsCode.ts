// Acquire once, globally
const vscodeApi =
  typeof window !== "undefined" && (window as any).acquireVsCodeApi
    ? (window as any).acquireVsCodeApi()
    : undefined;

export function useSendToVsCode() {
  const sendToVscode = (command: string, payload: string) => {
    if (!vscodeApi) return;
    try {
      vscodeApi.postMessage({ command, payload });
    } catch (e) {
      console.error("Error sending message to VSCode:", e);
    }
  };

  return sendToVscode;
}
