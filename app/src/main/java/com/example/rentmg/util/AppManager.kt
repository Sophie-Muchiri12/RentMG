package com.example.rentmg.util

import android.content.Context
import com.example.rentmg.api.ApiClient
import com.example.rentmg.api.ApiService
import com.example.rentmg.api.AuthInterceptor
import com.example.rentmg.data.model.User

/**
 * AppManager Singleton Object
 * Centralized application state and API client management
 *
 * This singleton provides:
 * - Global access to API service instance
 * - Centralized authentication state management
 * - User session management
 * - Token initialization from persistent storage
 *
 * Usage:
 *   AppManager.initialize(context)
 *   val apiService = AppManager.getApiService()
 *   AppManager.setUser(user)
 *   val currentUser = AppManager.getCurrentUser()
 */
object AppManager {

    // Base URL for API
    // For Android emulator: 10.0.2.2 maps to host machine's localhost:5000
    // For physical device: replace with actual IP address (e.g., "http://192.168.1.100:5000/")
    private const val BASE_URL = "http://10.0.2.2:5000/"

    // Authentication interceptor instance (shared across entire app)
    private val authInterceptor = AuthInterceptor()

    // API service instance (shared across entire app)
    private val apiService: ApiService by lazy {
        // Build Retrofit instance with auth interceptor
        val retrofit = ApiClient.build(BASE_URL, authInterceptor)
        // Create and return API service
        retrofit.create(ApiService::class.java)
    }

    // Current logged-in user (null if not logged in)
    private var currentUser: User? = null

    /**
     * Initializes AppManager with application context
     * Should be called in Application.onCreate() or MainActivity.onCreate()
     *
     * Loads saved JWT token from persistent storage and sets it in auth interceptor
     * This allows authenticated API calls to work immediately after app restart
     *
     * @param context Application context
     */
    fun initialize(context: Context) {
        // Load saved token from SharedPreferences
        val savedToken = TokenStore.get(context)

        // If token exists, set it in auth interceptor
        if (savedToken != null) {
            authInterceptor.setToken(savedToken)
        }
    }

    /**
     * Gets the global API service instance
     * All API calls should use this instance
     *
     * @return ApiService instance for making API calls
     */
    fun getApiService(): ApiService {
        return apiService
    }

    /**
     * Gets the auth interceptor instance
     * Needed when activities/fragments need to update the token
     *
     * @return AuthInterceptor instance
     */
    fun getAuthInterceptor(): AuthInterceptor {
        return authInterceptor
    }

    /**
     * Sets the current logged-in user
     * Should be called after successful login or registration
     *
     * @param user User object from backend
     */
    fun setUser(user: User) {
        currentUser = user
    }

    /**
     * Gets the current logged-in user
     * Returns null if no user is logged in
     *
     * @return Current User object or null
     */
    fun getCurrentUser(): User? {
        return currentUser
    }

    /**
     * Checks if a user is currently logged in
     *
     * @return true if user is logged in, false otherwise
     */
    fun isLoggedIn(): Boolean {
        return currentUser != null
    }

    /**
     * Logs out the current user
     * Clears user data and removes auth token
     *
     * @param context Application context for clearing token storage
     */
    fun logout(context: Context) {
        // Clear current user
        currentUser = null

        // Clear auth token from interceptor
        authInterceptor.setToken(null)

        // Clear token from persistent storage
        TokenStore.clear(context)
    }

    /**
     * Sets authentication token after login
     * Saves token to both interceptor and persistent storage
     *
     * @param context Application context for saving token
     * @param token JWT token string
     */
    fun setToken(context: Context, token: String) {
        // Set token in interceptor for API calls
        authInterceptor.setToken(token)

        // Save token to persistent storage
        TokenStore.save(context, token)
    }
}
