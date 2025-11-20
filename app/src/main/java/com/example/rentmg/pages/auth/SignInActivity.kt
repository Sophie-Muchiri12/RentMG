package com.example.rentmg.pages.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.view.View
import com.example.rentmg.api.*
import com.example.rentmg.util.AppManager
import retrofit2.*
import com.example.rentmg.R
import com.example.rentmg.pages.dashboard.DashboardActivity
import com.example.rentmg.pages.dashboard.TenantDashboardActivity

/**
 * SignInActivity
 * Handles user authentication and login flow
 *
 * Features:
 * - Email/password validation
 * - Backend API authentication
 * - JWT token storage
 * - Role-based dashboard routing (tenant vs landlord/property manager)
 * - Loading state management
 */
class SignInActivity : AppCompatActivity() {

    // UI components
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signInButton: Button
    private lateinit var progressBar: ProgressBar

    // Loading state flag to prevent multiple simultaneous login attempts
    private var isLoading = false

    /**
     * Activity lifecycle: onCreate
     * Called when activity is first created
     * Initializes UI components and sets up event listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Hide the action bar for cleaner UI
        supportActionBar?.hide()

        // Initialize UI components
        initViews()

        // Set up button click listeners
        setupListeners()
    }

    /**
     * Initializes all UI components by finding them by ID
     * Called once during onCreate
     */
    private fun initViews() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        signInButton = findViewById(R.id.sign_in_button)
        progressBar = findViewById(R.id.progress_bar)
    }

    /**
     * Sets up click listeners for interactive elements
     * Prevents multiple simultaneous login attempts when already loading
     */
    private fun setupListeners() {
        signInButton.setOnClickListener {
            // Only process click if not already loading
            if (!isLoading) {
                validateAndSignIn()
            }
        }
    }

    /**
     * Validates user input before attempting login
     * Checks email format and password presence
     * Shows error toast if validation fails
     */
    private fun validateAndSignIn() {
        // Get input values and trim whitespace
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Validate email format using Android's built-in pattern matcher
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure password is not empty
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validation passed, proceed with login
        performSignIn(email, password)
    }

    /**
     * Performs actual login by calling backend API
     * Shows loading indicator and makes network request
     *
     * @param email User's email address
     * @param password User's password
     */
    private fun performSignIn(email: String, password: String) {
        // Set loading state
        isLoading = true

        // Show progress bar, hide sign in button
        signInButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        // Create login request object
        val loginRequest = LoginRequest(email, password)

        // Make API call to login endpoint using global API service
        AppManager.getApiService().login(loginRequest).enqueue(object : Callback<LoginResponse> {

            /**
             * Called when server responds (success or error)
             * @param call The retrofit call object
             * @param response Server response containing status code and body
             */
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                // Reset loading state
                isLoading = false
                signInButton.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                // Check if response was successful (2xx status code)
                if (response.isSuccessful) {
                    // Extract token and user from response body
                    val token = response.body()?.access_token
                    val user = response.body()?.user

                    // Verify token and user exist
                    if (token != null && user != null) {
                        // Save token and user to app manager (handles both storage and interceptor)
                        AppManager.setToken(this@SignInActivity, token)
                        AppManager.setUser(user)

                        // Show success message
                        Toast.makeText(this@SignInActivity, "Sign In Success!", Toast.LENGTH_SHORT).show()

                        // Get user role and navigate to appropriate dashboard
                        val userRole = user.role
                        navigateToDashboard(userRole, user.fullName ?: "User", user.email)
                    } else {
                        // Response was successful but missing expected data
                        Toast.makeText(this@SignInActivity, "Invalid login response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Server returned error (4xx or 5xx)
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid email or password"
                        500 -> "Server error. Please try again later"
                        else -> "Login failed: ${response.code()}"
                    }
                    Toast.makeText(this@SignInActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            /**
             * Called when network request fails completely (no response)
             * Examples: no internet, server offline, timeout
             * @param call The retrofit call object
             * @param t The exception that caused the failure
             */
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Reset loading state
                isLoading = false
                signInButton.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                // Show network error message
                Toast.makeText(
                    this@SignInActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    /**
     * Navigates to appropriate dashboard based on user role
     * Clears back stack to prevent returning to login screen
     *
     * @param userRole User's role from backend ("tenant", "landlord", or "property_manager")
     * @param userName User's full name
     * @param userEmail User's email address
     */
    private fun navigateToDashboard(userRole: String, userName: String, userEmail: String) {
        // Determine which dashboard activity to launch based on role
        val intent = when (userRole) {
            "tenant" -> Intent(this, TenantDashboardActivity::class.java)
            "landlord", "property_manager" -> Intent(this, DashboardActivity::class.java)
            else -> {
                // Unknown role, default to landlord dashboard
                Toast.makeText(this, "Unknown user role: $userRole", Toast.LENGTH_SHORT).show()
                Intent(this, DashboardActivity::class.java)
            }
        }

        // Pass user information to dashboard activity
        intent.putExtra("USER_TYPE", userRole)
        intent.putExtra("USER_NAME", userName)
        intent.putExtra("USER_EMAIL", userEmail)

        // Clear back stack so user can't navigate back to sign in screen
        // This is important for security - logged-in users shouldn't access login screen
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start dashboard activity
        startActivity(intent)

        // Finish this activity (remove from back stack)
        finish()
    }
}
