package com.cosup.app.data

import com.cosup.app.BuildConfig
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(private val tokenProvider: () -> String?) {

    val api: CosupApi by lazy {
        val auth = Interceptor { chain ->
            val token = tokenProvider()
            val req = if (token.isNullOrBlank()) chain.request()
            else chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            chain.proceed(req)
        }
        val builder = OkHttpClient.Builder()
            .addInterceptor(auth)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            // Log request lines only - never bodies, they can contain tokens.
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        }
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CosupApi::class.java)
    }
}

/** Turns any failure into a short message that is safe to show the user. */
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> parseError(response()?.errorBody()?.string()) ?: when (code()) {
        401 -> "Please sign in again."
        403 -> "You do not have access to that."
        else -> "The server returned an error (${code()}). Please try again."
    }
    is IOException -> "Network problem. Check your connection and try again."
    else -> message ?: "Something went wrong. Please try again."
}

/** Reads { "error": "..." } or ASP.NET validation problems { "errors": { "Field": ["msg"] } }. */
fun parseError(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return try {
        val obj = JsonParser.parseString(body).asJsonObject
        when {
            obj.has("error") && !obj.get("error").isJsonNull -> obj.get("error").asString
            obj.has("errors") && obj.get("errors").isJsonObject ->
                obj.getAsJsonObject("errors").entrySet().firstOrNull()
                    ?.value?.asJsonArray?.firstOrNull()?.asString
            obj.has("title") -> obj.get("title").asString
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
