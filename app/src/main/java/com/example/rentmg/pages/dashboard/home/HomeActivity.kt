package com.example.rentmg.dashboard.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.rentmg.R

class HomeActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var totalRentText: TextView
    private lateinit var unitsRentingText: TextView
    private lateinit var monthYearText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_home)

        supportActionBar?.hide()

        // Handle status bar insets
        findViewById<android.view.View>(android.R.id.content).setOnApplyWindowInsetsListener { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.updatePadding(top = statusBarHeight)
            insets
        }

        initViews()
        loadData()
    }

    private fun initViews() {
        welcomeText = findViewById(R.id.welcome_text)
        totalRentText = findViewById(R.id.total_rent_text)
        unitsRentingText = findViewById(R.id.units_renting_text)
        monthYearText = findViewById(R.id.month_year_text)
    }

    private fun loadData() {
        val userName = "Griffin Otieno"
        welcomeText.text = "Welcome, $userName"

        val currentMonth = getCurrentMonth()
        monthYearText.text = currentMonth

        val totalRent = 150000.0
        val unitsRenting = 12
        val totalUnits = 15

        totalRentText.text = formatCurrency(totalRent)
        unitsRentingText.text = "$unitsRenting / $totalUnits Units"
    }

    private fun formatCurrency(amount: Double): String {
        return "KES ${String.format("%,.2f", amount)}"
    }

    private fun getCurrentMonth(): String {
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val calendar = java.util.Calendar.getInstance()
        val month = months[calendar.get(java.util.Calendar.MONTH)]
        val year = calendar.get(java.util.Calendar.YEAR)
        return "$month $year"
    }
}