package com.github.abmnukmr.jetbrainplugin.services

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class GenerateApiClient(private val baseUrl: String = "http://localhost:8000") {

    private val client = OkHttpClient()

    fun generate(prompt: String): String? {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val url = "$baseUrl/generate?prompt=$encodedPrompt"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("API call failed: ${response.code}")
                return null
            }

            return response.body?.string()
        }
    }
}
