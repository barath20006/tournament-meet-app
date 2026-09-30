package com.sportmeet.app.data.remote.api

import android.content.Context
import com.sportmeet.app.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Default emulator base URL pointing to host machine's Spring Boot server
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/"

    @Volatile
    private var currentBaseUrl: String = DEFAULT_BASE_URL

    @Volatile
    private var apiInstance: SportMeetApi? = null

    @Volatile
    private var initializedFromPrefs: Boolean = false

    fun setBaseUrl(url: String, context: Context? = null) {
        val formatted = if (url.endsWith("/")) url else "$url/"
        if (currentBaseUrl != formatted) {
            currentBaseUrl = formatted
            apiInstance = null
        }
        context?.let {
            SessionManager.getInstance(it).baseUrl = formatted
        }
    }

    fun getBaseUrl(): String = currentBaseUrl

    fun getApi(context: Context): SportMeetApi {
        if (!initializedFromPrefs) {
            synchronized(this) {
                if (!initializedFromPrefs) {
                    val saved = SessionManager.getInstance(context).baseUrl
                    if (saved.isNotBlank()) {
                        val formatted = if (saved.endsWith("/")) saved else "$saved/"
                        currentBaseUrl = formatted
                    }
                    initializedFromPrefs = true
                }
            }
        }
        return apiInstance ?: synchronized(this) {
            apiInstance ?: createRetrofit(context).create(SportMeetApi::class.java).also {
                apiInstance = it
            }
        }
    }

    private fun createRetrofit(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val sessionManager = SessionManager.getInstance(context)
            val token = sessionManager.authToken

            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
