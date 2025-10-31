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
import com.example.rentmg.dashboard.DashboardActivity

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
        
        // TODO: Make API call here
        signInButton.postDelayed({
            val userType = "landlord" // This should come from API response
            
            Toast.makeText(this, "Sign In Success!", Toast.LENGTH_SHORT).show()
            
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_TYPE", userType)
            startActivity(intent)
            finish()
            
            isLoading = false
            signInButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }, 2000)
    }
}