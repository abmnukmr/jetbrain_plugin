package com.github.abmnukmr.jetbrainplugin.toolWindow

import com.github.abmnukmr.jetbrainplugin.listener.SelectionGutterListener
import com.github.abmnukmr.jetbrainplugin.listener.SelectionInputInlayListener
import com.github.abmnukmr.jetbrainplugin.listener.SelectionInputPopupListener
import com.github.abmnukmr.jetbrainplugin.listener.SelectionSuggestListener
import com.github.abmnukmr.jetbrainplugin.services.*
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import org.json.JSONObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.io.File
import java.io.FileOutputStream
import java.net.JarURLConnection
import java.nio.file.Files
import java.util.jar.JarFile
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange


class MyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {


        // 1. Extract Next.js static site into a temp directory
        val tempDir = Files.createTempDirectory("next").toFile()
        extractResources("web/precommit-ai/out", tempDir)

        // 2. Serve the static site
        val server = StaticFileServer(tempDir, 63342)
        server.start()

        // 3. Setup JCEF browser pointing to index.html
        val browser = JBCefBrowser("http://localhost:63342/index.html")
        browser.openDevtools()

        val client = browser.jbCefClient
        val cefBrowser = browser.cefBrowser

        // 4. Setup message router (React ➝ Plugin via window.cefQuery)
        val router = CefMessageRouter.create()


        router.addHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(browser: CefBrowser, frame: CefFrame?, queryId: Long, request: String, persistent: Boolean, callback: CefQueryCallback): Boolean {
                val json = JSONObject(request)
                val filesPath = ProjectFileOperation().getAllProjectFilePaths(project)

                println("file path== $filesPath")
                when (json.getString("type")) {
                    "generate" ->{
                        SSEClient().startSSEStream(json.getString("payload"), browser);
                    }
                    "stopStream" ->{
                        SSEClient().stopSSEStream(browser);
                    }
                    "chooseDirectory" ->{
                        ReadWrite().insertSuggestionWithClickHandlers(project,"console.log(1)")

                       /// DirectoryPicker().createDirectoryPickerPanel(project, browser);
                    }

                    "preCommit-ai-setup" -> {
                        DirectoryPicker().getFilesNotIgnored(project, null,browser);
                    }

                    "readFile" -> {
                        val path = json.getString("path")
                        val contents = File(path).readText()
                        callback.success(contents)
                    }
                    "writeFile" -> {
                        val path = json.getString("path")
                        val contents = json.getString("contents")
                        File(path).writeText(contents)
                        callback.success("✅ Written")
                    }
                    else -> callback.failure(404, "Unknown command")
                }
                return true
            }
        }, true)

        client.cefClient.addMessageRouter(router)

        // 5. Optional: Safe communication via JBCefJSQuery
        val jsQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)
        jsQuery.addHandler { message ->
            null
        }

        // 6. Inject JavaScript bridge after page load
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(
                cefBrowser: CefBrowser?,
                frame: CefFrame?,
                httpStatusCode: Int
            ) {
                if (frame?.isMain == true) {
                    val jsToInject = """
                    (function() {
                        function injectPluginBridge() {
                            if (typeof window.__sendToPlugin === 'undefined') {
                                window.__sendToPlugin = function(data) {
                                    window.cefQuery({
                                        request: JSON.stringify(data),
                                        onSuccess: function(response) {
                                            console.log("✅ Plugin responded:", response);
                                        },
                                        onFailure: function(errCode, errMsg) {
                                            console.error("❌ Plugin error:", errCode, errMsg);
                                        }
                                    });
                                };
                                
                                // Send pluginReady message after injection
                                window.postMessage({ command: "pluginReady" }, "*");
                                
                                console.log("🔗 __sendToPlugin injected and pluginReady posted");
                            }
                        }

                        injectPluginBridge();

                        const observer = new MutationObserver(injectPluginBridge);
                        observer.observe(document.body || document.documentElement, {
                            childList: true,
                            subtree: true
                        });

                        setInterval(injectPluginBridge, 3000);
                    })();
                    """.trimIndent()

                    cefBrowser?.executeJavaScript(jsToInject, cefBrowser.url, 0)
                }
            }
        }, cefBrowser)

        // 7. Add browser to tool window
        val content = ContentFactory.getInstance()
            .createContent(browser.component, "", false)
        toolWindow.contentManager.addContent(content)
    }




    private fun extractResources(resourcePath: String, destDir: File) {
        val resourceUrl = javaClass.classLoader.getResource(resourcePath)
            ?: throw IllegalStateException("Resource not found: $resourcePath")

        val connection = resourceUrl.openConnection()
        if (connection is JarURLConnection) {
            val jarFile: JarFile = connection.jarFile
            jarFile.entries().asSequence().forEach { entry ->
                if (entry.name.startsWith(resourcePath)) {
                    val relative = entry.name.removePrefix("$resourcePath/")
                    val outFile = File(destDir, relative)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile.mkdirs()
                        jarFile.getInputStream(entry).use { input ->
                            FileOutputStream(outFile).use { output -> input.copyTo(output) }
                        }
                    }
                }
            }
        } else {
            File(resourceUrl.toURI()).copyRecursively(destDir, overwrite = true)
        }
    }



}
