package com.example.rentmg.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import com.example.rentmg.data.model.Property
import com.example.rentmg.data.model.Tenant
import java.text.NumberFormat
import java.util.Locale

class TenantAdapter(
    private var tenants: List<Tenant>,
    private var properties: List<Property>,
    private val onEditClick: (Tenant) -> Unit,
    private val onDeleteClick: (Tenant) -> Unit
) : RecyclerView.Adapter<TenantAdapter.TenantViewHolder>() {

    // ViewHolder holds references to the views for each tenant item
    class TenantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTenantName: TextView = itemView.findViewById(R.id.tvTenantName)
        val tvTenantProperty: TextView = itemView.findViewById(R.id.tvTenantProperty)
        val tvTenantPhone: TextView = itemView.findViewById(R.id.tvTenantPhone)
        val tvTenantRent: TextView = itemView.findViewById(R.id.tvTenantRent)
        val tvTenantDueDay: TextView = itemView.findViewById(R.id.tvTenantDueDay)
        val btnEditTenant: ImageButton = itemView.findViewById(R.id.btnEditTenant)
        val btnDeleteTenant: ImageButton = itemView.findViewById(R.id.btnDeleteTenant)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tenant, parent, false)
        return TenantViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        val tenant = tenants[position]

        // Find the property this tenant belongs to
        val property = properties.find { it.id == tenant.propertyId }

        // Set the tenant data to the views
        holder.tvTenantName.text = tenant.name

        // Show property name and unit number
        val propertyText = if (property != null) {
            if (tenant.unitNumber != null) {
                "${property.name} - Unit ${tenant.unitNumber}"
            } else {
                property.name
            }
        } else {
            "Unknown Property"
        }
        holder.tvTenantProperty.text = propertyText

        holder.tvTenantPhone.text = tenant.phoneNumber

        // Format rent amount with currency
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        numberFormat.maximumFractionDigits = 0
        holder.tvTenantRent.text = numberFormat.format(tenant.rentAmount)

        // Show rent due day
        val dayOrdinal = getDayOrdinal(tenant.rentDueDay)
        holder.tvTenantDueDay.text = "Due: $dayOrdinal"

        // Set click listeners for edit and delete buttons
        holder.btnEditTenant.setOnClickListener {
            onEditClick(tenant)
        }

        holder.btnDeleteTenant.setOnClickListener {
            onDeleteClick(tenant)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int = tenants.size

    // Method to update the list of tenants
    fun updateTenants(newTenants: List<Tenant>, newProperties: List<Property>) {
        tenants = newTenants
        properties = newProperties
        notifyDataSetChanged()
    }

    // Helper function to get ordinal suffix for day (1st, 2nd, 3rd, etc.)
    private fun getDayOrdinal(day: Int): String {
        return when {
            day in 11..13 -> "${day}th"
            day % 10 == 1 -> "${day}st"
            day % 10 == 2 -> "${day}nd"
            day % 10 == 3 -> "${day}rd"
            else -> "${day}th"
        }
    }
}