package com.example.rentmg.pages.payment

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.rentmg.R
import com.example.rentmg.data.model.Payment
import com.example.rentmg.data.model.PaymentInitRequest
import com.example.rentmg.data.model.PaymentInitResponse
import com.example.rentmg.util.AppManager
import com.google.android.material.textfield.TextInputEditText
import retrofit2.*

/**
 * CheckoutActivity
 * Handles rent payment via M-Pesa STK Push
 *
 * Features:
 * - Displays payment breakdown (rent + fees)
 * - Phone number validation for Kenyan format
 * - M-Pesa STK push integration
 * - Payment status polling
 * - Real-time status updates
 *
 * Flow:
 * 1. User enters M-Pesa phone number
 * 2. App calls backend API to initiate M-Pesa STK push
 * 3. Backend calls M-Pesa Daraja API
 * 4. User receives STK push notification on their phone
 * 5. User enters M-Pesa PIN
 * 6. App polls backend for payment status
 * 7. Shows success/failure message
 */
class CheckoutActivity : AppCompatActivity() {

    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var tvCheckoutProperty: TextView
    private lateinit var tvCheckoutUnit: TextView
    private lateinit var tvCheckoutRent: TextView
    private lateinit var tvCheckoutFee: TextView
    private lateinit var tvCheckoutTotal: TextView
    private lateinit var tvStatus: TextView
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var btnCompletePayment: Button
    private lateinit var btnCancel: TextView

    // Progress dialog for payment processing
    private lateinit var progressDialog: ProgressDialog

    // Payment details from intent
    private var propertyName: String = ""
    private var unitNumber: String = ""
    private var rentAmount: Double = 0.0
    private var transactionFee: Double = 0.0
    private var totalAmount: Double = 0.0
    private var leaseId: Int = 0

    // Handler for payment status polling
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Activity lifecycle: onCreate
     * Initializes UI and loads payment information
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Initialize all views
        initializeViews()

        // Setup toolbar with back button
        setupToolbar()

        // Get payment details from intent
        getIntentData()

        // Display payment information
        displayPaymentInfo()

        // Setup button click listeners
        setupClickListeners()
    }

    /**
     * Initializes all UI components by finding them by ID
     * Called once during onCreate
     */
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tvCheckoutProperty = findViewById(R.id.tv_checkout_property)
        tvCheckoutUnit = findViewById(R.id.tv_checkout_unit)
        tvCheckoutRent = findViewById(R.id.tv_checkout_rent)
        tvCheckoutFee = findViewById(R.id.tv_checkout_fee)
        tvCheckoutTotal = findViewById(R.id.tv_checkout_total)
        tvStatus = findViewById(R.id.tv_status)
        etPhoneNumber = findViewById(R.id.et_phone_number)
        btnCompletePayment = findViewById(R.id.btn_complete_payment)
        btnCancel = findViewById(R.id.btn_cancel)

        // Create progress dialog for showing payment processing state
        progressDialog = ProgressDialog(this).apply {
            setMessage("Processing payment...")
            setCancelable(false)
        }
    }

    /**
     * Sets up the toolbar with back navigation
     * Allows user to return to previous screen
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Handle toolbar back button click
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Retrieves payment details from intent extras
     * These details are passed from the dashboard when user clicks "Pay Rent"
     */
    private fun getIntentData() {
        // Get lease ID (required for payment API)
        leaseId = intent.getIntExtra("LEASE_ID", 0)

        // Get property and unit information
        propertyName = intent.getStringExtra("PROPERTY_NAME") ?: "Property"
        unitNumber = intent.getStringExtra("UNIT_NUMBER") ?: "Unit"

        // Get payment amounts
        rentAmount = intent.getDoubleExtra("RENT_AMOUNT", 25000.0)
        transactionFee = intent.getDoubleExtra("TRANSACTION_FEE", 0.0)

        // Calculate total amount
        totalAmount = rentAmount + transactionFee
    }

    /**
     * Displays payment breakdown in the UI
     * Shows property, unit, rent amount, fees, and total
     */
    private fun displayPaymentInfo() {
        tvCheckoutProperty.text = propertyName
        tvCheckoutUnit.text = unitNumber
        tvCheckoutRent.text = "KES ${formatAmount(rentAmount)}"
        tvCheckoutFee.text = "KES ${formatAmount(transactionFee)}"
        tvCheckoutTotal.text = "KES ${formatAmount(totalAmount)}"
    }

    /**
     * Sets up click listeners for interactive buttons
     * Complete Payment: initiates M-Pesa payment
     * Cancel: returns to previous screen
     */
    private fun setupClickListeners() {
        // Complete Payment button - starts payment process
        btnCompletePayment.setOnClickListener {
            processPayment()
        }

        // Cancel button - closes activity
        btnCancel.setOnClickListener {
            finish()
        }
    }

    /**
     * Processes payment by validating input and calling M-Pesa API
     * Main payment flow entry point
     */
    private fun processPayment() {
        // Get phone number from input field
        val phoneNumber = etPhoneNumber.text.toString().trim()

        // Validate phone number format
        if (!validatePhoneNumber(phoneNumber)) {
            return
        }

        // Check if lease ID was provided
        if (leaseId == 0) {
            Toast.makeText(
                this,
                "Error: Lease information not found. Please try again.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Show progress dialog
        progressDialog.show()

        // Format phone number to M-Pesa format (254XXXXXXXXX)
        val formattedPhone = formatPhoneNumberForMpesa(phoneNumber)

        // Call backend API to initiate M-Pesa STK push
        initiateMpesaPayment(leaseId, totalAmount.toInt(), formattedPhone)
    }

    /**
     * Validates Kenyan phone number format
     * Accepts formats: 07XXXXXXXX, 01XXXXXXXX, 254XXXXXXXXX
     *
     * @param phone Phone number string to validate
     * @return true if valid, false otherwise
     */
    private fun validatePhoneNumber(phone: String): Boolean {
        when {
            // Check if empty
            phone.isEmpty() -> {
                etPhoneNumber.error = "Phone number is required"
                etPhoneNumber.requestFocus()
                return false
            }
            // Check minimum length
            phone.length < 10 -> {
                etPhoneNumber.error = "Invalid phone number"
                etPhoneNumber.requestFocus()
                return false
            }
            // Check if starts with valid prefix
            !phone.startsWith("07") && !phone.startsWith("01") && !phone.startsWith("254") -> {
                etPhoneNumber.error = "Phone number must start with 07, 01, or 254"
                etPhoneNumber.requestFocus()
                return false
            }
            else -> return true
        }
    }

    /**
     * Converts phone number to M-Pesa format (254XXXXXXXXX)
     * M-Pesa API requires international format with country code
     *
     * @param phone Phone number in any Kenyan format
     * @return Phone number in 254XXXXXXXXX format
     */
    private fun formatPhoneNumberForMpesa(phone: String): String {
        return when {
            // 07XXXXXXXX or 01XXXXXXXX -> 254XXXXXXXXX
            phone.startsWith("07") || phone.startsWith("01") -> {
                "254${phone.substring(1)}"
            }
            // Already in 254XXXXXXXXX format
            phone.startsWith("254") -> phone
            // +254XXXXXXXXX -> 254XXXXXXXXX
            phone.startsWith("+254") -> phone.substring(1)
            // Default: return as is
            else -> phone
        }
    }

    /**
     * Initiates M-Pesa payment by calling backend API
     * Backend will then call M-Pesa Daraja API to send STK push
     *
     * @param leaseId ID of the lease being paid for
     * @param amount Payment amount in KES
     * @param phone M-Pesa phone number in 254XXXXXXXXX format
     */
    private fun initiateMpesaPayment(leaseId: Int, amount: Int, phone: String) {
        // Create payment initiation request
        val paymentRequest = PaymentInitRequest(
            leaseId = leaseId,
            amount = amount,
            phone = phone
        )

        // Call backend API using AppManager's API service
        AppManager.getApiService().initiatePayment(paymentRequest)
            .enqueue(object : Callback<PaymentInitResponse> {

                /**
                 * Called when backend responds successfully
                 * Starts polling for payment status
                 */
                override fun onResponse(
                    call: Call<PaymentInitResponse>,
                    response: Response<PaymentInitResponse>
                ) {
                    // Hide progress dialog
                    progressDialog.dismiss()

                    // Check if response was successful
                    if (response.isSuccessful) {
                        // Extract payment ID from response
                        val paymentId = response.body()?.paymentId

                        if (paymentId != null) {
                            // Show success message
                            Toast.makeText(
                                this@CheckoutActivity,
                                "STK push sent! Check your phone to complete payment",
                                Toast.LENGTH_LONG
                            ).show()

                            // Update status text
                            tvStatus.text = "Status: Waiting for M-Pesa confirmation..."

                            // Start polling for payment status
                            pollPaymentStatus(paymentId)
                        } else {
                            // Payment ID missing from response
                            Toast.makeText(
                                this@CheckoutActivity,
                                "Payment initiated but ID not received",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Server returned error
                        val errorMessage = when (response.code()) {
                            400 -> "Invalid payment details"
                            401 -> "Unauthorized. Please log in again"
                            500 -> "Server error. Please try again"
                            else -> "Payment failed: ${response.code()}"
                        }
                        Toast.makeText(this@CheckoutActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                /**
                 * Called when network request fails
                 * Shows error message to user
                 */
                override fun onFailure(call: Call<PaymentInitResponse>, t: Throwable) {
                    // Hide progress dialog
                    progressDialog.dismiss()

                    // Show network error message
                    Toast.makeText(
                        this@CheckoutActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    /**
     * Polls backend API for payment status updates
     * Checks every 3 seconds for up to 10 attempts (30 seconds total)
     * Stops polling once payment is completed or failed
     *
     * @param paymentId ID of the payment to poll
     */
    private fun pollPaymentStatus(paymentId: Int) {
        // Counter for polling attempts
        var attempts = 0

        // Maximum number of polling attempts (10 attempts = 30 seconds)
        val maxAttempts = 10

        // Create runnable for polling
        val pollingRunnable = object : Runnable {
            override fun run() {
                // Make API call to get payment status
                AppManager.getApiService().getPayment(paymentId)
                    .enqueue(object : Callback<Payment> {

                        /**
                         * Called when payment status is retrieved
                         * Updates UI and determines if polling should continue
                         */
                        override fun onResponse(call: Call<Payment>, response: Response<Payment>) {
                            if (response.isSuccessful) {
                                // Extract payment object
                                val payment = response.body()

                                if (payment != null) {
                                    // Update status text in UI
                                    val statusText = "Status: ${payment.status}"
                                    tvStatus.text = statusText

                                    // Check payment status
                                    when (payment.status.lowercase()) {
                                        "completed" -> {
                                            // Payment successful!
                                            showPaymentSuccess(payment)
                                            // Stop polling (don't schedule next run)
                                            return
                                        }
                                        "failed" -> {
                                            // Payment failed
                                            showPaymentFailed()
                                            // Stop polling
                                            return
                                        }
                                        "pending" -> {
                                            // Still waiting for user to complete payment
                                            // Continue polling if under max attempts
                                        }
                                    }
                                }
                            }

                            // Increment attempt counter
                            attempts++

                            // Continue polling if under max attempts
                            if (attempts < maxAttempts) {
                                // Schedule next poll in 3 seconds
                                handler.postDelayed(this@Runnable, 3000)
                            } else {
                                // Max attempts reached, stop polling
                                tvStatus.text = "Status: Timeout. Check payment history to verify."
                                Toast.makeText(
                                    this@CheckoutActivity,
                                    "Payment status check timed out. Please check payment history.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        /**
                         * Called when polling request fails
                         * Continues polling to handle temporary network issues
                         */
                        override fun onFailure(call: Call<Payment>, t: Throwable) {
                            // Increment attempt counter
                            attempts++

                            // Continue polling if under max attempts
                            if (attempts < maxAttempts) {
                                handler.postDelayed(this@Runnable, 3000)
                            }
                        }
                    })
            }
        }

        // Start first poll immediately
        handler.post(pollingRunnable)
    }

    /**
     * Shows payment success message and navigates back to dashboard
     *
     * @param payment Completed payment object
     */
    private fun showPaymentSuccess(payment: Payment) {
        // Show success toast
        Toast.makeText(
            this,
            "Payment successful! Reference: ${payment.reference ?: "N/A"}",
            Toast.LENGTH_LONG
        ).show()

        // Update status text
        tvStatus.text = "Status: Payment completed successfully!"

        // Navigate back to dashboard after 2 seconds
        handler.postDelayed({
            finish()
        }, 2000)
    }

    /**
     * Shows payment failure message
     */
    private fun showPaymentFailed() {
        // Show failure toast
        Toast.makeText(
            this,
            "Payment failed or was cancelled. Please try again.",
            Toast.LENGTH_LONG
        ).show()

        // Update status text
        tvStatus.text = "Status: Payment failed"
    }

    /**
     * Formats amount with thousands separator
     *
     * @param amount Double value to format
     * @return Formatted string (e.g., "25,000")
     */
    private fun formatAmount(amount: Double): String {
        return String.format("%,.0f", amount)
    }

    /**
     * Activity lifecycle: onBackPressed
     * Handles back button press
     * Cancels any ongoing polling
     */
    override fun onBackPressed() {
        // Remove all pending polling callbacks
        handler.removeCallbacksAndMessages(null)

        // Call super to finish activity
        super.onBackPressed()
    }

    /**
     * Activity lifecycle: onDestroy
     * Cleanup when activity is destroyed
     * Cancels polling and dismisses dialogs
     */
    override fun onDestroy() {
        super.onDestroy()

        // Remove all pending polling callbacks
        handler.removeCallbacksAndMessages(null)

        // Dismiss progress dialog if showing
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}
