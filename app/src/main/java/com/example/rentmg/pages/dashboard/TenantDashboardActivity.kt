package com.example.rentmg.pages.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.rentmg.MainActivity
import com.example.rentmg.R
import com.example.rentmg.pages.dashboard.tenant.HistoryFragment
import com.example.rentmg.pages.dashboard.ProfileFragment
import com.example.rentmg.pages.dashboard.ReceiptsFragment
import com.example.rentmg.pages.dashboard.TenantDashboardFragment
import com.example.rentmg.util.AppManager
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * TenantDashboardActivity
 * Container activity for tenant dashboard screens
 *
 * Features:
 * - Bottom navigation for different tenant features
 * - Fragment container for switching between screens
 * - Passes user data to all fragments
 * - Handles logout functionality
 *
 * Navigation Items:
 * - Dashboard: Shows rent status, property info, payment button
 * - History: Shows payment transaction history
 * - Receipts: Shows downloadable payment receipts
 * - Profile: Shows tenant profile and settings
 */
class TenantDashboardActivity : AppCompatActivity() {

    // UI components
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbarTitle: TextView

    // User data received from login/signup
    private var userType: String? = null
    private var userName: String? = null
    private var userEmail: String? = null

    /**
     * Activity lifecycle: onCreate
     * Initializes UI and loads default fragment
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_dashboard)

        // Extract user data from intent
        // This data was passed from SignInActivity or SignUpActivity
        userType = intent.getStringExtra("USER_TYPE")
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")

        // Initialize all views by finding them by ID
        bottomNavigation = findViewById(R.id.bottom_navigation)
        toolbarTitle = findViewById(R.id.toolbar_title)

        // Setup bottom navigation click listeners
        setupBottomNavigation()

        // Load default fragment only on first creation
        // savedInstanceState is null when activity is first created
        // If not null, Android will restore fragments automatically
        if (savedInstanceState == null) {
            loadFragment(TenantDashboardFragment(), "Dashboard")
            // Highlight the dashboard item in bottom navigation
            bottomNavigation.selectedItemId = R.id.nav_dashboard
        }
    }

    /**
     * Sets up bottom navigation item click listeners
     * Each navigation item loads a different fragment
     */
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            // Variables to hold the fragment and title to load
            var fragment: Fragment? = null
            var title: String? = null

            // Determine which fragment to load based on selected item
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Dashboard: rent status and property info
                    fragment = TenantDashboardFragment()
                    title = "Dashboard"
                }
                R.id.nav_history -> {
                    // History: payment transaction history
                    fragment = HistoryFragment()
                    title = "Payment History"
                }
                R.id.nav_receipts -> {
                    // Receipts: downloadable payment receipts
                    fragment = ReceiptsFragment()
                    title = "Receipts"
                }
                R.id.nav_profile -> {
                    // Profile: tenant profile and settings with logout
                    fragment = ProfileFragment()
                    title = "Profile"
                }
            }

            // If valid fragment and title were determined, load the fragment
            if (fragment != null && title != null) {
                loadFragment(fragment, title)
                // Return true to indicate the item selection was handled
                return@setOnItemSelectedListener true
            }

            // Return false if item wasn't handled
            false
        }
    }

    /**
     * Loads a fragment into the container
     * Passes user data to the fragment via arguments bundle
     *
     * @param fragment Fragment instance to load
     * @param title Title to display in toolbar
     */
    private fun loadFragment(fragment: Fragment, title: String) {
        // Update toolbar title to reflect current screen
        toolbarTitle.text = title

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
            .addToBackStack(null) // Add to back stack so back button works
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