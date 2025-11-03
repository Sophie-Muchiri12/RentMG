package com.example.rentmg.pages.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rentmg.R

class HomeFragment : Fragment() {

    private lateinit var welcomeText: TextView
    private lateinit var totalRentText: TextView
    private lateinit var unitsRentingText: TextView
    private lateinit var monthYearText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        loadData()
    }

    private fun initViews(view: View) {
        welcomeText = view.findViewById(R.id.welcome_text)
        totalRentText = view.findViewById(R.id.total_rent_text)
        unitsRentingText = view.findViewById(R.id.units_renting_text)
        monthYearText = view.findViewById(R.id.month_year_text)
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