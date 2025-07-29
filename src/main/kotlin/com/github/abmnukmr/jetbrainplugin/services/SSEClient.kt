package com.github.abmnukmr.jetbrainplugin.services

import kotlinx.coroutines.*
import org.cef.browser.CefBrowser
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.SwingUtilities

class SSEClient {

    fun startSSEStream(prompt: String, cefBrowser: CefBrowser) {
        CoroutineScope(Dispatchers.IO).launch {
            val urlStr = "http://localhost:8000/stream?prompt=${java.net.URLEncoder.encode(prompt, "UTF-8")}"
            var completed = false
            val buffer = StringBuilder()

            try {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "text/event-stream")
                conn.connectTimeout = 5000
                conn.readTimeout = 0
                conn.doInput = true

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                reader.useLines { lines ->

                    lines.forEach { line ->
                        println("Render===> $lines");

                        if (line.isBlank()) return@forEach  // skip empty lines

                        buffer.append(line)
                        if (buffer.length >= 10) {
                            val emitChunk = buffer.substring(0, 10)
                            buffer.delete(0, 10)
                            println("Chunk: ${JSONObject.quote(emitChunk)}")
                            SwingUtilities.invokeLater {
                                val js = """
                                    window.postMessage({ command: "response", chunk: ${JSONObject.quote(emitChunk)} }, "*");
                                """.trimIndent()
                                cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                            }
                        }
                    }
                }

                // Flush remaining buffer
                if (buffer.isNotEmpty()) {
                    SwingUtilities.invokeLater {
                        val js = """
                            window.postMessage({ command: "response", chunk: ${JSONObject.quote(buffer.toString())} }, "*");
                        """.trimIndent()
                        cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                    }
                }

                completed = true

            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    val js = """
                        window.postMessage({ command: "error", error: ${JSONObject.quote(e.message ?: "Unknown error")} }, "*");
                    """.trimIndent()
                    cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                }

            } finally {
                SwingUtilities.invokeLater {
                    cefBrowser.executeJavaScript(
                        """window.postMessage({ command: "responseCompleted", ok: ${completed.toString()} }, "*");""",
                        cefBrowser.url,
                        0
                    )
                }
            }
        }
    }
}
