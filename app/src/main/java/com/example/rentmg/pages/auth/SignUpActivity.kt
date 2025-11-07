package com.example.rentmg.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import com.example.rentmg.R
import com.example.rentmg.pages.dashboard.DashboardActivity
import com.example.rentmg.pages.dashboard.TenantDashboardActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var userTypeSpinner: Spinner
    private lateinit var passwordInput: EditText
    private lateinit var passwordConfirmInput: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView

    private var selectedUserType: String = "landlord" // Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        supportActionBar?.hide()

        initViews()
        setupUserTypeSpinner()
        setupListeners()
    }

    private fun initViews() {
        firstNameInput = findViewById(R.id.first_name_input)
        lastNameInput = findViewById(R.id.last_name_input)
        usernameInput = findViewById(R.id.username_input)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        userTypeSpinner = findViewById(R.id.user_type_spinner)
        passwordInput = findViewById(R.id.password_input)
        passwordConfirmInput = findViewById(R.id.password_confirm_input)
        signUpButton = findViewById(R.id.sign_up_button)
        signInText = findViewById(R.id.sign_in_text)
    }

    private fun setupUserTypeSpinner() {
        val userTypes = arrayOf("Landlord", "Property Manager", "Tenant")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapter

        userTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedUserType = when (position) {
                    0 -> "landlord"
                    1 -> "property_manager"
                    2 -> "tenant"
                    else -> "landlord"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedUserType = "landlord"
            }
        }
    }

    private fun setupListeners() {
        signUpButton.setOnClickListener {
            validateAndSignUp()
        }

        signInText.setOnClickListener {
            finish()
        }
    }

    private fun validateAndSignUp() {
        val firstName = firstNameInput.text.toString()
        val lastName = lastNameInput.text.toString()
        val username = usernameInput.text.toString()
        val email = emailInput.text.toString()
        val phone = phoneInput.text.toString()
        val password = passwordInput.text.toString()
        val passwordConfirm = passwordConfirmInput.text.toString()

        // Validation
        if (firstName.isEmpty()) {
            Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (lastName.isEmpty()) {
            Toast.makeText(this, "Last name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != passwordConfirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Make API call with selectedUserType
        performSignUp(firstName, lastName, username, email, phone, password)
    }

    private fun performSignUp(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        phone: String,
        password: String
    ) {
        // Log the data for debugging
        println("=== SIGN UP DATA ===")
        println("First Name: $firstName")
        println("Last Name: $lastName")
        println("Username: $username")
        println("Email: $email")
        println("Phone: $phone")
        println("User Type: $selectedUserType")
        println("==================")

        Toast.makeText(this, "Account created as $selectedUserType!", Toast.LENGTH_LONG).show()

        // Navigate to appropriate dashboard based on selected user type
        navigateToDashboard(selectedUserType, firstName, email)
    }

    private fun navigateToDashboard(userType: String, firstName: String, email: String) {
        val intent = when (userType) {
            "tenant" -> Intent(this, TenantDashboardActivity::class.java)
            "property_manager" -> Intent(this, DashboardActivity::class.java)
            else -> Intent(this, DashboardActivity::class.java) // Landlord
        }

        // Pass user information to dashboard
        intent.putExtra("USER_TYPE", userType)
        intent.putExtra("USER_NAME", firstName)
        intent.putExtra("USER_EMAIL", email)

        // Clear back stack so user can't navigate back
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}