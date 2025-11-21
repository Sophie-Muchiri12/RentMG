package com.example.rentmg.pages.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import android.view.View
import com.example.rentmg.R
import com.example.rentmg.api.*
import com.example.rentmg.data.model.RegisterRequest
import com.example.rentmg.data.model.RegisterResponse
import com.example.rentmg.util.AppManager
import com.example.rentmg.pages.dashboard.DashboardActivity
import com.example.rentmg.pages.dashboard.TenantDashboardActivity
import retrofit2.*
import com.google.android.material.textfield.TextInputLayout

/**
 * SignUpActivity
 * Handles new user registration flow
 *
 * Features:
 * - Multi-field user registration form
 * - Role selection (landlord, property manager, or tenant)
 * - Comprehensive input validation
 * - Backend API registration
 * - Automatic login after registration
 * - Role-based dashboard routing
 */
class SignUpActivity : AppCompatActivity() {

    // UI components
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var propertyNameInput: EditText
    private lateinit var propertyAddressInput: EditText
    private lateinit var propertyNameLayout: TextInputLayout
    private lateinit var propertyAddressLayout: TextInputLayout
    private lateinit var userTypeSpinner: Spinner
    private lateinit var passwordInput: EditText
    private lateinit var passwordConfirmInput: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView
    private lateinit var progressBar: ProgressBar

    // Selected user role from spinner (default: landlord)
    private var selectedUserType: String = "landlord"

    // Loading state flag to prevent multiple simultaneous registration attempts
    private var isLoading = false

    /**
     * Activity lifecycle: onCreate
     * Called when activity is first created
     * Initializes UI components and sets up event listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Hide the action bar for cleaner UI
        supportActionBar?.hide()

        // Initialize UI components
        initViews()

        // Set up user type spinner with options
        setupUserTypeSpinner()

        // Set up button click listeners
        setupListeners()
    }

    /**
     * Initializes all UI components by finding them by ID
     * Called once during onCreate
     */
    private fun initViews() {
        firstNameInput = findViewById(R.id.first_name_input)
        lastNameInput = findViewById(R.id.last_name_input)
        usernameInput = findViewById(R.id.username_input)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        propertyNameInput = findViewById(R.id.property_name_input)
        propertyAddressInput = findViewById(R.id.property_address_input)
        propertyNameLayout = findViewById(R.id.property_name_layout)
        propertyAddressLayout = findViewById(R.id.property_address_layout)
        userTypeSpinner = findViewById(R.id.user_type_spinner)
        passwordInput = findViewById(R.id.password_input)
        passwordConfirmInput = findViewById(R.id.password_confirm_input)
        signUpButton = findViewById(R.id.sign_up_button)
        signInText = findViewById(R.id.sign_in_text)
        progressBar = findViewById(R.id.progress_bar)
    }

    /**
     * Sets up the user type spinner with three role options
     * Tracks the selected role in selectedUserType variable
     */
    private fun setupUserTypeSpinner() {
        // Define available user types
        val userTypes = arrayOf("Landlord", "Property Manager", "Tenant")

        // Create adapter for spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapter

        // Listen for spinner selection changes
        userTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            /**
             * Called when user selects a user type
             * Maps display names to backend role values
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedUserType = when (position) {
                    0 -> "landlord"          // Landlord option
                    1 -> "property_manager"  // Property Manager option
                    2 -> "tenant"            // Tenant option
                    else -> "landlord"       // Fallback
                }
                updatePropertyFieldVisibility()
            }

            /**
             * Called when nothing is selected (rare)
             * Defaults to landlord role
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedUserType = "landlord"
                updatePropertyFieldVisibility()
            }
        }

        // Ensure UI matches default selection on load
        updatePropertyFieldVisibility()
    }

    /**
     * Shows or hides property-related fields based on selected role.
     * Landlords: property name + address required.
     * Tenants: property name required to auto-link to landlord.
     * Others: hide property fields.
     */
    private fun updatePropertyFieldVisibility() {
        when (selectedUserType) {
            "landlord" -> {
                propertyNameLayout.visibility = View.VISIBLE
                propertyAddressLayout.visibility = View.VISIBLE
            }
            "tenant" -> {
                propertyNameLayout.visibility = View.VISIBLE
                propertyAddressLayout.visibility = View.GONE
                propertyAddressInput.setText("")
            }
            else -> {
                propertyNameLayout.visibility = View.GONE
                propertyAddressLayout.visibility = View.GONE
                propertyNameInput.setText("")
                propertyAddressInput.setText("")
            }
        }
    }

    /**
     * Sets up click listeners for interactive elements
     * Sign Up button: validates and registers user
     * Sign In text: navigates back to login screen
     */
    private fun setupListeners() {
        // Sign Up button click listener
        signUpButton.setOnClickListener {
            // Only process click if not already loading
            if (!isLoading) {
                validateAndSignUp()
            }
        }

        // Sign In text click listener (for users who already have account)
        signInText.setOnClickListener {
            // Go back to sign in screen
            finish()
        }
    }

    /**
     * Validates all user input before registration
     * Performs comprehensive checks:
     * - All fields filled
     * - Valid email format
     * - Password minimum length
     * - Passwords match
     */
    private fun validateAndSignUp() {
        // Get all input values
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val passwordConfirm = passwordConfirmInput.text.toString()

        // Validate first name
        if (firstName.isEmpty()) {
            Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate last name
        if (lastName.isEmpty()) {
            Toast.makeText(this, "Last name is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate username
        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate email format
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate phone number
        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate password length (minimum 8 characters)
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate passwords match
        if (password != passwordConfirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val propertyName = propertyNameInput.text.toString().trim()
        val propertyAddress = propertyAddressInput.text.toString().trim()

        // Landlords must supply property name and address for initial listing
        if (selectedUserType == "landlord") {
            if (propertyName.isEmpty()) {
                Toast.makeText(this, "Property name is required for landlord signup", Toast.LENGTH_SHORT).show()
                return
            }
            if (propertyAddress.isEmpty()) {
                Toast.makeText(this, "Property address is required for landlord signup", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Tenants must supply the landlord's property name to auto-link
        if (selectedUserType == "tenant" && propertyName.isEmpty()) {
            Toast.makeText(this, "Please enter your landlord's property name", Toast.LENGTH_SHORT).show()
            return
        }

        val propertyNameArg = when (selectedUserType) {
            "landlord", "tenant" -> propertyName
            else -> null
        }
        val propertyAddressArg = if (selectedUserType == "landlord") propertyAddress else null

        // All validation passed, proceed with registration
        performSignUp(firstName, lastName, email, password, propertyNameArg, propertyAddressArg)
    }

    /**
     * Performs actual registration by calling backend API
     * Shows loading indicator and makes network request
     *
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email address
     * @param password User's password
     */
    private fun performSignUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        propertyName: String?,
        propertyAddress: String?
    ) {
        // Set loading state
        isLoading = true

        // Show progress bar, hide sign up button
        signUpButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        // Combine first and last name for full name
        val fullName = "$firstName $lastName"

        // Create registration request object
        val registerRequest = RegisterRequest(
            email = email,
            password = password,
            role = selectedUserType,
            fullName = fullName,
            propertyName = propertyName,
            propertyAddress = propertyAddress
        )

        // Make API call to register endpoint using global API service
        AppManager.getApiService().register(registerRequest).enqueue(object : Callback<RegisterResponse> {

            /**
             * Called when server responds (success or error)
             * @param call The retrofit call object
             * @param response Server response containing status code and body
             */
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                // Reset loading state
                isLoading = false
                signUpButton.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                // Check if response was successful (2xx status code)
                if (response.isSuccessful) {
                    // Extract user from response
                    val user = response.body()?.user

                    if (user != null) {
                        // Show success message
                        Toast.makeText(
                            this@SignUpActivity,
                            "Account created successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        // After successful registration, automatically log in the user
                        // This provides better UX - user doesn't need to manually sign in
                        performAutoLogin(email, password)
                    } else {
                        // Response was successful but missing user data
                        Toast.makeText(
                            this@SignUpActivity,
                            "Registration successful but invalid response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Server returned error (4xx or 5xx)
                    val errorMessage = when (response.code()) {
                        400 -> "Email already exists or invalid data"
                        500 -> "Server error. Please try again later"
                        else -> "Registration failed: ${response.code()}"
                    }
                    Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            /**
             * Called when network request fails completely (no response)
             * Examples: no internet, server offline, timeout
             * @param call The retrofit call object
             * @param t The exception that caused the failure
             */
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                // Reset loading state
                isLoading = false
                signUpButton.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                // Show network error message
                Toast.makeText(
                    this@SignUpActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    /**
     * Automatically logs in user after successful registration
     * Provides seamless UX - user goes directly to dashboard
     *
     * @param email User's email address
     * @param password User's password
     */
    private fun performAutoLogin(email: String, password: String) {
        // Create login request
        val loginRequest = LoginRequest(email, password)

        // Make API call to login endpoint using global API service
        AppManager.getApiService().login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    // Extract token and user from response
                    val token = response.body()?.access_token
                    val user = response.body()?.user

                    if (token != null && user != null) {
                        // Save token and user to app manager
                        AppManager.setToken(this@SignUpActivity, token)
                        AppManager.setUser(user)

                        // Navigate to appropriate dashboard
                        navigateToDashboard(user.role, user.fullName ?: "User", user.email)
                    }
                } else {
                    // Auto-login failed, redirect to sign in screen
                    Toast.makeText(
                        this@SignUpActivity,
                        "Account created! Please sign in.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Auto-login failed, redirect to sign in screen
                Toast.makeText(
                    this@SignUpActivity,
                    "Account created! Please sign in.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        })
    }

    /**
     * Navigates to appropriate dashboard based on user role
     * Clears back stack to prevent returning to sign up screen
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
            else -> Intent(this, DashboardActivity::class.java) // Default to landlord dashboard
        }

        // Pass user information to dashboard activity
        intent.putExtra("USER_TYPE", userRole)
        intent.putExtra("USER_NAME", userName)
        intent.putExtra("USER_EMAIL", userEmail)

        // Clear back stack so user can't navigate back to sign up screen
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start dashboard activity
        startActivity(intent)

        // Finish this activity (remove from back stack)
        finish()
    }
}
