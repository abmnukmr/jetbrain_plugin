

export  const useSentToJetBrain = (pluginReady:boolean) =>{
  const  sendToPlugin = (command:string, payload: string)=>
     {

          if (!pluginReady || typeof window.cefQuery !== "function") return;

               //Send message to plugin via cefQuery
               window.cefQuery({
                 request: JSON.stringify({type:command,
                   payload,
                 }),
                 onSuccess: (response: string) => {
                   console.log("✅ Plugin responded: ", response);
                 },
                 onFailure: (code: number, msg: string) => {
                   console.error(`❌ Plugin error (${code}):`, msg);
                 },
               });

     }

    return sendToPlugin;
}
