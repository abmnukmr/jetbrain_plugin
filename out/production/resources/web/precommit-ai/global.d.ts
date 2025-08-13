// global.d.ts
declare function acquireVsCodeApi(): {
  postMessage: (msg: any) => void;
  setState: (state: any) => void;
  getState: () => any;
};

declare global {
  interface Window {
    __sendToPlugin?: (data: string) => void;
  }
}

declare global {
  interface Window {
    cefQuery?: (params: {
      request: string;
      onSuccess: (response: string) => void;
      onFailure: (errorCode: number, errorMsg: string) => void;
    }) => void;
  }
}

export {};