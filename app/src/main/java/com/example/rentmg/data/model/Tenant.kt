package com.example.rentmg.data.model

data class Tenant(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String?,
    val propertyId: String,
    val unitNumber: String?,
    val rentAmount: Double,
    val rentDueDay: Int, // Day of month rent is due
    val moveInDate: Long
)