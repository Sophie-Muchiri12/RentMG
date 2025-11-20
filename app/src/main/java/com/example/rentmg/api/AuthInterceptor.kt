package com.example.rentmg.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor Class
 * OkHttp interceptor that automatically adds JWT authentication token to API requests
 *
 * This interceptor:
 * - Stores the JWT token in a thread-safe manner using @Volatile
 * - Intercepts all outgoing HTTP requests
 * - Adds "Authorization: Bearer <token>" header if token is available
 * - Allows requests to proceed without token (for login/register endpoints)
 *
 * The token should be set after successful login using setToken() method
 */
class AuthInterceptor : Interceptor {

    /**
     * JWT token storage
     * @Volatile ensures visibility of changes across threads
     * Initialized as null (no token) until user logs in
     */
    @Volatile
    private var token: String? = null

    /**
     * Sets the JWT authentication token
     * Call this after successful login to enable authenticated requests
     *
     * @param t JWT token string, or null to clear the token (logout)
     */
    fun setToken(t: String?) {
        token = t
    }

    /**
     * Intercepts outgoing HTTP requests and adds authentication header
     *
     * This method is called automatically by OkHttp for every request
     * If a token is available, it adds: "Authorization: Bearer <token>"
     * If no token is set, the request proceeds without modification
     *
     * @param chain The interceptor chain
     * @return HTTP response from the server
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request
        var req = chain.request()

        // If token exists, add Authorization header
        token?.let {
            req = req.newBuilder()
                .addHeader("Authorization", "Bearer $it")
                .build()
        }

        // Proceed with the (possibly modified) request
        return chain.proceed(req)
    }
}
