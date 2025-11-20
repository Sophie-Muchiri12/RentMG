package com.example.rentmg.pages.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import com.example.rentmg.data.model.Property
import com.example.rentmg.data.model.PropertyCreateRequest
import com.example.rentmg.data.model.PropertyResponse
import com.example.rentmg.util.AppManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.*

/**
 * PropertiesFragment
 * Landlord property management screen
 *
 * Features:
 * - Displays list of all landlord's properties
 * - Shows property name and address
 * - Provides "Add Property" button (FloatingActionButton)
 * - Allows adding new properties via dialog
 * - Fetches real property data from backend API
 * - Handles loading and error states
 *
 * Data Flow:
 * 1. Fragment loads
 * 2. Fetches properties from API
 * 3. Displays properties in RecyclerView
 * 4. User can add new property via FAB
 * 5. Dialog captures property details
 * 6. Creates property via API
 * 7. Refreshes list with new property
 */
class PropertiesFragment : Fragment() {

    // ============================================
    // UI COMPONENTS - RecyclerView
    // ============================================
    private lateinit var rvProperties: RecyclerView
    private lateinit var propertiesAdapter: PropertiesAdapter

    // ============================================
    // UI COMPONENTS - Empty State
    // ============================================
    private lateinit var tvNoProperties: TextView

    // ============================================
    // UI COMPONENTS - Loading State
    // ============================================
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: Button

    // ============================================
    // UI COMPONENTS - Add Button
    // ============================================
    private lateinit var fabAddProperty: FloatingActionButton

    // ============================================
    // DATA - Property List
    // ============================================
    private val properties = mutableListOf<Property>()

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
        return inflater.inflate(R.layout.fragment_properties, container, false)
    }

    /**
     * Fragment lifecycle: onViewCreated
     * Called after view is created
     * Initializes everything and loads data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize all UI components
        initializeViews(view)

        // Setup RecyclerView with adapter
        setupRecyclerView()

        // Setup button click listeners
        setupListeners()

        // Fetch properties from backend API
        loadProperties()
    }

    /**
     * Initializes all UI components by finding them by ID
     * Called once during onViewCreated
     */
    private fun initializeViews(view: View) {
        // RecyclerView for property list
        rvProperties = view.findViewById(R.id.rv_properties)

        // Empty state message
        tvNoProperties = view.findViewById(R.id.tv_no_properties)

        // Loading state
        progressBar = view.findViewById(R.id.progress_bar)
        contentLayout = view.findViewById(R.id.content_layout)
        errorLayout = view.findViewById(R.id.error_layout)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)
        btnRetry = view.findViewById(R.id.btn_retry)

        // FloatingActionButton for adding properties
        fabAddProperty = view.findViewById(R.id.fab_add_property)
    }

    /**
     * Sets up RecyclerView with adapter and layout manager
     * RecyclerView displays properties in a scrollable list
     */
    private fun setupRecyclerView() {
        // Create adapter with empty list (will be populated later)
        // Pass click listener to handle property item clicks
        propertiesAdapter = PropertiesAdapter(properties) { property ->
            // Handle property click - show property details
            onPropertyClicked(property)
        }

        // Configure RecyclerView
        rvProperties.apply {
            // Use LinearLayoutManager for vertical list
            layoutManager = LinearLayoutManager(requireContext())

            // Set adapter
            adapter = propertiesAdapter
        }
    }

    /**
     * Sets up click listeners for interactive elements
     */
    private fun setupListeners() {
        // Retry button - retries loading data after error
        btnRetry.setOnClickListener {
            loadProperties()
        }

        // FloatingActionButton - shows add property dialog
        fabAddProperty.setOnClickListener {
            showAddPropertyDialog()
        }
    }

    /**
     * Loads properties from backend API
     * Fetches all properties for logged-in landlord
     * Shows loading state while fetching
     */
    private fun loadProperties() {
        // Show loading state
        showLoading()

        // Fetch properties from API
        // Backend automatically filters to return only landlord's properties
        AppManager.getApiService().listProperties().enqueue(object : Callback<List<Property>> {
            override fun onResponse(call: Call<List<Property>>, response: Response<List<Property>>) {
                if (response.isSuccessful) {
                    // Get list of properties
                    val propertiesList = response.body()

                    if (propertiesList != null) {
                        // Clear existing data
                        properties.clear()

                        // Add new data
                        properties.addAll(propertiesList)

                        // Notify adapter that data changed
                        propertiesAdapter.notifyDataSetChanged()

                        // Show content or empty state
                        if (properties.isEmpty()) {
                            showEmptyState()
                        } else {
                            showContent()
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
     * Shows dialog for adding a new property
     * User can enter property name and address
     */
    private fun showAddPropertyDialog() {
        // Create a custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_property, null)

        // Find input fields in dialog
        val etPropertyName = dialogView.findViewById<EditText>(R.id.et_property_name)
        val etPropertyAddress = dialogView.findViewById<EditText>(R.id.et_property_address)

        // Create AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Property")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Get input values
                val name = etPropertyName.text.toString().trim()
                val address = etPropertyAddress.text.toString().trim()

                // Validate input
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter property name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (address.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter property address", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Create property via API
                createProperty(name, address)
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Show the dialog
        dialog.show()
    }

    /**
     * Creates a new property via backend API
     * Sends POST request to create property
     *
     * @param name Property name
     * @param address Property address
     */
    private fun createProperty(name: String, address: String) {
        // Show loading message
        Toast.makeText(requireContext(), "Creating property...", Toast.LENGTH_SHORT).show()

        // Create request object
        val request = PropertyCreateRequest(
            name = name,
            address = address
        )

        // Send POST request to create property
        AppManager.getApiService().createProperty(request).enqueue(object : Callback<PropertyResponse> {
            override fun onResponse(call: Call<PropertyResponse>, response: Response<PropertyResponse>) {
                if (response.isSuccessful) {
                    // Property created successfully
                    val propertyResponse = response.body()

                    if (propertyResponse != null && propertyResponse.property != null) {
                        // Show success message
                        Toast.makeText(
                            requireContext(),
                            "Property created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Add new property to list
                        properties.add(propertyResponse.property)

                        // Notify adapter
                        propertiesAdapter.notifyItemInserted(properties.size - 1)

                        // If list was empty, update state
                        if (properties.size == 1) {
                            showContent()
                        }

                        // Alternatively, refresh the entire list
                        // loadProperties()
                    }
                } else {
                    // API returned error
                    Toast.makeText(
                        requireContext(),
                        "Failed to create property: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PropertyResponse>, t: Throwable) {
                // Network error
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Handles property item click
     * Shows property details and units
     *
     * @param property The clicked property
     */
    private fun onPropertyClicked(property: Property) {
        // TODO: Navigate to property details screen
        // This should show:
        // - Property information (name, address)
        // - List of units in this property
        // - Option to add new unit
        // - Option to edit/delete property

        // For now, show a toast
        Toast.makeText(
            requireContext(),
            "Property: ${property.name}\nAddress: ${property.address}",
            Toast.LENGTH_LONG
        ).show()

        // Future implementation:
        // val action = PropertiesFragmentDirections.actionPropertiesToPropertyDetails(property.id)
        // findNavController().navigate(action)
    }

    /**
     * Shows loading state
     * Hides content and error, shows progress bar
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
        tvNoProperties.visibility = View.GONE
        fabAddProperty.visibility = View.GONE
    }

    /**
     * Shows content state (property list)
     * Hides loading, error, and empty state
     */
    private fun showContent() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        tvNoProperties.visibility = View.GONE
        fabAddProperty.visibility = View.VISIBLE
    }

    /**
     * Shows empty state (no properties)
     * Hides loading, error, and content
     */
    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
        tvNoProperties.visibility = View.VISIBLE
        fabAddProperty.visibility = View.VISIBLE
    }

    /**
     * Shows error state
     * Hides loading, content, and empty state
     *
     * @param message Error message to display
     */
    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvErrorMessage.text = message
        tvNoProperties.visibility = View.GONE
        fabAddProperty.visibility = View.VISIBLE
    }

    /**
     * Fragment lifecycle: onResume
     * Called when fragment becomes visible
     * Refresh data when returning from other screens
     */
    override fun onResume() {
        super.onResume()

        // Refresh properties when returning to fragment
        // This ensures data is up-to-date after adding/editing properties
        if (properties.isNotEmpty()) {
            // Only refresh if we've loaded data before
            // Don't reload on first creation
            loadProperties()
        }
    }
}

/**
 * PropertiesAdapter
 * RecyclerView adapter for displaying property list
 *
 * Responsibilities:
 * - Creates ViewHolder for each property item
 * - Binds property data to views
 * - Handles property item clicks
 */
class PropertiesAdapter(
    // List of properties to display
    private val items: List<Property>,

    // Click listener for property items
    private val onPropertyClick: (Property) -> Unit
) : RecyclerView.Adapter<PropertiesAdapter.PropertyViewHolder>() {

    /**
     * PropertyViewHolder
     * Holds references to views in each property item
     * Binds property data to those views
     */
    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Binds a Property object to the view
         * Displays property information
         *
         * @param property Property object to display
         */
        fun bind(property: Property) {
            // Find all views in the item layout
            val tvPropertyName = itemView.findViewById<TextView>(R.id.tv_property_name)
            val tvPropertyAddress = itemView.findViewById<TextView>(R.id.tv_property_address)
            val tvUnitCount = itemView.findViewById<TextView>(R.id.tv_unit_count)

            // Display property name
            tvPropertyName.text = property.name

            // Display property address
            tvPropertyAddress.text = property.address

            // TODO: Display unit count
            // This requires fetching units for this property
            // For now, show placeholder
            tvUnitCount.text = "View Units"

            // Set click listener for entire item
            itemView.setOnClickListener {
                // Call the click listener passed to adapter
                onPropertyClick(property)
            }
        }
    }

    /**
     * Creates a new ViewHolder
     * Called by RecyclerView when it needs a new item view
     *
     * @param parent Parent ViewGroup
     * @param viewType View type (unused, we only have one type)
     * @return New PropertyViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property, parent, false)

        // Create and return ViewHolder
        return PropertyViewHolder(view)
    }

    /**
     * Binds data to a ViewHolder
     * Called by RecyclerView when an item needs to be displayed
     *
     * @param holder ViewHolder to bind data to
     * @param position Position of item in the list
     */
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        // Get property at this position
        val property = items[position]

        // Bind property data to holder
        holder.bind(property)
    }

    /**
     * Returns the total number of items in the list
     * Called by RecyclerView to determine how many items to display
     *
     * @return Number of properties in the list
     */
    override fun getItemCount() = items.size
}
