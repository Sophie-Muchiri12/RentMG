package com.example.rentmg.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rentmg.R
import com.example.rentmg.dashboard.home.HomeActivity
import com.example.rentmg.dashboard.settings.SettingsActivity

class DashboardActivity : AppCompatActivity() {
    
    private var userType: String = "landlord"
    private var currentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        supportActionBar?.hide()

        userType = intent.getStringExtra("USER_TYPE") ?: "landlord"

        navigateToScreen(0)
    }

    private fun navigateToScreen(index: Int) {
        currentIndex = index
        
        when (userType) {
            "admin" -> {
                when (index) {
                    0 -> startActivity(Intent(this, HomeActivity::class.java))
                    3 -> startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            "landlord" -> {
                when (index) {
                    0 -> startActivity(Intent(this, HomeActivity::class.java))
                    4 -> startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            "property manager", "tenant" -> {
                when (index) {
                    0 -> startActivity(Intent(this, HomeActivity::class.java))
                    3 -> startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
        }
    }
}