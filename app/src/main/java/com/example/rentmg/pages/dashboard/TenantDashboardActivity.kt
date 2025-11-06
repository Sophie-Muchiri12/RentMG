package com.example.rentmg.pages.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rentmg.R

class TenantDashboardActivity : AppCompatActivity() {
    
    private var userType: String? = null
    private var userName: String? = null
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_dashboard)
        
        // Retrieve user data from Intent
        retrieveUserData()
        
        // Load the fragment only on first creation
        if (savedInstanceState == null) {
            val bundle = Bundle().apply {
                putString("USER_TYPE", userType)
                putString("USER_NAME", userName)
                putString("USER_EMAIL", userEmail)
            }
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TenantDashboardFragment::class.java, bundle)
                .commit()
        }
    }

    private fun retrieveUserData() {
        // Get data from Intent extras passed from SignInActivity or SignUpActivity
        userType = intent.getStringExtra("USER_TYPE") ?: "tenant"
        userName = intent.getStringExtra("USER_NAME") ?: "Tenant"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        
        // Log the received data for debugging
        println("=== TENANT DASHBOARD ACTIVITY ===")
        println("User Type: $userType")
        println("User Name: $userName")
        println("User Email: $userEmail")
        println("==================================")
    }

    // Public methods to access user data from Fragment if needed
    fun getUserType(): String = userType ?: "tenant"
    fun getUserName(): String = userName ?: "Tenant"
    fun getUserEmail(): String = userEmail ?: ""
}