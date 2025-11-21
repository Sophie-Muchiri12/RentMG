package com.example.rentmg.data.model

import com.google.gson.annotations.SerializedName

/**
 * Payment data model representing a rent payment transaction
 * Maps to the Payment table in the backend database
 * Tracks M-Pesa payments made by tenants for their rent
 *
 * @property id Unique identifier for the payment
 * @property leaseId ID of the lease this payment is for
 * @property method Payment method used (e.g., "mpesa")
 * @property amount Payment amount in local currency
 * @property status Payment status (pending, completed, failed, refunded)
 * @property reference Payment reference number (e.g., M-Pesa transaction code)
 * @property mpesaCheckoutId M-Pesa checkout request ID for tracking
 * @property createdAt Timestamp when payment was initiated
 * @property updatedAt Timestamp when payment status was last updated
 */
data class Payment(
    // Unique ID for the payment from backend
    @SerializedName("id")
    val id: Int,

    // ID of the lease this payment is for
    @SerializedName("lease_id")
    val leaseId: Int,

    // Payment method: "mpesa", "bank_transfer", "cash", etc.
    @SerializedName("method")
    val method: String,

    // Payment amount
    @SerializedName("amount")
    val amount: Double,

    // Payment status: "pending", "completed", "failed", "refunded"
    @SerializedName("status")
    val status: String,

    // Payment reference number (e.g., M-Pesa code)
    @SerializedName("reference")
    val reference: String?,

    // M-Pesa checkout request ID (for STK push)
    @SerializedName("mpesa_checkout_id")
    val mpesaCheckoutId: String?,

    // Timestamp when payment was created (ISO 8601 format)
    @SerializedName("created_at")
    val createdAt: String,

    // Timestamp when payment was last updated
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * Request model for initiating an M-Pesa payment
 * Used when tenant wants to pay rent via M-Pesa STK push
 *
 * @property leaseId ID of the lease to pay for
 * @property amount Amount to pay
 * @property phone M-Pesa phone number (254XXXXXXXXX format)
 */
data class PaymentInitRequest(
    // Lease ID
    @SerializedName("lease_id")
    val leaseId: Int,

    // Payment amount
    @SerializedName("amount")
    val amount: Int,

    // M-Pesa phone number
    @SerializedName("phone")
    val phone: String
)

/**
 * Response model for payment initiation
 * Returned after successfully initiating an M-Pesa STK push
 *
 * @property paymentId ID of the created payment record
 * @property message Status message
 */
data class PaymentInitResponse(
    // Created payment ID
    @SerializedName("payment_id")
    val paymentId: Int?,

    // Status message from backend
    @SerializedName("message")
    val message: String?,

    // CheckoutRequestID from M-Pesa (useful for debugging/polling)
    @SerializedName("mpesa_checkout_id")
    val mpesaCheckoutId: String?
)

/**
 * Payment history entry with additional details
 * Used for displaying payment history in the UI
 */
data class PaymentHistoryItem(
    // The payment object
    val payment: Payment,

    // Month/year this payment is for (e.g., "January 2025")
    val periodDescription: String,

    // Formatted amount (e.g., "KES 25,000")
    val formattedAmount: String,

    // Human-readable status
    val statusDescription: String
)
