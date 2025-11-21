package com.example.rentmg.data.model

import com.google.gson.annotations.SerializedName

/**
 * Unit data model representing a rental unit within a property
 * Maps to the Unit table in the backend database
 * A property can have multiple units (e.g., apartments, rooms, or office spaces)
 *
 * @property id Unique identifier for the unit
 * @property code Unit identifier (e.g., "A101", "B204", "Room 5")
 * @property rentAmount Monthly rent amount in local currency
 * @property propertyId ID of the property this unit belongs to
 * @property createdAt Timestamp when unit was created
 * @property updatedAt Timestamp when unit was last updated
 */
data class Unit(
    // Unique ID for the unit from backend
    @SerializedName("id")
    val id: Int,

    // Unit code/number (e.g., "A101", "Suite 200")
    @SerializedName("code")
    val code: String,

    // Monthly rent amount for this unit
    @SerializedName("rent_amount")
    val rentAmount: Double,

    // ID of the property this unit belongs to
    @SerializedName("property_id")
    val propertyId: Int,

    // Timestamp when unit was created (ISO 8601 format)
    @SerializedName("created_at")
    val createdAt: String?,

    // Timestamp when unit was last updated
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * Request model for creating a new unit
 * Used when landlord adds a new unit to a property
 *
 * @property code Unit identifier/number
 * @property rentAmount Monthly rent amount
 * @property propertyId ID of the property this unit belongs to
 */
data class UnitCreateRequest(
    // Unit code/number
    @SerializedName("code")
    val code: String,

    // Monthly rent amount
    @SerializedName("rent_amount")
    val rentAmount: Double,

    // Property ID
    @SerializedName("property_id")
    val propertyId: Int
)
