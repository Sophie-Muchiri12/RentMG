package com.example.rentmg.data.model

import com.google.gson.annotations.SerializedName

/**
 * Lease data model representing a rental agreement between landlord and tenant
 * Maps to the Lease table in the backend database
 * Links a tenant to a specific unit with start and end dates
 *
 * @property id Unique identifier for the lease
 * @property unitId ID of the unit being leased
 * @property tenantId ID of the tenant renting the unit
 * @property startDate Lease start date (YYYY-MM-DD format)
 * @property endDate Lease end date (YYYY-MM-DD format), null for month-to-month
 * @property status Current lease status (active, expired, terminated)
 * @property createdAt Timestamp when lease was created
 * @property updatedAt Timestamp when lease was last updated
 */
data class Lease(
    // Unique ID for the lease from backend
    @SerializedName("id")
    val id: Int,

    // ID of the unit being leased
    @SerializedName("unit_id")
    val unitId: Int,

    // ID of the tenant renting the unit
    @SerializedName("tenant_id")
    val tenantId: Int,

    // Lease start date
    @SerializedName("start_date")
    val startDate: String,

    // Lease end date (null for month-to-month leases)
    @SerializedName("end_date")
    val endDate: String?,

    // Lease status: "active", "expired", or "terminated"
    @SerializedName("status")
    val status: String,

    // Property ID for the unit (helps avoid extra calls)
    @SerializedName("property_id")
    val propertyId: Int? = null,

    // Timestamp when lease was created (ISO 8601 format)
    @SerializedName("created_at")
    val createdAt: String?,

    // Timestamp when lease was last updated
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * Request model for creating a new lease
 * Used when landlord assigns a tenant to a unit
 *
 * @property unitId ID of the unit to lease
 * @property tenantId ID of the tenant
 * @property startDate Lease start date
 * @property endDate Lease end date (optional, null for month-to-month)
 * @property status Initial lease status (usually "active")
 */
data class LeaseCreateRequest(
    // Unit ID
    @SerializedName("unit_id")
    val unitId: Int,

    // Tenant ID
    @SerializedName("tenant_id")
    val tenantId: Int,

    // Start date (YYYY-MM-DD)
    @SerializedName("start_date")
    val startDate: String,

    // End date (YYYY-MM-DD), optional
    @SerializedName("end_date")
    val endDate: String?,

    // Status (usually "active")
    @SerializedName("status")
    val status: String = "active"
)

/**
 * Extended lease information including unit and property details
 * Used for displaying complete lease information in the UI
 *
 * @property lease The lease object
 * @property unit The unit being leased
 * @property property The property containing the unit
 */
data class LeaseWithDetails(
    // The lease object
    val lease: Lease,

    // The unit details
    val unit: Unit,

    // The property details
    val property: Property
)
