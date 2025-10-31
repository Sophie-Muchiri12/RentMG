package com.example.rentmg.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.rentmg.R

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val firstNameInput = findViewById<EditText>(R.id.first_name_input)
        val lastNameInput = findViewById<EditText>(R.id.last_name_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val signUpButton = findViewById<Button>(R.id.sign_up_button)

        signUpButton.setOnClickListener {
            val firstName = firstNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && 
                email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}