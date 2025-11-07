package com.example.rentmg.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.view.View
import com.example.rentmg.R
import com.example.rentmg.pages.dashboard.DashboardActivity
import com.example.rentmg.pages.dashboard.TenantDashboardActivity

class SignInActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signInButton: Button
    private lateinit var progressBar: ProgressBar

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        supportActionBar?.hide()

        initViews()
        setupListeners()
    }

    private fun initViews() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        signInButton = findViewById(R.id.sign_in_button)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        signInButton.setOnClickListener {
            if (!isLoading) {
                validateAndSignIn()
            }
        }
    }

    private fun validateAndSignIn() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            return
        }

        performSignIn()
    }

    private fun performSignIn() {
        isLoading = true
        signInButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        signInButton.postDelayed({
            val email = emailInput.text.toString().trim()

            // Determine user type from API response or email
            // For demo: if email contains "tenant" -> redirect to TenantDashboard
            val userType = if (email.contains("tenant", ignoreCase = true)) {
                "tenant"
            } else {
                "landlord" // Landlord/Property Manager
            }

            Toast.makeText(this, "Sign In Success!", Toast.LENGTH_SHORT).show()

            // Route to appropriate dashboard based on user type
            navigateToDashboard(userType)

            isLoading = false
        }, 2000)
    }

    private fun navigateToDashboard(userType: String) {
        val intent = when (userType) {
            "tenant" -> Intent(this, TenantDashboardActivity::class.java)
            else -> Intent(this, DashboardActivity::class.java)
        }

        // Pass user type to the dashboard
        intent.putExtra("USER_TYPE", userType)
        intent.putExtra("USER_EMAIL", emailInput.text.toString().trim())

        // Clear back stack so user can't go back to sign in
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}