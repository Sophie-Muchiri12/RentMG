package com.example.rentmg.data.model

data class Property(
    val id: String,
    val name: String,
    val address: String,
    val type: String, // Apartment, House, Commercial, etc.
    val numberOfUnits: Int,
    val createdDate: Long = System.currentTimeMillis()
)