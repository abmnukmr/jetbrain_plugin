package com.github.abmnukmr.jetbrainplugin.services

import kotlinx.coroutines.*
import org.cef.browser.CefBrowser
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.swing.SwingUtilities

class SSEClient {
    fun startSSEStream(prompt: String, cefBrowser: CefBrowser) {
        PluginCoroutineScope.scope.launch {
            val urlStr = "http://localhost:8000/stream?prompt=${URLEncoder.encode(prompt, "UTF-8")}"
            val buffer = StringBuilder()
            var streamEnded = false
            var completed = false

            try {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "text/event-stream")
                conn.connectTimeout = 5000
                conn.readTimeout = 0
                conn.doInput = true

                val reader = BufferedReader(InputStreamReader(conn.inputStream))

                // Background line-by-line reader
                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (line.isBlank()) return@forEach
                        // Preserve line breaks
                        buffer.append(line).append('\n')

                        while (buffer.length >= 10) {
                            val chunk = buffer.substring(0, 10)
                            buffer.delete(0, 10)
                            SwingUtilities.invokeLater {
                                val js = """window.postMessage({ command: "response", chunk: ${JSONObject.quote(chunk)} }, "*");"""
                                cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                            }
                            Thread.sleep(100)
                        }

                    }
                    streamEnded = true
                }

                // Emit remaining data
                if (buffer.isNotEmpty()) {
                    SwingUtilities.invokeLater {
                        val js = """window.postMessage({ command: "response", chunk: ${JSONObject.quote(buffer.toString())} }, "*");"""
                        cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                    }
                    buffer.clear()
                }

                completed = true

            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    val js = """window.postMessage({ command: "error", error: ${JSONObject.quote(e.message ?: "Unknown error")} }, "*");"""
                    cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                }

            } finally {
                SwingUtilities.invokeLater {
                    cefBrowser.executeJavaScript(
                        """window.postMessage({ command: "responseCompleted", ok: ${completed} }, "*");""",
                        cefBrowser.url,
                        0
                    )
                }
            }
        }
    }

}
