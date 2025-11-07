package com.example.rentmg.pages.dashboard.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.rentmg.R
import com.example.rentmg.auth.SignInActivity

class SettingsFragment : Fragment() {

    private var isDarkMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.btn_profile)?.setOnClickListener {
            Toast.makeText(context, "Profile - Coming soon", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<LinearLayout>(R.id.btn_change_password)?.setOnClickListener {
            Toast.makeText(context, "Change Password - Coming soon", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<LinearLayout>(R.id.btn_dark_mode)?.setOnClickListener {
            toggleTheme()
        }

        view.findViewById<LinearLayout>(R.id.btn_sign_out)?.setOnClickListener {
            showSignOutDialog()
        }

        view.findViewById<LinearLayout>(R.id.btn_delete_account)?.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun toggleTheme() {
        isDarkMode = !isDarkMode
        Toast.makeText(
            context,
            if (isDarkMode) "Dark mode activated" else "Light mode activated",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), SignInActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Deleting your account will permanently remove all data. Continue?")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(context, "Account deleted", Toast.LENGTH_LONG).show()
                startActivity(Intent(requireContext(), SignInActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}