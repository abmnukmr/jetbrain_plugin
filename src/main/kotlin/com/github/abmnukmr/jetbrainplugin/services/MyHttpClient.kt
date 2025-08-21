package com.github.abmnukmr.jetbrainplugin.services

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --------------------------
// Request & Response Data Classes
// --------------------------
data class CompletionRequest(
    val codeBeforeCaret: String,
    val codeAfterCaret: String,
    val query: String,
    val language: String
)

data class CompletionResponse(
    val completion: String
)

// --------------------------
// Retrofit API Interface
// --------------------------
interface CodeCompletionApi {
    @POST("/code-completion")
    fun getCompletion(@Body request: CompletionRequest): Call<CompletionResponse>
}

// --------------------------
// HTTP Client using Retrofit
// --------------------------
class MyHttpClient(parentDisposable: Disposable, baseUrl: String = "http://localhost:8000") {

    private val api: CodeCompletionApi

    init {
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

        api = retrofit.create(CodeCompletionApi::class.java)

        // Dispose interceptor if needed
        ApplicationManager.getApplication().executeOnPooledThread {
            Disposer.register(parentDisposable) {
                // Cleanup or cancel pending requests if needed
            }
        }
    }

    /**
     * Fetch code completion asynchronously.
     * onSuccess / onError are run on the UI thread.
     */
    fun fetchCompletionAsync(
        codeBeforeCaret: String,
        codeAfterCaret: String,
        query: String = "",
        language: String,
        onSuccess: (CompletionResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val request = CompletionRequest(codeBeforeCaret, codeAfterCaret, query, language)
        api.getCompletion(request).enqueue(object : Callback<CompletionResponse> {
            override fun onResponse(
                call: Call<CompletionResponse>,
                response: Response<CompletionResponse>
            ) {
                if (response.isSuccessful) {
                    ApplicationManager.getApplication().invokeLater {
                        response.body()?.let(onSuccess)
                            ?: onError(Throwable("Empty response body"))
                    }
                } else {
                    ApplicationManager.getApplication().invokeLater {
                        onError(Throwable("HTTP error: ${response.code()}"))
                    }
                }
            }

            override fun onFailure(call: Call<CompletionResponse>, t: Throwable) {
                ApplicationManager.getApplication().invokeLater {
                    onError(t)
                }
            }
        })
    }
}
