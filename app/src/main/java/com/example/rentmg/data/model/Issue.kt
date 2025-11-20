package com.example.rentmg.data.model

import com.google.gson.annotations.SerializedName

/**
 * Issue data model representing a maintenance or service issue
 * Maps to the Issue table in the backend database
 * Tenants can report issues and landlords can track and resolve them
 *
 * @property id Unique identifier for the issue
 * @property title Short description of the issue
 * @property description Detailed description of the issue
 * @property status Current issue status (open, in_progress, resolved, closed)
 * @property priority Issue priority (low, medium, high, urgent)
 * @property reporterId ID of the user who reported the issue
 * @property assigneeId ID of the user assigned to fix the issue (optional)
 * @property propertyId ID of the property where issue exists (optional)
 * @property unitId ID of the specific unit with the issue (optional)
 * @property createdAt Timestamp when issue was reported
 * @property updatedAt Timestamp when issue was last updated
 */
data class Issue(
    // Unique ID for the issue from backend
    @SerializedName("id")
    val id: Int,

    // Short title/summary of the issue
    @SerializedName("title")
    val title: String,

    // Detailed description of the issue
    @SerializedName("description")
    val description: String?,

    // Issue status: "open", "in_progress", "resolved", "closed"
    @SerializedName("status")
    val status: String,

    // Priority level: "low", "medium", "high", "urgent"
    @SerializedName("priority")
    val priority: String,

    // ID of the user who reported this issue
    @SerializedName("reporter_id")
    val reporterId: Int,

    // ID of the user assigned to fix this (optional)
    @SerializedName("assignee_id")
    val assigneeId: Int?,

    // ID of the property (optional)
    @SerializedName("property_id")
    val propertyId: Int?,

    // ID of the specific unit (optional)
    @SerializedName("unit_id")
    val unitId: Int?,

    // Timestamp when issue was created (ISO 8601 format)
    @SerializedName("created_at")
    val createdAt: String,

    // Timestamp when issue was last updated
    @SerializedName("updated_at")
    val updatedAt: String
)

/**
 * Request model for creating a new issue
 * Used when tenant reports a maintenance issue
 *
 * @property title Short title of the issue
 * @property description Detailed description (optional)
 * @property propertyId Property ID where issue exists (optional)
 * @property unitId Unit ID where issue exists (optional)
 * @property priority Issue priority (optional, defaults to "medium" on backend)
 */
data class IssueCreateRequest(
    // Issue title
    @SerializedName("title")
    val title: String,

    // Issue description (optional)
    @SerializedName("description")
    val description: String? = null,

    // Property ID (optional)
    @SerializedName("property_id")
    val propertyId: Int? = null,

    // Unit ID (optional)
    @SerializedName("unit_id")
    val unitId: Int? = null,

    // Priority (optional)
    @SerializedName("priority")
    val priority: String? = null
)

/**
 * Request model for updating an issue
 * Used when landlord updates issue status or assigns someone to fix it
 *
 * @property status New status (optional)
 * @property assigneeId User ID to assign to (optional)
 * @property priority New priority (optional)
 */
data class IssueUpdateRequest(
    // New status (optional)
    @SerializedName("status")
    val status: String? = null,

    // User to assign to (optional)
    @SerializedName("assignee_id")
    val assigneeId: Int? = null,

    // New priority (optional)
    @SerializedName("priority")
    val priority: String? = null
)
