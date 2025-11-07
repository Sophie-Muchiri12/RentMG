package com.example.rentmg.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// Data Classes
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val password_confirm: String,
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val user_type: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserData(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val full_name: String,
    val phone_number: String,
    val user_type: String,
    val date_joined: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?,
    val errors: Map<String, List<String>>?
)

data class AuthData(
    val user: UserData,
    val token: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val errors: Map<String, List<String>>?
)

// API Interface
interface ApiService {
    
    @POST("auth/register/")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("auth/logout/")
    suspend fun logout(@Header("Authorization") token: String): ApiResponse<Unit>
    
    @GET("auth/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): ApiResponse<UserData>
    
    companion object {
        // Replace with your actual backend URL
        private const val BASE_URL = "http://10.0.2.2:8000/api/"  // For Android Emulator
        // For real device, use: "http://YOUR_IP_ADDRESS:8000/api/"
        
        fun create(): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
}