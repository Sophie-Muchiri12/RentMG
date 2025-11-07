package com.example.rentmg.data.model

data class Payment(
    val id: String,
    val tenantId: String,
    val amount: Double,
    val paymentDate: Long,
    val monthYear: String, // e.g., "2024-11"
    val status: PaymentStatus
)

enum class PaymentStatus {
    PAID, PENDING, LATE, PARTIAL
}