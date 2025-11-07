package com.example.rentmg.pages.payment

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.rentmg.R
import com.google.android.material.textfield.TextInputEditText

class CheckoutActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvCheckoutProperty: TextView
    private lateinit var tvCheckoutUnit: TextView
    private lateinit var tvCheckoutRent: TextView
    private lateinit var tvCheckoutFee: TextView
    private lateinit var tvCheckoutTotal: TextView
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var btnCompletePayment: Button
    private lateinit var btnCancel: TextView
    
    private lateinit var progressDialog: ProgressDialog
    
    private var propertyName: String = ""
    private var unitNumber: String = ""
    private var rentAmount: Double = 0.0
    private var transactionFee: Double = 0.0
    private var totalAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        
        // Initialize views
        initializeViews()
        
        // Setup toolbar
        setupToolbar()
        
        // Get data from intent
        getIntentData()
        
        // Display payment information
        displayPaymentInfo()
        
        // Setup click listeners
        setupClickListeners()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tvCheckoutProperty = findViewById(R.id.tv_checkout_property)
        tvCheckoutUnit = findViewById(R.id.tv_checkout_unit)
        tvCheckoutRent = findViewById(R.id.tv_checkout_rent)
        tvCheckoutFee = findViewById(R.id.tv_checkout_fee)
        tvCheckoutTotal = findViewById(R.id.tv_checkout_total)
        etPhoneNumber = findViewById(R.id.et_phone_number)
        btnCompletePayment = findViewById(R.id.btn_complete_payment)
        btnCancel = findViewById(R.id.btn_cancel)
        
        progressDialog = ProgressDialog(this).apply {
            setMessage("Processing payment...")
            setCancelable(false)
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun getIntentData() {
        propertyName = intent.getStringExtra("PROPERTY_NAME") ?: "Greenview Apartments"
        unitNumber = intent.getStringExtra("UNIT_NUMBER") ?: "A-102"
        rentAmount = intent.getDoubleExtra("RENT_AMOUNT", 25000.0)
        transactionFee = intent.getDoubleExtra("TRANSACTION_FEE", 0.0)
        totalAmount = rentAmount + transactionFee
    }
    
    private fun displayPaymentInfo() {
        tvCheckoutProperty.text = propertyName
        tvCheckoutUnit.text = unitNumber
        tvCheckoutRent.text = "KES ${formatAmount(rentAmount)}"
        tvCheckoutFee.text = "KES ${formatAmount(transactionFee)}"
        tvCheckoutTotal.text = "KES ${formatAmount(totalAmount)}"
    }
    
    private fun setupClickListeners() {
        btnCompletePayment.setOnClickListener {
            processPayment()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun processPayment() {
        val phoneNumber = etPhoneNumber.text.toString().trim()
        
        // Validate phone number
        if (!validatePhoneNumber(phoneNumber)) {
            return
        }
        
        // Show progress dialog
        progressDialog.show()
        
        // Format phone number for M-Pesa (254XXXXXXXXX)
        val formattedPhone = formatPhoneNumberForMpesa(phoneNumber)
        
        // Simulate M-Pesa STK Push
        // In production, this would call your backend API which then calls M-Pesa API
        simulateMpesaPayment(formattedPhone)
    }
    
    private fun validatePhoneNumber(phone: String): Boolean {
        when {
            phone.isEmpty() -> {
                etPhoneNumber.error = "Phone number is required"
                etPhoneNumber.requestFocus()
                return false
            }
            phone.length < 10 -> {
                etPhoneNumber.error = "Invalid phone number"
                etPhoneNumber.requestFocus()
                return false
            }
            !phone.startsWith("07") && !phone.startsWith("01") && !phone.startsWith("254") -> {
                etPhoneNumber.error = "Invalid phone number format"
                etPhoneNumber.requestFocus()
                return false
            }
            else -> return true
        }
    }
    
    private fun formatPhoneNumberForMpesa(phone: String): String {
        return when {
            phone.startsWith("07") || phone.startsWith("01") -> {
                "254${phone.substring(1)}"
            }
            phone.startsWith("254") -> phone
            phone.startsWith("+254") -> phone.substring(1)
            else -> phone
        }
    }
    
    private fun simulateMpesaPayment(phoneNumber: String) {
        // Simulate API call delay
        android.os.Handler(mainLooper).postDelayed({
            progressDialog.dismiss()
            
            // In production, you would check the actual API response
            // For now, we'll simulate a successful payment
            showPaymentSuccess()
            
        }, 3000) // 3 seconds delay to simulate processing
    }
    
    private fun showPaymentSuccess() {
        Toast.makeText(
            this,
            "Payment initiated! Please enter your M-Pesa PIN",
            Toast.LENGTH_LONG
        ).show()
        
        // Navigate to success screen or back to dashboard
        // You can create a PaymentSuccessActivity for better UX
        android.os.Handler(mainLooper).postDelayed({
            finish()
        }, 2000)
    }
    
    private fun formatAmount(amount: Double): String {
        return String.format("%,.0f", amount)
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}