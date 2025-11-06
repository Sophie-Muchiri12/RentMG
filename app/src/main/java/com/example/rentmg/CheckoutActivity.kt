package com.example.rentmg

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.rentmg.databinding.FragmentCheckoutBinding
import java.text.NumberFormat
import java.util.*

/**
 * Checkout Fragment for processing rental payments
 */
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private var selectedPaymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD
    private var rentalAmount: Double = 0.0
    private var serviceFee: Double = 0.0
    private var totalAmount: Double = 0.0

    enum class PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        PAYPAL,
        MOBILE_MONEY
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get rental details from arguments (if passed)
        // In a real app, you would pass these via Safe Args
        rentalAmount = arguments?.getDouble("rental_amount") ?: 150.00

        // Calculate fees and total
        calculateTotal()

        // Display amounts
        updateAmountDisplay()

        // Set up payment method selection
        setupPaymentMethods()

        // Set up payment button
        setupPaymentButton()

        // Set up form validation
        setupFormValidation()
    }

    private fun calculateTotal() {
        serviceFee = rentalAmount * 0.05 // 5% service fee
        totalAmount = rentalAmount + serviceFee
    }

    private fun updateAmountDisplay() {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

        binding.tvRentalAmount.text = currencyFormat.format(rentalAmount)
        binding.tvServiceFee.text = currencyFormat.format(serviceFee)
        binding.tvTotalAmount.text = currencyFormat.format(totalAmount)
    }

    private fun setupPaymentMethods() {
        // Credit Card
        binding.layoutCreditCard.setOnClickListener {
            selectPaymentMethod(PaymentMethod.CREDIT_CARD)
        }

        // Debit Card
        binding.layoutDebitCard.setOnClickListener {
            selectPaymentMethod(PaymentMethod.DEBIT_CARD)
        }

        // PayPal
        binding.layoutPaypal.setOnClickListener {
            selectPaymentMethod(PaymentMethod.PAYPAL)
        }

        // Mobile Money
        binding.layoutMobileMoney.setOnClickListener {
            selectPaymentMethod(PaymentMethod.MOBILE_MONEY)
        }

        // Default selection
        selectPaymentMethod(PaymentMethod.CREDIT_CARD)
    }

    private fun selectPaymentMethod(method: PaymentMethod) {
        selectedPaymentMethod = method

        // Reset all selections
        binding.layoutCreditCard.setBackgroundResource(R.drawable.payment_method_unselected)
        binding.layoutDebitCard.setBackgroundResource(R.drawable.payment_method_unselected)
        binding.layoutPaypal.setBackgroundResource(R.drawable.payment_method_unselected)
        binding.layoutMobileMoney.setBackgroundResource(R.drawable.payment_method_unselected)

        // Highlight selected method
        when (method) {
            PaymentMethod.CREDIT_CARD -> {
                binding.layoutCreditCard.setBackgroundResource(R.drawable.payment_method_selected)
                showCardForm(true)
            }
            PaymentMethod.DEBIT_CARD -> {
                binding.layoutDebitCard.setBackgroundResource(R.drawable.payment_method_selected)
                showCardForm(true)
            }
            PaymentMethod.PAYPAL -> {
                binding.layoutPaypal.setBackgroundResource(R.drawable.payment_method_selected)
                showCardForm(false)
            }
            PaymentMethod.MOBILE_MONEY -> {
                binding.layoutMobileMoney.setBackgroundResource(R.drawable.payment_method_selected)
                showCardForm(false)
            }
        }
    }

    private fun showCardForm(show: Boolean) {
        binding.layoutCardDetails.visibility = if (show) View.VISIBLE else View.GONE

        if (!show) {
            // Show alternative payment instructions
            when (selectedPaymentMethod) {
                PaymentMethod.PAYPAL -> {
                    binding.tvPaymentInstructions.visibility = View.VISIBLE
                    binding.tvPaymentInstructions.text =
                        "You will be redirected to PayPal to complete your payment"
                }
                PaymentMethod.MOBILE_MONEY -> {
                    binding.tvPaymentInstructions.visibility = View.VISIBLE
                    binding.tvPaymentInstructions.text =
                        "Enter your mobile money number to receive a payment prompt"
                }
                else -> {
                    binding.tvPaymentInstructions.visibility = View.GONE
                }
            }
        } else {
            binding.tvPaymentInstructions.visibility = View.GONE
        }
    }

    private fun setupFormValidation() {
        // Card number validation (simple)
        binding.etCardNumber.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateCardNumber()
            }
        }

        // CVV validation
        binding.etCvv.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateCVV()
            }
        }

        // Expiry date validation
        binding.etExpiryDate.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateExpiryDate()
            }
        }
    }

    private fun validateCardNumber(): Boolean {
        val cardNumber = binding.etCardNumber.text.toString().replace(" ", "")
        return if (cardNumber.length < 13 || cardNumber.length > 19) {
            binding.etCardNumber.error = "Invalid card number"
            false
        } else {
            binding.etCardNumber.error = null
            true
        }
    }

    private fun validateCVV(): Boolean {
        val cvv = binding.etCvv.text.toString()
        return if (cvv.length < 3 || cvv.length > 4) {
            binding.etCvv.error = "Invalid CVV"
            false
        } else {
            binding.etCvv.error = null
            true
        }
    }

    private fun validateExpiryDate(): Boolean {
        val expiry = binding.etExpiryDate.text.toString()
        val regex = Regex("^(0[1-9]|1[0-2])/([0-9]{2})$")
        return if (!regex.matches(expiry)) {
            binding.etExpiryDate.error = "Use MM/YY format"
            false
        } else {
            binding.etExpiryDate.error = null
            true
        }
    }

    private fun setupPaymentButton() {
        binding.btnPayNow.setOnClickListener {
            processPayment()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun processPayment() {
        // Validate based on payment method
        if (selectedPaymentMethod == PaymentMethod.CREDIT_CARD ||
            selectedPaymentMethod == PaymentMethod.DEBIT_CARD) {

            // Validate card details
            val cardNumberValid = validateCardNumber()
            val cvvValid = validateCVV()
            val expiryValid = validateExpiryDate()
            val cardholderName = binding.etCardholderName.text.toString()

            if (cardholderName.isEmpty()) {
                binding.etCardholderName.error = "Cardholder name is required"
                return
            }

            if (!cardNumberValid || !cvvValid || !expiryValid) {
                showToast("Please fix the errors in the form")
                return
            }
        }

        // Show loading state
        binding.btnPayNow.isEnabled = false
        binding.btnPayNow.text = "Processing..."

        // Simulate payment processing
        binding.root.postDelayed({
            handlePaymentSuccess()
        }, 2000)
    }

    private fun handlePaymentSuccess() {
        // Reset button state
        binding.btnPayNow.isEnabled = true
        binding.btnPayNow.text = "Pay Now"

        // Show success message
        showToast("Payment successful!")

        // Navigate to success screen or back
        // In a real app, you would navigate to a confirmation screen
        findNavController().navigateUp()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}