package com.example.rentmg.data.model

import com.google.gson.annotations.SerializedName

/**
 * User data model representing a system user (landlord, property manager, or tenant)
 * Maps to the User table in the backend database
 *
 * @property id Unique identifier for the user
 * @property email User's email address (used for login)
 * @property role User's role in the system (landlord, property_manager, or tenant)
 * @property fullName User's full name
 * @property createdAt Timestamp when user account was created
 * @property updatedAt Timestamp when user account was last updated
 */
data class User(
    // Unique ID for the user from backend
    @SerializedName("id")
    val id: Int,

    // Email address used for authentication
    @SerializedName("email")
    val email: String,

    // User role: "landlord", "property_manager", or "tenant"
    @SerializedName("role")
    val role: String,

    // Full name of the user
    @SerializedName("full_name")
    val fullName: String?,

    // Timestamp when user was created (ISO 8601 format)
    @SerializedName("created_at")
    val createdAt: String?,

    // Timestamp when user was last updated
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * Request model for user registration
 * Used when creating a new user account
 *
 * @property email User's email address
 * @property password User's password (will be hashed by backend)
 * @property role User's role (landlord, property_manager, or tenant)
 * @property fullName User's full name
 */
data class RegisterRequest(
    // Email address for login
    @SerializedName("email")
    val email: String,

    // Password (will be securely hashed by backend)
    @SerializedName("password")
    val password: String,

    // User role type
    @SerializedName("role")
    val role: String,

    // User's full name
    @SerializedName("full_name")
    val fullName: String
)

/**
 * Response model for user registration
 * Returned by backend after successful registration
 *
 * @property message Success message from backend
 * @property user The newly created user object
 */
data class RegisterResponse(
    // Success message
    @SerializedName("message")
    val message: String?,

    // The newly created user
    @SerializedName("user")
    val user: User?
)
