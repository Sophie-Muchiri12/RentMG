package com.example.rentmg.pages.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.rentmg.MainActivity
import com.example.rentmg.R
import com.example.rentmg.pages.dashboard.home.HomeFragment
import com.example.rentmg.pages.dashboard.settings.SettingsFragment
import com.example.rentmg.pages.dashboard.tenant.ProfileFragment
import com.example.rentmg.util.AppManager
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * DashboardActivity
 * Container activity for landlord/property manager dashboard screens
 *
 * Features:
 * - Bottom navigation for different landlord features
 * - Fragment container for switching between screens
 * - Passes user data to all fragments
 * - Handles logout functionality
 *
 * Navigation Items:
 * - Home: Shows dashboard with statistics and recent activities
 * - Financials: Shows financial reports and analytics (future)
 * - Properties: Shows property list and management
 * - Billing: Shows billing and invoicing (future)
 * - Settings: Shows settings and profile
 */
class DashboardActivity : AppCompatActivity() {

    // UI components
    private lateinit var bottomNav: BottomNavigationView

    // User data received from login/signup
    private var userType: String = "landlord"
    private var userName: String? = null
    private var userEmail: String? = null

    /**
     * Activity lifecycle: onCreate
     * Initializes UI and loads default fragment
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display (full screen)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set layout
        setContentView(R.layout.activity_dashboard)

        // Hide action bar for cleaner UI
        supportActionBar?.hide()

        // Handle system window insets (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Get system bars insets (status bar and navigation bar)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding to avoid content being hidden behind system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Extract user data from intent
        // This data was passed from SignInActivity or SignUpActivity
        userType = intent.getStringExtra("USER_TYPE") ?: "landlord"
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")

        // Initialize bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation)

        // Setup bottom navigation click listeners
        setupBottomNavigation()

        // Load default fragment only on first creation
        // savedInstanceState is null when activity is first created
        if (savedInstanceState == null) {
            // Start with Home fragment
            loadFragment(HomeFragment(), "Home")
            // Highlight the home item in bottom navigation
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    /**
     * Sets up bottom navigation item click listeners
     * Each navigation item loads a different fragment
     * Dynamically adjusts menu based on user type
     */
    private fun setupBottomNavigation() {
        // Remove billing for non-landlords (property managers)
        when (userType) {
            "property_manager" -> {
                // Property managers don't handle billing
                bottomNav.menu.removeItem(R.id.nav_billing)
            }
        }

        // Set up navigation item click listener
        bottomNav.setOnItemSelectedListener { item ->
            // Determine which fragment to load based on selected item
            when (item.itemId) {
                R.id.nav_home -> {
                    // Home: dashboard with statistics
                    loadFragment(HomeFragment(), "Dashboard")
                    true
                }
                R.id.nav_financials -> {
                    // Financials: financial reports (future implementation)
                    // TODO: Create FinancialsFragment with charts and reports
                    // For now, stay on current fragment
                    true
                }
                R.id.nav_properties -> {
                    // Properties: property management
                    loadFragment(PropertiesFragment(), "Properties")
                    true
                }
                R.id.nav_billing -> {
                    // Billing: invoicing and billing (future implementation)
                    // TODO: Create BillingFragment
                    // For now, stay on current fragment
                    true
                }
                R.id.nav_settings -> {
                    // Settings: use ProfileFragment (shared with tenants)
                    loadFragment(ProfileFragment(), "Profile & Settings")
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Loads a fragment into the container
     * Passes user data to the fragment via arguments bundle
     *
     * @param fragment Fragment instance to load
     * @param title Title for the screen (not currently displayed, for future toolbar)
     */
    private fun loadFragment(fragment: Fragment, title: String) {
        // Create a bundle with user data to pass to fragment
        val bundle = Bundle().apply {
            putString("USER_TYPE", userType)
            putString("USER_NAME", userName)
            putString("USER_EMAIL", userEmail)
        }
        // Attach bundle to fragment (accessed via fragment.arguments)
        fragment.arguments = bundle

        // Replace current fragment with new one
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace fragment in container
            .commit() // Execute the transaction
    }

    /**
     * Handles logout from ProfileFragment
     * Called when user clicks logout button
     */
    fun performLogout() {
        // Clear user session and tokens using AppManager
        AppManager.logout(this)

        // Navigate to MainActivity (login screen)
        val intent = Intent(this, MainActivity::class.java)
        // Clear all activities from back stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Finish this activity
        finish()
    }
}