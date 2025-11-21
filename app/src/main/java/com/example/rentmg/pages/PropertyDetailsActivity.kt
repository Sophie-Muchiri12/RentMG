package com.example.rentmg.pages

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import com.example.rentmg.data.model.Property
import com.example.rentmg.data.model.Unit
import com.example.rentmg.data.model.UnitCreateRequest
import com.example.rentmg.util.AppManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

/**
 * PropertyDetailsActivity
 * Displays detailed information about a property and its units
 *
 * Features:
 * - Shows property name and address
 * - Lists all units in the property
 * - Allows adding new units
 * - Shows unit details (code, rent, status)
 * - Provides edit/delete property options
 *
 * Data Flow:
 * 1. Receives property ID from intent
 * 2. Fetches property details from API
 * 3. Fetches units for this property
 * 4. Displays property info and units list
 * 5. User can add new units via FAB
 */
class PropertyDetailsActivity : AppCompatActivity() {

    // ============================================
    // UI COMPONENTS
    // ============================================
    private lateinit var tvPropertyName: TextView
    private lateinit var tvPropertyAddress: TextView
    private lateinit var tvUnitCount: TextView
    private lateinit var rvUnits: RecyclerView
    private lateinit var tvNoUnits: TextView
    private lateinit var fabAddUnit: FloatingActionButton
    private lateinit var btnEditProperty: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: Button

    // ============================================
    // DATA
    // ============================================
    private var propertyId: Int = -1
    private var property: Property? = null
    private val units = mutableListOf<Unit>()
    private lateinit var unitsAdapter: UnitsAdapter

    /**
     * Activity lifecycle: onCreate
     * Initializes the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)

        // Get property ID from intent
        propertyId = intent.getIntExtra("PROPERTY_ID", -1)
        if (propertyId == -1) {
            Toast.makeText(this, "Error: Invalid property", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI components
        initializeViews()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup listeners
        setupListeners()

        // Load property data
        loadPropertyDetails()
    }

    /**
     * Initializes all UI components
     */
    private fun initializeViews() {
        tvPropertyName = findViewById(R.id.tv_property_name)
        tvPropertyAddress = findViewById(R.id.tv_property_address)
        tvUnitCount = findViewById(R.id.tv_unit_count)
        rvUnits = findViewById(R.id.rv_units)
        tvNoUnits = findViewById(R.id.tv_no_units)
        fabAddUnit = findViewById(R.id.fab_add_unit)
        btnEditProperty = findViewById(R.id.btn_edit_property)
        backButton = findViewById(R.id.back_button)
        progressBar = findViewById(R.id.progress_bar)
        contentLayout = findViewById(R.id.content_layout)
        errorLayout = findViewById(R.id.error_layout)
        tvErrorMessage = findViewById(R.id.tv_error_message)
        btnRetry = findViewById(R.id.btn_retry)
    }

    /**
     * Sets up RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        unitsAdapter = UnitsAdapter(units) { unit ->
            onUnitClicked(unit)
        }

        rvUnits.apply {
            layoutManager = LinearLayoutManager(this@PropertyDetailsActivity)
            adapter = unitsAdapter
        }
    }

    /**
     * Sets up click listeners
     */
    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        fabAddUnit.setOnClickListener {
            showAddUnitDialog()
        }

        btnEditProperty.setOnClickListener {
            showEditPropertyDialog()
        }

        btnRetry.setOnClickListener {
            loadPropertyDetails()
        }
    }

    /**
     * Loads property details from API
     */
    private fun loadPropertyDetails() {
        showLoading()

        AppManager.getApiService().getProperty(propertyId).enqueue(object : Callback<Property> {
            override fun onResponse(call: Call<Property>, response: Response<Property>) {
                if (response.isSuccessful) {
                    property = response.body()
                    if (property != null) {
                        displayPropertyInfo()
                        loadUnits()
                    } else {
                        showError("Property not found")
                    }
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
     * Displays property information
     */
    private fun displayPropertyInfo() {
        property?.let {
            tvPropertyName.text = it.name
            tvPropertyAddress.text = it.address
        }
    }

    /**
     * Loads units for this property
     */
    private fun loadUnits() {
        AppManager.getApiService().listUnitsByProperty(propertyId).enqueue(object : Callback<List<Unit>> {
            override fun onResponse(call: Call<List<Unit>>, response: Response<List<Unit>>) {
                if (response.isSuccessful) {
                    val unitsList = response.body()
                    if (unitsList != null) {
                        units.clear()
                        units.addAll(unitsList)
                        unitsAdapter.notifyDataSetChanged()

                        tvUnitCount.text = units.size.toString()

                        if (units.isEmpty()) {
                            showEmptyState()
                        } else {
                            showContent()
                        }
                    } else {
                        showContent()
                        tvUnitCount.text = "0"
                    }
                } else {
                    showError("Failed to load units: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Unit>>, t: Throwable) {
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Shows dialog for adding a new unit
     */
    private fun showAddUnitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_unit, null)

        val etUnitCode = dialogView.findViewById<EditText>(R.id.et_unit_code)
        val etUnitRent = dialogView.findViewById<EditText>(R.id.et_unit_rent)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Unit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val code = etUnitCode.text.toString().trim()
                val rentStr = etUnitRent.text.toString().trim()

                if (code.isEmpty()) {
                    Toast.makeText(this, "Please enter unit code", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (rentStr.isEmpty()) {
                    Toast.makeText(this, "Please enter rent amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val rentAmount = rentStr.toDoubleOrNull()
                if (rentAmount == null || rentAmount <= 0) {
                    Toast.makeText(this, "Please enter a valid rent amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                createUnit(code, rentAmount)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    /**
     * Creates a new unit via API
     */
    private fun createUnit(code: String, rentAmount: Double) {
        Toast.makeText(this, "Creating unit...", Toast.LENGTH_SHORT).show()

        val request = UnitCreateRequest(
            code = code,
            rentAmount = rentAmount,
            propertyId = propertyId
        )

        AppManager.getApiService().createUnit(request).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    val unit = response.body()
                    if (unit != null) {
                        Toast.makeText(
                            this@PropertyDetailsActivity,
                            "Unit created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        units.add(unit)
                        unitsAdapter.notifyItemInserted(units.size - 1)
                        tvUnitCount.text = units.size.toString()

                        if (units.size == 1) {
                            showContent()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@PropertyDetailsActivity,
                        "Failed to create unit: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(
                    this@PropertyDetailsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Shows dialog for editing property
     */
    private fun showEditPropertyDialog() {
        property?.let { prop ->
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_property, null)

            val etPropertyName = dialogView.findViewById<EditText>(R.id.et_property_name)
            val etPropertyAddress = dialogView.findViewById<EditText>(R.id.et_property_address)

            etPropertyName.setText(prop.name)
            etPropertyAddress.setText(prop.address)

            AlertDialog.Builder(this)
                .setTitle("Edit Property")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    Toast.makeText(this, "Edit property feature coming soon", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Delete") { _, _ ->
                    showDeletePropertyDialog()
                }
                .create()
                .show()
        }
    }

    /**
     * Shows confirmation dialog for deleting property
     */
    private fun showDeletePropertyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Property")
            .setMessage("Are you sure you want to delete this property? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(this, "Delete property feature coming soon", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    /**
     * Handles unit item click
     */
    private fun onUnitClicked(unit: Unit) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        formatter.currency = java.util.Currency.getInstance("KES")

        Toast.makeText(
            this,
            "Unit: ${unit.code}\nRent: ${formatter.format(unit.rentAmount)}/month",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Shows loading state
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    /**
     * Shows content state
     */
    private fun showContent() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        tvNoUnits.visibility = View.GONE
        rvUnits.visibility = View.VISIBLE
    }

    /**
     * Shows empty state
     */
    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        tvNoUnits.visibility = View.VISIBLE
        rvUnits.visibility = View.GONE
    }

    /**
     * Shows error state
     */
    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvErrorMessage.text = message
    }
}

/**
 * UnitsAdapter
 * RecyclerView adapter for displaying unit list
 */
class UnitsAdapter(
    private val items: List<Unit>,
    private val onUnitClick: (Unit) -> kotlin.Unit
) : RecyclerView.Adapter<UnitsAdapter.UnitViewHolder>() {

    inner class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(unit: Unit) {
            val tvUnitCode = itemView.findViewById<TextView>(R.id.tv_unit_code)
            val tvUnitRent = itemView.findViewById<TextView>(R.id.tv_unit_rent)
            val tvUnitStatus = itemView.findViewById<TextView>(R.id.tv_unit_status)

            tvUnitCode.text = unit.code

            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
            formatter.currency = java.util.Currency.getInstance("KES")
            tvUnitRent.text = formatter.format(unit.rentAmount)

            // For now, show all units as "Available"
            // TODO: Check lease status to determine if occupied
            tvUnitStatus.text = "Available"
            tvUnitStatus.setBackgroundColor(0xFF4CAF50.toInt())

            itemView.setOnClickListener {
                onUnitClick(unit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unit, parent, false)
        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
