package com.example.rentmg.pages.dashboard

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import com.example.rentmg.data.model.*
import com.example.rentmg.util.AppManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * TenantsFragment
 * Shows list of all tenants (via leases) for landlord
 *
 * Features:
 * - Displays all active leases with tenant info
 * - Shows unit and property details for each lease
 * - Allows creating new leases (assigning tenants to units)
 * - Shows lease status and dates
 */
class TenantsFragment : Fragment() {

    // UI Components
    private lateinit var rvTenants: RecyclerView
    private lateinit var tvNoTenants: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: Button
    private lateinit var fabAddLease: FloatingActionButton

    // Data
    private val leases = mutableListOf<Lease>()
    private lateinit var tenantsAdapter: TenantsAdapter
    private val properties = mutableListOf<Property>()
    private val units = mutableListOf<Unit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tenants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupListeners()
        loadData()
    }

    private fun initializeViews(view: View) {
        rvTenants = view.findViewById(R.id.rv_tenants)
        tvNoTenants = view.findViewById(R.id.tv_no_tenants)
        progressBar = view.findViewById(R.id.progress_bar)
        contentLayout = view.findViewById(R.id.content_layout)
        errorLayout = view.findViewById(R.id.error_layout)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)
        btnRetry = view.findViewById(R.id.btn_retry)
        fabAddLease = view.findViewById(R.id.fab_add_lease)
    }

    private fun setupRecyclerView() {
        tenantsAdapter = TenantsAdapter(leases) { lease ->
            showLeaseDetails(lease)
        }

        rvTenants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tenantsAdapter
        }
    }

    private fun setupListeners() {
        btnRetry.setOnClickListener {
            loadData()
        }

        fabAddLease.setOnClickListener {
            showCreateLeaseDialog()
        }
    }

    private fun loadData() {
        showLoading()
        loadLeases()
    }

    private fun loadLeases() {
        AppManager.getApiService().listLeases().enqueue(object : Callback<List<Lease>> {
            override fun onResponse(call: Call<List<Lease>>, response: Response<List<Lease>>) {
                if (response.isSuccessful) {
                    val leasesList = response.body()
                    if (leasesList != null) {
                        leases.clear()
                        leases.addAll(leasesList)
                        tenantsAdapter.notifyDataSetChanged()

                        if (leases.isEmpty()) {
                            showEmptyState()
                        } else {
                            showContent()
                        }
                    } else {
                        showEmptyState()
                    }
                } else {
                    showError("Failed to load tenants: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Lease>>, t: Throwable) {
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun showLeaseDetails(lease: Lease) {
        val message = """
            Lease ID: ${lease.id}
            Unit ID: ${lease.unitId}
            Tenant ID: ${lease.tenantId}
            Status: ${lease.status}
            Start Date: ${lease.startDate}
            End Date: ${lease.endDate ?: "Month-to-month"}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Lease Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showCreateLeaseDialog() {
        // First, load properties and units
        loadPropertiesForDialog()
    }

    private fun loadPropertiesForDialog() {
        Toast.makeText(requireContext(), "Loading properties...", Toast.LENGTH_SHORT).show()

        AppManager.getApiService().listProperties().enqueue(object : Callback<List<Property>> {
            override fun onResponse(call: Call<List<Property>>, response: Response<List<Property>>) {
                if (response.isSuccessful) {
                    val propertiesList = response.body()
                    if (propertiesList != null && propertiesList.isNotEmpty()) {
                        properties.clear()
                        properties.addAll(propertiesList)
                        showCreateLeaseDialogWithData()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please create a property first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load properties",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Property>>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateLeaseDialogWithData() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_lease, null)

        val spinnerProperty = dialogView.findViewById<Spinner>(R.id.spinner_property)
        val spinnerUnit = dialogView.findViewById<Spinner>(R.id.spinner_unit)
        val etTenantEmail = dialogView.findViewById<EditText>(R.id.et_tenant_email)
        val etStartDate = dialogView.findViewById<EditText>(R.id.et_start_date)
        val etEndDate = dialogView.findViewById<EditText>(R.id.et_end_date)

        // Setup property spinner
        val propertyNames = properties.map { it.name }
        val propertyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, propertyNames)
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProperty.adapter = propertyAdapter

        // Load units when property is selected
        spinnerProperty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedProperty = properties[position]
                loadUnitsForProperty(selectedProperty.id, spinnerUnit)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Date pickers
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        etStartDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    etStartDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etEndDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    etEndDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create New Lease")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val tenantEmail = etTenantEmail.text.toString().trim()
                val startDate = etStartDate.text.toString().trim()
                val endDate = etEndDate.text.toString().trim().takeIf { it.isNotEmpty() }

                if (units.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select a property with units", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (tenantEmail.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter tenant email", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (startDate.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select start date", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedUnit = units[spinnerUnit.selectedItemPosition]
                createLeaseWithTenantEmail(selectedUnit.id, tenantEmail, startDate, endDate)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun loadUnitsForProperty(propertyId: Int, spinnerUnit: Spinner) {
        AppManager.getApiService().listUnitsByProperty(propertyId).enqueue(object : Callback<List<Unit>> {
            override fun onResponse(call: Call<List<Unit>>, response: Response<List<Unit>>) {
                if (response.isSuccessful) {
                    val unitsList = response.body()
                    if (unitsList != null) {
                        units.clear()
                        units.addAll(unitsList)

                        val unitCodes = units.map { "${it.code} - KSh ${it.rentAmount}" }
                        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitCodes)
                        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerUnit.adapter = unitAdapter
                    }
                }
            }

            override fun onFailure(call: Call<List<Unit>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to load units", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createLeaseWithTenantEmail(unitId: Int, tenantEmail: String, startDate: String, endDate: String?) {
        // Note: The backend API expects tenant_id, not email
        // For this demo, we'll show a message that this feature requires backend enhancement
        Toast.makeText(
            requireContext(),
            "Note: Please use tenant ID instead.\nBackend needs enhancement to support email lookup.",
            Toast.LENGTH_LONG
        ).show()

        // For now, assume tenant ID from email (this is a limitation)
        // In production, you'd need an API endpoint to look up tenant by email
        Toast.makeText(requireContext(), "Feature requires backend enhancement", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        tvNoTenants.visibility = View.GONE
        rvTenants.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        tvNoTenants.visibility = View.VISIBLE
        rvTenants.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvErrorMessage.text = message
    }
}

/**
 * TenantsAdapter
 * RecyclerView adapter for displaying tenant/lease list
 */
class TenantsAdapter(
    private val items: List<Lease>,
    private val onLeaseClick: (Lease) -> kotlin.Unit
) : RecyclerView.Adapter<TenantsAdapter.TenantViewHolder>() {

    inner class TenantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(lease: Lease) {
            val tvTenantId = itemView.findViewById<TextView>(R.id.tv_tenant_id)
            val tvUnitId = itemView.findViewById<TextView>(R.id.tv_unit_id)
            val tvLeaseStatus = itemView.findViewById<TextView>(R.id.tv_lease_status)
            val tvLeaseDate = itemView.findViewById<TextView>(R.id.tv_lease_date)

            tvTenantId.text = "Tenant ID: ${lease.tenantId}"
            tvUnitId.text = "Unit ID: ${lease.unitId}"
            tvLeaseStatus.text = lease.status.uppercase()
            tvLeaseDate.text = "Start: ${lease.startDate}"

            // Set status color
            when (lease.status.lowercase()) {
                "active" -> tvLeaseStatus.setBackgroundColor(0xFF4CAF50.toInt())
                "expired" -> tvLeaseStatus.setBackgroundColor(0xFFFFC107.toInt())
                "terminated" -> tvLeaseStatus.setBackgroundColor(0xFFF44336.toInt())
                else -> tvLeaseStatus.setBackgroundColor(0xFF9E9E9E.toInt())
            }

            itemView.setOnClickListener {
                onLeaseClick(lease)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tenant, parent, false)
        return TenantViewHolder(view)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
