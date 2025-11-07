package com.example.rentmg.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import com.example.rentmg.data.model.Property

class PropertyAdapter(
    private var properties: List<Property>,
    private val onEditClick: (Property) -> Unit,
    private val onDeleteClick: (Property) -> Unit
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    // ViewHolder holds references to the views for each property item
    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPropertyName: TextView = itemView.findViewById(R.id.tvPropertyName)
        val tvPropertyAddress: TextView = itemView.findViewById(R.id.tvPropertyAddress)
        val tvPropertyType: TextView = itemView.findViewById(R.id.tvPropertyType)
        val tvPropertyUnits: TextView = itemView.findViewById(R.id.tvPropertyUnits)
        val btnEditProperty: ImageButton = itemView.findViewById(R.id.btnEditProperty)
        val btnDeleteProperty: ImageButton = itemView.findViewById(R.id.btnDeleteProperty)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property, parent, false)
        return PropertyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]

        // Set the property data to the views
        holder.tvPropertyName.text = property.name
        holder.tvPropertyAddress.text = property.address
        holder.tvPropertyType.text = property.type
        holder.tvPropertyUnits.text = "${property.numberOfUnits} units"

        // Set click listeners for edit and delete buttons
        holder.btnEditProperty.setOnClickListener {
            onEditClick(property)
        }

        holder.btnDeleteProperty.setOnClickListener {
            onDeleteClick(property)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int = properties.size

    // Method to update the list of properties
    fun updateProperties(newProperties: List<Property>) {
        properties = newProperties
        notifyDataSetChanged()
    }
}