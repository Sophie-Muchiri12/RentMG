package com.example.rentmg.pages.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rentmg.R
import com.example.rentmg.auth.SignInActivity

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val userType: String,
    val joinDate: String,
    val totalPayments: Int,
    val outstandingBalance: Double
)

class ProfileFragment : Fragment() {
    
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserType: TextView
    private lateinit var tvJoinDate: TextView
    private lateinit var tvTotalPayments: TextView
    private lateinit var tvOutstandingBalance: TextView
    
    private lateinit var btnEditProfile: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var btnSettings: Button
    
    private var userProfile: UserProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        loadUserProfile()
        populateUI()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        // Profile Information
        tvUserName = view.findViewById(R.id.tv_profile_name)
        tvUserEmail = view.findViewById(R.id.tv_profile_email)
        tvUserPhone = view.findViewById(R.id.tv_profile_phone)
        tvUserType = view.findViewById(R.id.tv_profile_user_type)
        tvJoinDate = view.findViewById(R.id.tv_profile_join_date)
        
        // Statistics
        tvTotalPayments = view.findViewById(R.id.tv_total_payments)
        tvOutstandingBalance = view.findViewById(R.id.tv_outstanding_balance)
        
        // Buttons
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        btnChangePassword = view.findViewById(R.id.btn_change_password)
        btnSettings = view.findViewById(R.id.btn_settings)
        btnLogout = view.findViewById(R.id.btn_logout)
    }

    private fun loadUserProfile() {
        // TODO: Fetch from API using user ID or shared preferences
        // For now, using sample data
        userProfile = UserProfile(
            name = "Sophie Mwangi",
            email = "sophie.mwangi@example.com",
            phone = "+254 712 345 678",
            userType = "Tenant",
            joinDate = "January 15, 2024",
            totalPayments = 4,
            outstandingBalance = 0.0
        )
    }

    private fun populateUI() {
        userProfile?.let { profile ->
            tvUserName.text = profile.name
            tvUserEmail.text = profile.email
            tvUserPhone.text = profile.phone
            tvUserType.text = profile.userType
            tvJoinDate.text = "Joined: ${profile.joinDate}"
            
            tvTotalPayments.text = profile.totalPayments.toString()
            
            val currencyFormat = java.text.NumberFormat.getCurrencyInstance(
                java.util.Locale("en", "KE")
            )
            tvOutstandingBalance.text = currencyFormat.format(profile.outstandingBalance)
        }
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to edit profile screen
        }
        
        btnChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Change Password - Coming Soon", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to change password screen
        }
        
        btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to settings screen
        }
        
        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        // TODO: Clear user data from SharedPreferences or local database
        Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()
        
        // Navigate back to SignInActivity
        val intent = Intent(requireActivity(), SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}