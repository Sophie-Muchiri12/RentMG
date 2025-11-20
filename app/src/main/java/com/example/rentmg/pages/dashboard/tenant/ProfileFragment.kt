package com.example.rentmg.pages.dashboard.tenant

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rentmg.MainActivity
import com.example.rentmg.R
import com.example.rentmg.util.AppManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * ProfileFragment
 * Displays user profile information and settings
 *
 * Features:
 * - Shows user information (name, email, phone, type)
 * - Displays account statistics (join date, payments, balance)
 * - Provides logout functionality
 * - Buttons for future features (edit profile, change password, settings)
 *
 * Used by both tenants and landlords in their respective dashboards
 */
class ProfileFragment : Fragment() {

    // ============================================
    // UI COMPONENTS - Profile Information
    // ============================================
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserType: TextView
    private lateinit var tvJoinDate: TextView

    // ============================================
    // UI COMPONENTS - Statistics
    // ============================================
    private lateinit var tvTotalPayments: TextView
    private lateinit var tvOutstandingBalance: TextView

    // ============================================
    // UI COMPONENTS - Action Buttons
    // ============================================
    private lateinit var btnEditProfile: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var btnSettings: Button

    // ============================================
    // DATA - User Information from Arguments
    // ============================================
    private var userName: String? = null
    private var userEmail: String? = null
    private var userType: String? = null

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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    /**
     * Fragment lifecycle: onViewCreated
     * Called after view is created
     * Initializes UI and loads user data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user data from arguments bundle
        getUserDataFromArguments()

        // Initialize all UI components
        initializeViews(view)

        // Load and display user profile information
        populateUI()

        // Setup button click listeners
        setupListeners()
    }

    /**
     * Retrieves user data from fragment arguments
     * This data was passed from the dashboard activity
     */
    private fun getUserDataFromArguments() {
        userName = arguments?.getString("USER_NAME")
        userEmail = arguments?.getString("USER_EMAIL")
        userType = arguments?.getString("USER_TYPE")
    }

    /**
     * Initializes all UI components by finding them by ID
     * Called once during onViewCreated
     */
    private fun initializeViews(view: View) {
        // Profile Information TextViews
        tvUserName = view.findViewById(R.id.tv_profile_name)
        tvUserEmail = view.findViewById(R.id.tv_profile_email)
        tvUserPhone = view.findViewById(R.id.tv_profile_phone)
        tvUserType = view.findViewById(R.id.tv_profile_user_type)
        tvJoinDate = view.findViewById(R.id.tv_profile_join_date)

        // Statistics TextViews
        tvTotalPayments = view.findViewById(R.id.tv_total_payments)
        tvOutstandingBalance = view.findViewById(R.id.tv_outstanding_balance)

        // Action Buttons
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        btnChangePassword = view.findViewById(R.id.btn_change_password)
        btnSettings = view.findViewById(R.id.btn_settings)
        btnLogout = view.findViewById(R.id.btn_logout)
    }

    /**
     * Populates UI with user profile information
     * Displays both data from arguments and from AppManager
     */
    private fun populateUI() {
        // Get current user from AppManager (if available)
        val currentUser = AppManager.getCurrentUser()

        // Display user name (from arguments or AppManager)
        tvUserName.text = userName ?: currentUser?.fullName ?: "User"

        // Display email (from arguments or AppManager)
        tvUserEmail.text = userEmail ?: currentUser?.email ?: "email@example.com"

        // Display phone number (placeholder for now, add to User model in future)
        tvUserPhone.text = "+254 712 XXX XXX"

        // Display user type with proper capitalization
        val displayUserType = when (userType ?: currentUser?.role) {
            "tenant" -> "Tenant"
            "landlord" -> "Landlord"
            "property_manager" -> "Property Manager"
            else -> "User"
        }
        tvUserType.text = displayUserType

        // Display join date (from User model if available)
        if (currentUser != null) {
            try {
                // Parse ISO 8601 date from backend
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val date = isoFormat.parse(currentUser.createdAt)
                tvJoinDate.text = "Joined: ${dateFormat.format(date!!)}"
            } catch (e: Exception) {
                // If parsing fails, show raw date
                tvJoinDate.text = "Joined: ${currentUser.createdAt}"
            }
        } else {
            // Default join date if no user data
            tvJoinDate.text = "Joined: Recently"
        }

        // Display statistics (placeholder values for now)
        // TODO: Fetch real statistics from API
        tvTotalPayments.text = "0"

        // Display outstanding balance with currency formatting
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        tvOutstandingBalance.text = currencyFormat.format(0.0)
    }

    /**
     * Sets up click listeners for all action buttons
     * Handles edit profile, change password, settings, and logout
     */
    private fun setupListeners() {
        // Edit Profile button - placeholder for future feature
        btnEditProfile.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Edit Profile - Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Navigate to edit profile screen
            // Future implementation:
            // - Allow user to update name, phone, email
            // - Call PUT /api/users/{id} endpoint
            // - Update AppManager with new user data
        }

        // Change Password button - placeholder for future feature
        btnChangePassword.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Change Password - Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Navigate to change password screen
            // Future implementation:
            // - Form with current password, new password, confirm password
            // - Call POST /api/auth/change-password endpoint
            // - Show success message
        }

        // Settings button - placeholder for future feature
        btnSettings.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Settings - Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Navigate to settings screen
            // Future implementation:
            // - Notification preferences
            // - Language selection
            // - Theme selection (dark/light mode)
            // - Privacy settings
        }

        // Logout button - fully functional
        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    /**
     * Performs user logout
     * Clears all user session data and navigates to login screen
     *
     * Process:
     * 1. Shows logout message
     * 2. Clears JWT token from storage
     * 3. Clears user data from AppManager
     * 4. Clears auth interceptor token
     * 5. Navigates to MainActivity (login screen)
     * 6. Clears back stack (prevents back button)
     */
    private fun performLogout() {
        // Show logout message to user
        Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()

        // Clear all user session data using AppManager
        // This clears:
        // - JWT token from SharedPreferences
        // - Current user from AppManager
        // - Token from AuthInterceptor
        AppManager.logout(requireContext())

        // Navigate back to MainActivity (login/signup screen)
        val intent = Intent(requireActivity(), MainActivity::class.java)

        // Clear all activities from back stack
        // This prevents user from pressing back button to return to dashboard
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start MainActivity
        startActivity(intent)

        // Finish current activity (dashboard)
        requireActivity().finish()
    }
}