package com.example.rentmg.pages.dashboard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.rentmg.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class TenantDashboardActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbarTitle: TextView
    
    private var userType: String? = null
    private var userName: String? = null
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_dashboard)
        
        // Get user data from intent
        userType = intent.getStringExtra("USER_TYPE")
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        
        // Initialize views
        bottomNavigation = findViewById(R.id.bottom_navigation)
        toolbarTitle = findViewById(R.id.toolbar_title)
        
        // Setup bottom navigation listener
        setupBottomNavigation()
        
        // Load default fragment (Dashboard)
        if (savedInstanceState == null) {
            loadFragment(TenantDashboardFragment(), "Dashboard")
            // Set the default selected item
            bottomNavigation.selectedItemId = R.id.nav_dashboard
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            var fragment: Fragment? = null
            var title: String? = null
            
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    fragment = TenantDashboardFragment()
                    title = "Dashboard"
                }
                R.id.nav_history -> {
                    fragment = HistoryFragment()
                    title = "Payment History"
                }
                R.id.nav_receipts -> {
                    fragment = ReceiptsFragment()
                    title = "Receipts"
                }
                R.id.nav_profile -> {
                    fragment = ProfileFragment()
                    title = "Profile"
                }
            }
            
            if (fragment != null && title != null) {
                loadFragment(fragment, title)
                return@setOnItemSelectedListener true
            }
            
            false
        }
    }
    
    private fun loadFragment(fragment: Fragment, title: String) {
        // Update toolbar title
        toolbarTitle.text = title
        
        // Pass user data to fragment
        val bundle = Bundle().apply {
            putString("USER_TYPE", userType)
            putString("USER_NAME", userName)
            putString("USER_EMAIL", userEmail)
        }
        fragment.arguments = bundle
        
        // Replace fragment with proper transaction
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}