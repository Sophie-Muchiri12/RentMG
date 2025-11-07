package com.example.rentmg.pages.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.rentmg.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class TenantInfo(
    val name: String,
    val propertyName: String,
    val houseNumber: String,
    val bedrooms: Int,
    val monthlyRent: Double,
    val dueDate: Date,
    val isPaid: Boolean,
    val balance: Double,
    val lastPaymentDate: Date?
)

class TenantDashboardFragment : Fragment() {
    
    // Views - Greeting Section
    private lateinit var tvNameGreeting: TextView
    private lateinit var tvGreeting: TextView
    
    // Views - Rent Status Card
    private lateinit var cardRentStatus: LinearLayout
    private lateinit var ivRentStatusIcon: ImageView
    private lateinit var tvRentStatusTitle: TextView
    private lateinit var tvRentStatusBadge: TextView
    private lateinit var tvPaymentNotice: TextView
    private lateinit var tvDueDateDisplay: TextView
    private lateinit var btnProceedPayment: Button
    
    // Views - Property Details
    private lateinit var tvPropertyName: TextView
    private lateinit var tvHouseNumber: TextView
    private lateinit var tvBedrooms: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var tvMonthlyRent: TextView
    
    // Views - Recent Payment
    private lateinit var containerLastPayment: LinearLayout
    private lateinit var tvLastPaymentDate: TextView
    private lateinit var tvLastPaymentAmount: TextView
    
    // Data
    private lateinit var tenant: TenantInfo
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var currencyFormat: NumberFormat
    
    // User data from Activity
    private var userType: String? = null
    private var userName: String? = null
    private var userEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tenant_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Retrieve user data from Bundle arguments
        getUserDataFromActivity()
        
        // Initialize all views
        initializeViews(view)
        
        // Initialize formatters and tenant data
        initializeData()
        
        // Populate UI with data
        populateUI()
        
        // Setup event listeners
        setupListeners()
    }

    private fun getUserDataFromActivity() {
        // Get data from Bundle passed by Activity
        userType = arguments?.getString("USER_TYPE")
        userName = arguments?.getString("USER_NAME")
        userEmail = arguments?.getString("USER_EMAIL")
        
        println("=== TENANT DASHBOARD FRAGMENT ===")
        println("User Type: $userType")
        println("User Name: $userName")
        println("User Email: $userEmail")
        println("==================================")
    }

    private fun initializeViews(view: View) {
        // Greeting Section
        tvNameGreeting = view.findViewById(R.id.tv_name_greeting)
        tvGreeting = view.findViewById(R.id.tv_greeting)
        
        // Rent Status Card
        cardRentStatus = view.findViewById(R.id.card_rent_status)
        ivRentStatusIcon = view.findViewById(R.id.iv_rent_status_icon)
        tvRentStatusTitle = view.findViewById(R.id.tv_rent_status_title)
        tvRentStatusBadge = view.findViewById(R.id.tv_rent_status_badge)
        tvPaymentNotice = view.findViewById(R.id.tv_payment_notice)
        tvDueDateDisplay = view.findViewById(R.id.tv_due_date_display)
        btnProceedPayment = view.findViewById(R.id.btn_proceed_payment)
        
        // Property Details
        tvPropertyName = view.findViewById(R.id.tv_property_name)
        tvHouseNumber = view.findViewById(R.id.tv_house_number)
        tvBedrooms = view.findViewById(R.id.tv_bedrooms)
        tvDueDate = view.findViewById(R.id.tv_due_date)
        tvMonthlyRent = view.findViewById(R.id.tv_monthly_rent)
        
        // Recent Payment
        containerLastPayment = view.findViewById(R.id.container_last_payment)
        tvLastPaymentDate = view.findViewById(R.id.tv_last_payment_date)
        tvLastPaymentAmount = view.findViewById(R.id.tv_last_payment_amount)
    }

    private fun initializeData() {
        // Initialize date and currency formatters
        dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        
        // Use real user name from auth if available, otherwise use default
        val displayName = userName ?: "Sophie Mwangi"
        
        // Create tenant data object
        tenant = TenantInfo(
            name = displayName,
            propertyName = "Greenview Apartments",
            houseNumber = "A-102",
            bedrooms = 2,
            monthlyRent = 25000.0,
            dueDate = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 5)
            }.time,
            isPaid = false,
            balance = 25000.0,
            lastPaymentDate = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
            }.time
        )
    }

    private fun populateUI() {
        // Greeting Section
        tvNameGreeting.text = "Hi, ${tenant.name.split(" ")[0]}"
        tvGreeting.text = "Good ${getGreeting()}"
        
        // Rent Status Card
        updateRentStatusCard()
        
        // Property Details
        tvPropertyName.text = tenant.propertyName
        tvHouseNumber.text = tenant.houseNumber
        tvBedrooms.text = "${tenant.bedrooms} Bedroom"
        tvDueDate.text = "Every ${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)}${getDaySuffix(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))} of the month"
        tvMonthlyRent.text = currencyFormat.format(tenant.monthlyRent)
        
        // Recent Payment Section
        if (tenant.lastPaymentDate != null) {
            containerLastPayment.visibility = View.VISIBLE
            tvLastPaymentDate.text = dateFormat.format(tenant.lastPaymentDate)
            tvLastPaymentAmount.text = currencyFormat.format(tenant.monthlyRent)
        } else {
            containerLastPayment.visibility = View.GONE
        }
    }

    private fun updateRentStatusCard() {
        val backgroundColor = if (tenant.isPaid) {
            ContextCompat.getColor(requireContext(), R.color.success)
        } else {
            ContextCompat.getColor(requireContext(), R.color.primary_red)
        }
        
        // Set card background color
        cardRentStatus.setBackgroundColor(backgroundColor)
        
        // Set status title
        tvRentStatusTitle.text = if (tenant.isPaid) "Rent Paid" else "Rent Overdue"
        
        // Set badge text
        tvRentStatusBadge.text = if (tenant.isPaid) "Paid" else "Overdue"
        
        // Set status icon
        val iconRes = if (tenant.isPaid) R.drawable.ic_check_circle else R.drawable.ic_warning
        ivRentStatusIcon.setImageResource(iconRes)
        
        // Show/hide payment button
        if (tenant.isPaid) {
            btnProceedPayment.visibility = View.GONE
            tvPaymentNotice.visibility = View.GONE
            tvDueDateDisplay.visibility = View.GONE
        } else {
            btnProceedPayment.visibility = View.VISIBLE
            tvPaymentNotice.visibility = View.VISIBLE
            tvDueDateDisplay.visibility = View.VISIBLE
            tvDueDateDisplay.text = dateFormat.format(tenant.dueDate)
        }
    }

    private fun setupListeners() {
        btnProceedPayment.setOnClickListener {
            // TODO: Navigate to payment screen
            // findNavController().navigate(R.id.action_dashboard_to_payment)
            println("Proceed to Payment clicked")
        }
    }

    private fun getGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Morning"
            in 12..16 -> "Afternoon"
            else -> "Evening"
        }
    }

    private fun getDaySuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}