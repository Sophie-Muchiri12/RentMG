package com.example.rentmg.pages.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.rentmg.R

class PropertiesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_properties, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIX 1: Enable edge-to-edge for the Activity that HOLDS the fragment.
        // The best place for this is in the Activity's onCreate, not here.
        // If you need to do it from the fragment, you can use requireActivity().
        requireActivity().enableEdgeToEdge()

        // FIX 2: Handle window insets on the fragment's root view.
        // The 'view' parameter IS the root view (the CoordinatorLayout).
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- You can now add the rest of your UI logic here ---
        // For example, finding a button and setting a click listener:
        // val myButton = view.findViewById<Button>(R.id.some_button_in_your_layout)
        // myButton.setOnClickListener {
        //     // ... do something ...
        // }
    }
}
