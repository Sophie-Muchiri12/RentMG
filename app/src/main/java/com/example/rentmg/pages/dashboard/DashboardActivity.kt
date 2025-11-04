package com.example.rentmg.pages.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.rentmg.R
import com.example.rentmg.pages.dashboard.home.HomeFragment
import com.example.rentmg.pages.dashboard.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {
    
    private var userType: String = "landlord"
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_dashboard)

        supportActionBar?.hide()

        findViewById<android.view.View>(android.R.id.content).setOnApplyWindowInsetsListener { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.updatePadding(top = statusBarHeight)
            insets
        }

        userType = intent.getStringExtra("USER_TYPE") ?: "landlord"

        bottomNav = findViewById(R.id.bottom_navigation)
        
        setupBottomNavigation()
        
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun setupBottomNavigation() {
        when (userType) {
            "property manager", "tenant" -> {
                bottomNav.menu.removeItem(R.id.nav_billing)
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_financials -> {
                    true
                }
                R.id.nav_properties -> {
                    true
                }
                R.id.nav_billing -> {
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}