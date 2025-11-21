package com.example.rentmg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rentmg.databinding.ActivityMainBinding
import com.example.rentmg.pages.auth.SignInActivity
import com.example.rentmg.pages.auth.SignUpActivity
import com.example.rentmg.util.AppManager

/**
 * MainActivity
 * Landing page of the application
 *
 * This is the entry point activity that:
 * - Initializes global app state and API client
 * - Provides navigation to Sign In and Sign Up screens
 * - Uses View Binding for type-safe view access
 *
 * The activity is displayed when:
 * - App is first launched
 * - User logs out
 * - User clears app from back stack
 */
class MainActivity : AppCompatActivity() {

    // View Binding object for type-safe view access
    // Automatically generated from activity_main.xml
    private lateinit var binding: ActivityMainBinding

    /**
     * Activity lifecycle: onCreate
     * Called when activity is first created
     * Initializes app manager and sets up UI
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AppManager singleton
        // This loads any saved JWT token and prepares API client
        // Should be called before any API calls are made
        AppManager.initialize(this)

        // Inflate layout using View Binding
        // This provides type-safe access to views without findViewById()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up button click listeners
        setupListeners()
    }

    /**
     * Sets up click listeners for all interactive elements
     * Navigates to appropriate authentication screens
     */
    private fun setupListeners() {
        // Sign In button - navigates to SignInActivity
        binding.signInButton.setOnClickListener {
            // Create intent to launch SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        // Sign Up button - navigates to SignUpActivity
        binding.signUpButton.setOnClickListener {
            // Create intent to launch SignUpActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}