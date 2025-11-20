package com.example.rentmg.pages.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rentmg.R
import com.example.rentmg.data.model.Property
import com.example.rentmg.data.model.Unit
import com.example.rentmg.data.model.Lease
import com.example.rentmg.data.model.Payment
import com.example.rentmg.util.AppManager
import retrofit2.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeFragment
 * Landlord dashboard home screen showing statistics and overview
 *
 * Features:
 * - Displays welcome message with landlord name
 * - Shows total properties count
 * - Shows occupied units vs total units
 * - Shows monthly revenue from rent payments
 * - Displays recent payment activities
 * - Fetches real data from backend API
 *
 * Data Flow:
 * 1. Fragment loads
 * 2. Fetches landlord's properties from API
 * 3. Fetches units for all properties
 * 4. Fetches leases to determine occupied units
 * 5. Fetches recent payments to calculate revenue
 * 6. Displays all statistics in UI
 */
class HomeFragment : Fragment() {

    // ============================================
    // UI COMPONENTS - Welcome Section
    // ============================================
    private lateinit var welcomeText: TextView
    private lateinit var monthYearText: TextView

    // ============================================
    // UI COMPONENTS - Statistics Cards
    // ============================================
    private lateinit var totalRentText: TextView
    private lateinit var unitsRentingText: TextView
    private lateinit var totalPropertiesText: TextView
    private lateinit var occupancyRateText: TextView

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
    private val properties = mutableListOf<Property>()
    private val allUnits = mutableListOf<Unit>()
    private val allLeases = mutableListOf<Lease>()
    private val recentPayments = mutableListOf<Payment>()

    // ============================================
    // DATA - Calculated Statistics
    // ============================================
    private var totalProperties: Int = 0
    private var totalUnits: Int = 0
    private var occupiedUnits: Int = 0
    private var monthlyRevenue: Double = 0.0

    // ============================================
    // DATA - Formatters
    // ============================================
    private lateinit var currencyFormat: NumberFormat
    private lateinit var dateFormat: SimpleDateFormat

    // ============================================
    // DATA - User Information
    // ============================================
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
        return inflater.inflate(R.layout.activity_home, container, false)
    }

    /**
     * Fragment lifecycle: onViewCreated
     * Called after view is created
     * Initializes everything and loads data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user data from arguments bundle (passed from activity)
        getUserDataFromArguments()

        // Initialize all UI components
        initializeViews(view)

        // Initialize formatters
        initializeFormatters()

        // Setup button click listeners
        setupListeners()

        // Fetch dashboard data from backend API
        loadDashboardData()
    }

    /**
     * Retrieves user data from fragment arguments
     * This data was passed from DashboardActivity
     */
    private fun getUserDataFromArguments() {
        userName = arguments?.getString("USER_NAME")
        userEmail = arguments?.getString("USER_EMAIL")
    }

    /**
     * Initializes all UI components by finding them by ID
     * Called once during onViewCreated
     */
    private fun initializeViews(view: View) {
        // Welcome Section
        welcomeText = view.findViewById(R.id.welcome_text)
        monthYearText = view.findViewById(R.id.month_year_text)

        // Statistics Cards
        totalRentText = view.findViewById(R.id.total_rent_text)
        unitsRentingText = view.findViewById(R.id.units_renting_text)
        totalPropertiesText = view.findViewById(R.id.total_properties_text)
        occupancyRateText = view.findViewById(R.id.occupancy_rate_text)

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
        // Currency formatter for Kenyan Shillings (e.g., "KES 25,000.00")
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))

        // Date formatter for Kenyan locale (e.g., "15 Jan 2025")
        dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    /**
     * Sets up click listeners for interactive elements
     */
    private fun setupListeners() {
        // Retry button - retries loading data after error
        btnRetry.setOnClickListener {
            loadDashboardData()
        }
    }

    /**
     * Loads dashboard data from backend API
     * Fetches properties, units, leases, and payments
     * Shows loading state while fetching
     */
    private fun loadDashboardData() {
        // Show loading state
        showLoading()

        // Step 1: Fetch landlord's properties from API
        // The backend returns only properties for the logged-in landlord
        AppManager.getApiService().listProperties().enqueue(object : Callback<List<Property>> {
            override fun onResponse(call: Call<List<Property>>, response: Response<List<Property>>) {
                if (response.isSuccessful) {
                    // Get list of properties
                    val propertiesList = response.body()

                    if (propertiesList != null) {
                        // Clear existing data
                        properties.clear()
                        properties.addAll(propertiesList)

                        // Update total properties count
                        totalProperties = properties.size

                        if (properties.isNotEmpty()) {
                            // Step 2: Fetch units for all properties
                            loadUnitsForProperties()
                        } else {
                            // No properties found
                            showEmptyState()
                        }
                    } else {
                        // API returned null body
                        showEmptyState()
                    }
                } else {
                    // API returned error
                    showError("Failed to load properties: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Property>>, t: Throwable) {
                // Network error
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Loads units for all properties
     * Called after properties are fetched
     * Fetches units for each property and aggregates them
     */
    private fun loadUnitsForProperties() {
        // Clear existing units
        allUnits.clear()

        // Counter to track how many property unit requests have completed
        var completedRequests = 0
        val totalRequests = properties.size

        // Fetch units for each property
        for (property in properties) {
            AppManager.getApiService().listUnits(property.id).enqueue(object : Callback<List<Unit>> {
                override fun onResponse(call: Call<List<Unit>>, response: Response<List<Unit>>) {
                    if (response.isSuccessful) {
                        val units = response.body()
                        if (units != null) {
                            // Add units from this property to the list
                            allUnits.addAll(units)
                        }
                    }

                    // Increment completed counter
                    completedRequests++

                    // If all requests completed, proceed to next step
                    if (completedRequests == totalRequests) {
                        // Update total units count
                        totalUnits = allUnits.size

                        // Step 3: Fetch leases to determine occupied units
                        loadLeases()
                    }
                }

                override fun onFailure(call: Call<List<Unit>>, t: Throwable) {
                    // Even if one request fails, increment counter to avoid blocking
                    completedRequests++

                    if (completedRequests == totalRequests) {
                        totalUnits = allUnits.size
                        loadLeases()
                    }
                }
            })
        }
    }

    /**
     * Loads all leases for the landlord
     * Used to determine which units are occupied
     */
    private fun loadLeases() {
        // Fetch all leases
        // Note: Backend should filter to return only leases for landlord's properties
        AppManager.getApiService().listLeases().enqueue(object : Callback<List<Lease>> {
            override fun onResponse(call: Call<List<Lease>>, response: Response<List<Lease>>) {
                if (response.isSuccessful) {
                    val leases = response.body()

                    if (leases != null) {
                        // Clear existing leases
                        allLeases.clear()
                        allLeases.addAll(leases)

                        // Count active leases (occupied units)
                        occupiedUnits = leases.count { it.status == "active" }
                    }
                }

                // Proceed to load payments (even if leases fail)
                // Step 4: Fetch recent payments to calculate revenue
                loadRecentPayments()
            }

            override fun onFailure(call: Call<List<Lease>>, t: Throwable) {
                // Continue even if leases fail
                loadRecentPayments()
            }
        })
    }

    /**
     * Loads recent payments to calculate monthly revenue
     * Fetches payment history and filters for current month
     */
    private fun loadRecentPayments() {
        // Fetch payment history (all payments for landlord's properties)
        AppManager.getApiService().getPaymentHistory().enqueue(object : Callback<List<Payment>> {
            override fun onResponse(call: Call<List<Payment>>, response: Response<List<Payment>>) {
                if (response.isSuccessful) {
                    val payments = response.body()

                    if (payments != null) {
                        // Clear existing payments
                        recentPayments.clear()
                        recentPayments.addAll(payments)

                        // Calculate monthly revenue from completed payments
                        calculateMonthlyRevenue()
                    }
                }

                // All data loaded, display the dashboard
                displayDashboard()
            }

            override fun onFailure(call: Call<List<Payment>>, t: Throwable) {
                // Continue even if payments fail
                // Will show 0 revenue
                displayDashboard()
            }
        })
    }

    /**
     * Calculates monthly revenue from completed payments
     * Sums up all completed payments from current month
     */
    private fun calculateMonthlyRevenue() {
        // Get current month and year
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Reset monthly revenue
        monthlyRevenue = 0.0

        // ISO date format used by backend
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        // Sum up completed payments from current month
        for (payment in recentPayments) {
            // Only count completed payments
            if (payment.status.lowercase() == "completed") {
                try {
                    // Parse payment date
                    val paymentDate = isoFormat.parse(payment.createdAt)

                    if (paymentDate != null) {
                        // Get payment month and year
                        val paymentCalendar = Calendar.getInstance()
                        paymentCalendar.time = paymentDate
                        val paymentMonth = paymentCalendar.get(Calendar.MONTH)
                        val paymentYear = paymentCalendar.get(Calendar.YEAR)

                        // If payment is from current month, add to revenue
                        if (paymentMonth == currentMonth && paymentYear == currentYear) {
                            monthlyRevenue += payment.amount
                        }
                    }
                } catch (e: Exception) {
                    // If date parsing fails, skip this payment
                    continue
                }
            }
        }
    }

    /**
     * Displays all dashboard statistics in the UI
     * Called after all data is fetched and calculated
     */
    private fun displayDashboard() {
        // Hide loading, show content
        showContent()

        // Display welcome message
        val firstName = userName?.split(" ")?.firstOrNull() ?: "Landlord"
        welcomeText.text = "Welcome, $firstName"

        // Display current month and year
        monthYearText.text = getCurrentMonth()

        // Display total properties
        totalPropertiesText.text = "$totalProperties"

        // Display units renting (occupied / total)
        unitsRentingText.text = "$occupiedUnits / $totalUnits Units"

        // Calculate and display occupancy rate
        val occupancyRate = if (totalUnits > 0) {
            (occupiedUnits.toDouble() / totalUnits.toDouble() * 100).toInt()
        } else {
            0
        }
        occupancyRateText.text = "$occupancyRate%"

        // Display monthly revenue with currency formatting
        totalRentText.text = currencyFormat.format(monthlyRevenue)
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
     * Shows empty state (no properties)
     * Special case of error state for when landlord has no properties
     */
    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvErrorMessage.text = "No properties found. Add your first property to get started!"
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
     * Gets current month and year in readable format
     *
     * @return String in format "January 2025"
     */
    private fun getCurrentMonth(): String {
        // Array of month names
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        // Get current calendar instance
        val calendar = Calendar.getInstance()

        // Get month name from array
        val month = months[calendar.get(Calendar.MONTH)]

        // Get year
        val year = calendar.get(Calendar.YEAR)

        // Return formatted string
        return "$month $year"
    }

    /**
     * Fragment lifecycle: onResume
     * Called when fragment becomes visible
     * Refresh data when returning to fragment
     */
    override fun onResume() {
        super.onResume()

        // Refresh dashboard data when fragment becomes visible
        // This ensures data is up-to-date after returning from other screens
        if (properties.isNotEmpty()) {
            // Only refresh if we've loaded data before
            // Don't reload on first creation
            loadDashboardData()
        }
    }
}
