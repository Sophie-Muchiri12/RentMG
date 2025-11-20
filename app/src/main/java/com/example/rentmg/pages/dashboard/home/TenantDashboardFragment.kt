package com.example.rentmg.pages.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.rentmg.R
import com.example.rentmg.data.model.Lease
import com.example.rentmg.data.model.Payment
import com.example.rentmg.data.model.Property
import com.example.rentmg.data.model.Unit
import com.example.rentmg.pages.payment.CheckoutActivity
import com.example.rentmg.util.AppManager
import retrofit2.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * TenantDashboardFragment
 * Main dashboard screen for tenants
 *
 * Features:
 * - Displays current rent status (paid/unpaid)
 * - Shows property and unit details
 * - Provides "Pay Rent" button for unpaid rent
 * - Shows last payment information
 * - Fetches real data from backend API
 *
 * Data Flow:
 * 1. Fragment loads
 * 2. Fetches tenant's lease from API
 * 3. Fetches unit details for the lease
 * 4. Fetches property details for the unit
 * 5. Checks payment status
 * 6. Displays all information in UI
 */
class TenantDashboardFragment : Fragment() {

    // ============================================
    // UI COMPONENTS - Greeting Section
    // ============================================
    private lateinit var tvNameGreeting: TextView
    private lateinit var tvGreeting: TextView

    // ============================================
    // UI COMPONENTS - Rent Status Card
    // ============================================
    private lateinit var cardRentStatus: LinearLayout
    private lateinit var ivRentStatusIcon: ImageView
    private lateinit var tvRentStatusTitle: TextView
    private lateinit var tvRentStatusBadge: TextView
    private lateinit var tvPaymentNotice: TextView
    private lateinit var tvDueDateDisplay: TextView
    private lateinit var btnProceedPayment: Button

    // ============================================
    // UI COMPONENTS - Property Details
    // ============================================
    private lateinit var tvPropertyName: TextView
    private lateinit var tvHouseNumber: TextView
    private lateinit var tvBedrooms: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var tvMonthlyRent: TextView

    // ============================================
    // UI COMPONENTS - Recent Payment
    // ============================================
    private lateinit var containerLastPayment: LinearLayout
    private lateinit var tvLastPaymentDate: TextView
    private lateinit var tvLastPaymentAmount: TextView

    // ============================================
    // UI COMPONENTS - Loading State
    // ============================================
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: Button

    // ============================================
    // DATA - API Response Objects
    // ============================================
    private var currentLease: Lease? = null
    private var currentUnit: Unit? = null
    private var currentProperty: Property? = null
    private var lastPayment: Payment? = null
    private var isRentPaid: Boolean = false

    // ============================================
    // DATA - Formatters
    // ============================================
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var currencyFormat: NumberFormat
    private lateinit var isoDateParser: SimpleDateFormat

    // ============================================
    // DATA - User Information
    // ============================================
    private var userType: String? = null
    private var userName: String? = null
    private var userEmail: String? = null

    /**
     * Fragment lifecycle: onCreateView
     * Inflates the fragment layout
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tenant_dashboard, container, false)
    }

    /**
     * Fragment lifecycle: onViewCreated
     * Called after view is created
     * Initializes everything and loads data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user data from arguments bundle (passed from activity)
        getUserDataFromActivity()

        // Initialize all UI components
        initializeViews(view)

        // Initialize date and currency formatters
        initializeFormatters()

        // Setup button click listeners
        setupListeners()

        // Fetch tenant data from backend API
        loadTenantData()
    }

    /**
     * Retrieves user data from fragment arguments
     * This data was passed from TenantDashboardActivity
     */
    private fun getUserDataFromActivity() {
        userType = arguments?.getString("USER_TYPE")
        userName = arguments?.getString("USER_NAME")
        userEmail = arguments?.getString("USER_EMAIL")
    }

    /**
     * Initializes all UI components by finding them by ID
     * Called once during onViewCreated
     */
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

        // Loading State
        progressBar = view.findViewById(R.id.progress_bar)
        contentLayout = view.findViewById(R.id.content_layout)
        errorLayout = view.findViewById(R.id.error_layout)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)
        btnRetry = view.findViewById(R.id.btn_retry)
    }

    /**
     * Initializes date and currency formatters
     * Used for displaying dates and money amounts
     */
    private fun initializeFormatters() {
        // Date formatter for Kenyan locale (e.g., "15 Jan 2025")
        dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        isoDateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        // Currency formatter for Kenyan Shillings (e.g., "KES 25,000.00")
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        currencyFormat.currency = Currency.getInstance("KES")
    }

    /**
     * Sets up click listeners for interactive elements
     */
    private fun setupListeners() {
        // Pay Rent button - navigates to checkout
        btnProceedPayment.setOnClickListener {
            navigateToCheckout()
        }

        // Retry button - retries loading data after error
        btnRetry.setOnClickListener {
            loadTenantData()
        }
    }

    /**
     * Loads tenant data from backend API
     * Fetches lease, unit, and property information
     * Shows loading state while fetching
     */
    private fun loadTenantData() {
        // Show loading state
        showLoading()

        // Step 1: Fetch tenant's lease from API
        // The backend returns only leases for the logged-in user
        AppManager.getApiService().listLeases().enqueue(object : Callback<List<Lease>> {
            override fun onResponse(call: Call<List<Lease>>, response: Response<List<Lease>>) {
                if (response.isSuccessful) {
                    // Get list of leases
                    val leases = response.body()

                    if (leases != null && leases.isNotEmpty()) {
                        // Get the first active lease
                        // In a real app, you might want to filter for active leases only
                        currentLease = leases.firstOrNull { it.status == "active" } ?: leases.first()

                        // Step 2: Fetch unit details using unit_id from lease
                        loadUnitData(currentLease!!.unitId)
                    } else {
                        // No leases found for this tenant
                        showError("No active lease found. Please contact your landlord.")
                    }
                } else {
                    // API returned error
                    showError("Failed to load lease data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Lease>>, t: Throwable) {
                // Network error
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Loads unit details from backend API
     * Called after lease is fetched
     *
     * @param unitId ID of the unit to fetch
     */
    private fun loadUnitData(unitId: Int) {
        AppManager.getApiService().getUnit(unitId).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    currentUnit = response.body()
                    val propertyId = currentUnit?.propertyId
                    if (propertyId != null) {
                        loadPropertyData(propertyId)
                    } else {
                        showError("Unit is missing property information.")
                    }
                } else {
                    showError("Failed to load unit data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Loads property data from backend API
     *
     * @param propertyId ID of the property to fetch
     */
    private fun loadPropertyData(propertyId: Int) {
        AppManager.getApiService().getProperty(propertyId).enqueue(object : Callback<Property> {
            override fun onResponse(call: Call<Property>, response: Response<Property>) {
                if (response.isSuccessful) {
                    currentProperty = response.body()
                    // Load latest payment after property is available
                    loadLastPayment()
                } else {
                    showError("Failed to load property: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Property>, t: Throwable) {
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Fetches latest payment for this lease to determine status
     */
    private fun loadLastPayment() {
        val leaseId = currentLease?.id
        if (leaseId == null) {
            displayTenantData()
            return
        }

        AppManager.getApiService().getPaymentHistory(leaseId).enqueue(object : Callback<List<Payment>> {
            override fun onResponse(call: Call<List<Payment>>, response: Response<List<Payment>>) {
                if (response.isSuccessful) {
                    val payments = response.body().orEmpty()
                    lastPayment = payments.firstOrNull()
                    isRentPaid = false

                    lastPayment?.let { payment ->
                        val paidStatus = payment.status.equals("completed", true) || payment.status.equals("success", true)
                        val paidDate = parseIsoDate(payment.updatedAt ?: payment.createdAt)
                        if (paidStatus && paidDate != null) {
                            val now = Calendar.getInstance()
                            val paidCal = Calendar.getInstance().apply { time = paidDate }
                            isRentPaid = paidCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                    paidCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                        }
                    }
                }

                displayTenantData()
            }

            override fun onFailure(call: Call<List<Payment>>, t: Throwable) {
                // Even if payment history fails, show available data
                displayTenantData()
            }
        })
    }

    /**
     * Displays all tenant data in the UI
     * Called after all data is fetched successfully
     */
    private fun displayTenantData() {
        // Hide loading, show content
        showContent()

        // Display greeting
        val firstName = userName?.split(" ")?.firstOrNull() ?: "Tenant"
        tvNameGreeting.text = "Hi, $firstName"
        tvGreeting.text = "Good ${getTimeOfDayGreeting()}"

        // Display property details
        tvPropertyName.text = currentProperty?.name ?: "Property"
        tvHouseNumber.text = currentUnit?.code ?: "Unit"
        tvBedrooms.text = currentProperty?.address ?: "No address on file"
        tvMonthlyRent.text = currencyFormat.format(currentUnit?.rentAmount ?: 0.0)

        // Calculate due date (5th of each month for example)
        val calendar = Calendar.getInstance()
        val dueDay = currentLease?.startDate?.split("-")?.lastOrNull()?.toIntOrNull() ?: 5
        calendar.set(Calendar.DAY_OF_MONTH, dueDay)
        tvDueDate.text = "Every ${dueDay}th of the month"
        tvDueDateDisplay.text = dateFormat.format(calendar.time)

        // Update rent status card based on payment
        updateRentStatusCard(isRentPaid)

        // Display last payment if available
        lastPayment?.let { payment ->
            containerLastPayment.visibility = View.VISIBLE
            tvLastPaymentAmount.text = currencyFormat.format(payment.amount)
            val paymentDate = parseIsoDate(payment.updatedAt ?: payment.createdAt)
            tvLastPaymentDate.text = paymentDate?.let { dateFormat.format(it) } ?: payment.createdAt
        } ?: run {
            containerLastPayment.visibility = View.GONE
        }
    }

    /**
     * Updates the rent status card based on payment status
     * Shows whether rent is paid or overdue
     */
    private fun updateRentStatusCard(isPaid: Boolean) {
        val isPending = lastPayment?.status?.equals("pending", true) == true

        // Set card color based on status
        val backgroundColor = when {
            isPaid -> ContextCompat.getColor(requireContext(), R.color.success)
            isPending -> ContextCompat.getColor(requireContext(), R.color.warning)
            else -> ContextCompat.getColor(requireContext(), R.color.primary_red)
        }
        cardRentStatus.setBackgroundColor(backgroundColor)

        // Set status text
        tvRentStatusTitle.text = when {
            isPaid -> "Rent Paid"
            isPending -> "Awaiting Confirmation"
            else -> "Rent Due"
        }
        tvRentStatusBadge.text = when {
            isPaid -> "Paid"
            isPending -> "Pending"
            else -> "Unpaid"
        }

        // Set status icon
        val iconRes = if (isPaid) R.drawable.ic_check_circle else R.drawable.ic_warning
        ivRentStatusIcon.setImageResource(iconRes)

        // Show/hide payment button
        when {
            isPaid -> {
                btnProceedPayment.visibility = View.GONE
                tvPaymentNotice.visibility = View.GONE
                tvDueDateDisplay.visibility = View.GONE
            }
            isPending -> {
                btnProceedPayment.visibility = View.GONE
                tvPaymentNotice.visibility = View.VISIBLE
                tvPaymentNotice.text = "Processing your payment..."
                tvDueDateDisplay.visibility = View.VISIBLE
            }
            else -> {
                btnProceedPayment.visibility = View.VISIBLE
                tvPaymentNotice.visibility = View.VISIBLE
                tvPaymentNotice.text = "Payment Due:"
                tvDueDateDisplay.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Navigates to checkout screen for payment
     * Passes property, unit, and lease information
     */
    private fun navigateToCheckout() {
        if (currentLease == null || currentUnit == null || currentProperty == null) {
            Toast.makeText(requireContext(), "Error: Property data not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        // Create intent to launch CheckoutActivity
        val intent = Intent(requireContext(), CheckoutActivity::class.java).apply {
            // Pass lease ID (required for payment API)
            putExtra("LEASE_ID", currentLease!!.id)

            // Pass property and unit information
            putExtra("PROPERTY_NAME", currentProperty!!.name)
            putExtra("UNIT_NUMBER", currentUnit!!.code)

            // Pass rent amount
            putExtra("RENT_AMOUNT", currentUnit!!.rentAmount)

            // Pass transaction fee (0 for now)
            putExtra("TRANSACTION_FEE", 0.0)

            // Pass user information
            putExtra("USER_NAME", userName)
            putExtra("USER_EMAIL", userEmail)
        }

        // Start checkout activity
        startActivity(intent)
    }

    /**
     * Shows loading state
     * Hides content and error, shows progress bar
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    /**
     * Shows content state
     * Hides loading and error, shows content
     */
    private fun showContent() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
    }

    /**
     * Shows error state
     * Hides loading and content, shows error message
     *
     * @param message Error message to display
     */
    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvErrorMessage.text = message
    }

    /**
     * Gets appropriate greeting based on time of day
     *
     * @return "Morning", "Afternoon", or "Evening"
     */
    private fun getTimeOfDayGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Morning"
            in 12..16 -> "Afternoon"
            else -> "Evening"
        }
    }

    /**
     * Safely parse ISO date strings returned by the API
     */
    private fun parseIsoDate(raw: String?): Date? {
        if (raw.isNullOrBlank()) return null
        return try {
            isoDateParser.parse(raw.replace("Z", ""))
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fragment lifecycle: onResume
     * Called when fragment becomes visible
     * Refresh data in case payment was made
     */
    override fun onResume() {
        super.onResume()
        // Refresh payment status when returning from checkout
        if (currentLease != null) {
            loadLastPayment()
        }
    }
}
