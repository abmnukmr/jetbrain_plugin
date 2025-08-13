// pages/index.tsx (or index.js) - this code runs in the browser, inside the iframe
import React, { useEffect, useState } from 'react';

// Define the shape of the data received from the extension
interface WorkspaceData {
  rootPath: string;
  files: string[];
}

const ProjectDirectoryExplorer: React.FC = () => {
  const [workspaceData, setWorkspaceData] = useState<WorkspaceData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // This effect runs once on component mount in the browser.

    // 1. Set up listener for messages from the extension (relayed by the parent webview script)
    try {
    const messageListener = (event: MessageEvent) => {
      // Ensure the message is from the expected source (the parent webview script)
      // and contains valid data.
      if (!event.data || typeof event.data.command === 'undefined') {
        return;
      }

      console.log('React app received message from extension (via relay):', event.data);
      const message = event.data;

      switch (message.command) {
        case 'webviewReady':
            setWorkspaceData({
            rootPath: message.rootPath,
            files: message.files,
          });
          setLoading(false);
          setError(JSON.stringify(message));
          console.log('Workspace data received and set in React state.');
          break;
        case 'workspaceData':
          setWorkspaceData({
            rootPath: message.rootPath,
            files: message.files,
          });
          setLoading(false);
          setError(JSON.stringify(message));
          console.log('Workspace data received and set in React state.');
          break;
        case 'workspaceDataError':
          setError(message.error || 'Unknown error getting workspace data.');
          setLoading(false);
          setError(JSON.stringify(message));
          console.error('Error receiving workspace data:', message.error);
          break;
        // Handle other commands if you add them (e.g., 'fileContent')
        default:
          console.log('React app received unhandled message command:', message.command);
      }
    };

    window.addEventListener('message', messageListener);
    } catch (e: any) {
      console.error('Failed to set up message listener:', e);
      setError(`Failed to set up message listener: ${e.message}`);
      setLoading(false);
    }
    // 2. Send request to the extension (via the parent webview script)
    // We send this after setting up the listener to ensure we don't miss the response.
    // The `postMessage` target is `window.parent` because the React app is in an iframe.
    // The `*` targetOrigin is generally fine for webviews.
   


    // Cleanup: remove the event listener when the component unmounts
    return () => {
        // try {
        //   window.removeEventListener('message', messageListener);
        // } catch (e: any) {
        //   console.error('Failed to remove message listener:', e);
        // }
    };
  }, []); // Empty dependency array means this effect runs once after the initial render

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
        <h1>Project Directory Explorer</h1>
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}

      {workspaceData && (
        <div>
          {/* <h2>Workspace Info:</h2>
          <p><strong>Root Path:</strong> {workspaceData.rootPath}</p>

          <h3>Files Found ({workspaceData.files.length}):</h3>
          {workspaceData.files.length > 0 ? (
            <ul style={{ maxHeight: '300px', overflowY: 'auto', border: '1px solid #ccc', padding: '10px' }}>
              {workspaceData.files.map((file, index) => (
                <li key={index} style={{ marginBottom: '5px', wordBreak: 'break-all' }}>{file}</li>
              ))}
            </ul>
          ) : (
            <p>No files found in the workspace (excluding node_modules and .git).</p>
          )} */}
        </div>
      )}
    </div>
  );
};

export default ProjectDirectoryExplorer;