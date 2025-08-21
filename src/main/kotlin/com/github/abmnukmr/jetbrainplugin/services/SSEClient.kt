package com.github.abmnukmr.jetbrainplugin.services

import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import groovy.json.StringEscapeUtils
import kotlinx.coroutines.*
import org.cef.browser.CefBrowser
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.util.logging.Logger

class SSEClient {

    private val logger: Logger = Logger.getLogger(SSEClient::class.java.name)
    private var connection: HttpURLConnection? = null


    fun startSSEStream(prompt: String, cefBrowser: CefBrowser) {

        PluginCoroutineScope.sseJob?.cancel()
        PluginCoroutineScope.sseJob = PluginCoroutineScope.scope.launch {
            val urlStr = "http://localhost:8000/stream?prompt=${URLEncoder.encode(prompt, "UTF-8")}"
            val buffer = StringBuilder()
            var completed = false

            try {
                val url = URI.create(urlStr).toURL();
                connection = url.openConnection() as HttpURLConnection
                connection?.setRequestProperty("Accept", "text/event-stream")
                connection?.connectTimeout = 5000
                connection?.readTimeout = 0
                connection?.doInput = true

                val reader = BufferedReader(InputStreamReader(connection!!.inputStream))

                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (line.isBlank()) return@forEach
                        buffer.append(line).append('\n')

                        while (buffer.length >= 10) {
                            val chunk = buffer.substring(0, 10)
                            buffer.delete(0, 10)

                            ApplicationManager.getApplication().invokeLater {
                                val js = """window.postMessage({ command: "response", chunk: ${JSONObject.quote(chunk)} }, "*");"""
                                cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                            }
                            Thread.sleep(100)
                        }
                    }
                }

                if (buffer.isNotEmpty()) {
                    ApplicationManager.getApplication()
                    .invokeLater {
                        val js = """window.postMessage({ command: "response", chunk: ${JSONObject.quote(buffer.toString())} }, "*");"""
                        cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                    }
                    buffer.clear()
                }

                completed = true

            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    val js = """window.postMessage({ command: "error", error: ${JSONObject.quote(e.message ?: "Unknown error")} }, "*");"""
                    cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
                }

            } finally {
                ApplicationManager.getApplication().invokeLater {
                    cefBrowser.executeJavaScript(
                        """window.postMessage({ command: "responseCompleted", ok: ${completed} }, "*");""",
                        cefBrowser.url,
                        0
                    )
                }
                connection = null
            }
        }
    }



    fun stopSSEStream(cefBrowser: CefBrowser) {
        println("Stopping stream...")
        PluginCoroutineScope.sseJob?.cancel()
        PluginCoroutineScope.sseJob = null

        try {
            connection?.disconnect() // 💥 forcibly closes the input stream
        } catch (e: Exception) {
            println("Stream already closed or failed to disconnect")
        }
        finally {
            connection = null
        }

        cefBrowser.executeJavaScript(
            """window.postMessage({ command: "responseCompleted", ok: "interrupted" }, "*");""",
            cefBrowser.url,
            0
        )
    }

    fun fetchInlineCorrectionWordStream(
        fullCode: String,
        selectedCode: String,
        query: String,
        language: String,
        onChunk: (String) -> Unit,
        onError: (String) -> Unit,
        onComplete: (String) -> Unit,
    ) {
        PluginCoroutineScope.scope.launch(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("http://localhost:8000/inline-correct-stream")
                val payload = mapOf(
                    "fullCode" to fullCode,
                    "selectedCode" to selectedCode,
                    "query" to query,
                    "language" to language
                )
                val jsonPayload = Gson().toJson(payload)

                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "text/event-stream")
                    connectTimeout = 5000
                    readTimeout = 0 // infinite for streaming
                }

                connection.outputStream.use { os ->
                    os.write(jsonPayload.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                val buffer = StringBuilder()

                reader.useLines { lines ->
                    lines.forEach { rawLine ->
                        if (rawLine.isBlank()) return@forEach
                        val line = rawLine
                            .trim()                     // remove leading/trailing whitespace
                            .removePrefix("\"")         // remove leading quote
                            .removeSuffix("\"")         // remove trailing quote
                            .let { StringEscapeUtils.unescapeJava(it) }  // handle \n, \"
                            .replaceFirst("^```[a-zA-Z0-9]*\\s*".toRegex(), "")  // remove opening fence
                            .replaceFirst("```$".toRegex(RegexOption.MULTILINE), "") // remove closing fence


                        buffer.append(line)

                        // Emit fixed-size chunks
                        while (buffer.length >= 10) {
                            val chunk = buffer.substring(0, 10)
                            buffer.delete(0, 10)
                            ApplicationManager.getApplication().invokeLater {
                                if(!chunk.equals('"')) {
                                    logger.info("chunk--> $chunk")
                                    onChunk(chunk)
                                }
                            }
                            Thread.sleep(100)
                        }
                    }

                    // Emit any remaining content
                    if (buffer.isNotEmpty()) {
                        val chunk = buffer.toString()
                        ApplicationManager.getApplication().invokeLater {
                            if(!chunk.equals('"')) {
                                onChunk(chunk)
                            }
                        }
                        buffer.clear()
                    }
                }

            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    onError(e.message ?: "Unknown SSE error")
                }
            } finally {
                onComplete("completed")
                connection?.disconnect()
            }
        }
    }


}
