package com.example.rentmg.util

import android.content.Context

/**
 * TokenStore Singleton Object
 * Manages persistent storage of JWT authentication token using SharedPreferences
 *
 * This utility:
 * - Saves JWT token to persistent storage (survives app restarts)
 * - Retrieves stored token for authenticated API requests
 * - Clears token on logout
 * - Uses Android SharedPreferences in private mode for security
 *
 * Usage:
 *   TokenStore.save(context, "jwt_token_here")
 *   val token = TokenStore.get(context)
 *   TokenStore.clear(context) // On logout
 */
object TokenStore {

    // SharedPreferences file name
    private const val PREF = "rentmg.pref"

    // Key for storing JWT token
    private const val KEY = "jwt"

    /**
     * Saves JWT token to persistent storage
     * Token will be available even after app restart
     *
     * @param context Android context (Activity or Application)
     * @param token JWT token string to store
     */
    fun save(context: Context, token: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, token)
            .apply() // Asynchronous save
    }

    /**
     * Retrieves stored JWT token from persistent storage
     * Returns null if no token is stored (user not logged in)
     *
     * @param context Android context (Activity or Application)
     * @return JWT token string, or null if not found
     */
    fun get(context: Context): String? {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, null)
    }

    /**
     * Clears stored JWT token from persistent storage
     * Should be called when user logs out
     *
     * @param context Android context (Activity or Application)
     */
    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY)
            .apply() // Asynchronous removal
    }
}
