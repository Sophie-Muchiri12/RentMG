package com.example.rentmg.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ApiClient Singleton Object
 * Responsible for building Retrofit instances with proper configuration
 *
 * This object creates a configured Retrofit instance that:
 * - Uses OkHttp as the HTTP client
 * - Adds JWT authentication via AuthInterceptor
 * - Converts JSON responses to Kotlin objects using Gson
 *
 * Usage:
 *   val authInterceptor = AuthInterceptor()
 *   val retrofit = ApiClient.build("http://10.0.2.2:5000/", authInterceptor)
 *   val apiService = retrofit.create(ApiService::class.java)
 */
object ApiClient {

    /**
     * Builds a Retrofit instance with authentication
     *
     * @param baseUrl The base URL of the API (e.g., "http://10.0.2.2:5000/")
     *                For Android emulator: 10.0.2.2 maps to host machine's localhost
     *                For physical device: Use actual IP address
     * @param auth AuthInterceptor instance that adds JWT token to requests
     * @return Configured Retrofit instance ready to create API service interfaces
     */
    fun build(baseUrl: String, auth: AuthInterceptor): Retrofit {
        // Build OkHttp client with authentication interceptor
        // The interceptor will automatically add Authorization header to all requests
        val client = OkHttpClient.Builder()
            .addInterceptor(auth)
            .build()

        // Build and return Retrofit instance
        return Retrofit.Builder()
            .baseUrl(baseUrl) // API base URL
            .addConverterFactory(GsonConverterFactory.create()) // JSON to Kotlin object conversion
            .client(client) // Use our configured HTTP client with auth
            .build()
    }
}
