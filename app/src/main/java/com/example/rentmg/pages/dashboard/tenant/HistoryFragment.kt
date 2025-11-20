package com.example.rentmg.pages.dashboard.tenant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import com.example.rentmg.data.model.Lease
import com.example.rentmg.data.model.Payment
import com.example.rentmg.util.AppManager
import retrofit2.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * HistoryFragment
 * Displays tenant's payment transaction history
 *
 * Features:
 * - Fetches payment history from backend API
 * - Displays payments in a scrollable list
 * - Shows payment status (completed, pending, failed)
 * - Displays transaction details (amount, date, reference)
 * - Handles loading and error states
 *
 * Data Flow:
 * 1. Fragment loads
 * 2. Fetches tenant's lease from API
 * 3. Fetches payment history for that lease
 * 4. Displays payments in RecyclerView
 */
class HistoryFragment : Fragment() {

    // ============================================
    // UI COMPONENTS
    // ============================================
    private lateinit var rvPaymentHistory: RecyclerView
    private lateinit var tvNoHistory: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: Button

    // ============================================
    // DATA - Formatters
    // ============================================
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var currencyFormat: NumberFormat

    // ============================================
    // DATA - Adapter
    // ============================================
    private lateinit var historyAdapter: PaymentHistoryAdapter

    // ============================================
    // DATA - Payment List
    // ============================================
    private val paymentHistory = mutableListOf<Payment>()

    // ============================================
    // DATA - Lease ID
    // ============================================
    private var currentLeaseId: Int? = null

    /**
     * Fragment lifecycle: onCreateView
     * Inflates the fragment layout
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    /**
     * Fragment lifecycle: onViewCreated
     * Called after view is created
     * Initializes everything and loads data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize all UI components
        initializeViews(view)

        // Initialize date and currency formatters
        initializeFormatters()

        // Setup RecyclerView with adapter
        setupRecyclerView()

        // Setup button click listeners
        setupListeners()

        // Fetch payment history from backend API
        loadPaymentHistory()
    }

    /**
     * Initializes all UI components by finding them by ID
     * Called once during onViewCreated
     */
    private fun initializeViews(view: View) {
        rvPaymentHistory = view.findViewById(R.id.rv_payment_history)
        tvNoHistory = view.findViewById(R.id.tv_no_history)
        progressBar = view.findViewById(R.id.progress_bar)
        errorLayout = view.findViewById(R.id.error_layout)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)
        btnRetry = view.findViewById(R.id.btn_retry)
    }

    /**
     * Initializes date and currency formatters
     * Used for displaying dates and money amounts
     */
    private fun initializeFormatters() {
        // Date formatter with time (e.g., "15 Jan 2025, 02:30 PM")
        dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        // Currency formatter for Kenyan Shillings (e.g., "KES 25,000.00")
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
    }

    /**
     * Sets up RecyclerView with adapter and layout manager
     * RecyclerView displays payment history in a scrollable list
     */
    private fun setupRecyclerView() {
        // Create adapter with empty list (will be populated later)
        historyAdapter = PaymentHistoryAdapter(paymentHistory, dateFormat, currencyFormat)

        // Configure RecyclerView
        rvPaymentHistory.apply {
            // Use LinearLayoutManager for vertical list
            layoutManager = LinearLayoutManager(requireContext())

            // Set adapter
            adapter = historyAdapter
        }
    }

    /**
     * Sets up click listeners for interactive elements
     */
    private fun setupListeners() {
        // Retry button - retries loading data after error
        btnRetry.setOnClickListener {
            loadPaymentHistory()
        }
    }

    /**
     * Loads payment history from backend API
     * First fetches tenant's lease, then fetches payments for that lease
     * Shows loading state while fetching
     */
    private fun loadPaymentHistory() {
        // Show loading state
        showLoading()

        // Step 1: Fetch tenant's lease from API
        // Need lease ID to fetch payments
        AppManager.getApiService().listLeases().enqueue(object : Callback<List<Lease>> {
            override fun onResponse(call: Call<List<Lease>>, response: Response<List<Lease>>) {
                if (response.isSuccessful) {
                    // Get list of leases
                    val leases = response.body()

                    if (leases != null && leases.isNotEmpty()) {
                        // Get the first active lease
                        val lease = leases.firstOrNull { it.status == "active" } ?: leases.first()
                        currentLeaseId = lease.id

                        // Step 2: Fetch payment history for this lease
                        loadPaymentsForLease(lease.id)
                    } else {
                        // No leases found for this tenant
                        showError("No lease found. Cannot load payment history.")
                    }
                } else {
                    // API returned error
                    showError("Failed to load lease data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Lease>>, t: Throwable) {
                // Network error
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Loads payment history for a specific lease
     * Called after lease is fetched
     *
     * @param leaseId ID of the lease to fetch payments for
     */
    private fun loadPaymentsForLease(leaseId: Int) {
        // Fetch payment history from API
        AppManager.getApiService().getPaymentHistory(leaseId).enqueue(object : Callback<List<Payment>> {
            override fun onResponse(call: Call<List<Payment>>, response: Response<List<Payment>>) {
                if (response.isSuccessful) {
                    // Get list of payments
                    val payments = response.body()

                    if (payments != null) {
                        // Clear existing data
                        paymentHistory.clear()

                        // Add new data (backend already returns newest first)
                        paymentHistory.addAll(payments)

                        // Notify adapter that data changed
                        historyAdapter.notifyDataSetChanged()

                        // Show content or empty state
                        if (paymentHistory.isEmpty()) {
                            showEmptyState()
                        } else {
                            showContent()
                        }
                    } else {
                        // No payments found
                        showEmptyState()
                    }
                } else {
                    // API returned error
                    showError("Failed to load payment history: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Payment>>, t: Throwable) {
                // Network error
                showError("Network error: ${t.message}")
            }
        })
    }

    /**
     * Shows loading state
     * Hides content and error, shows progress bar
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        rvPaymentHistory.visibility = View.GONE
        tvNoHistory.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    /**
     * Shows content state (payment list)
     * Hides loading, error, and empty state
     */
    private fun showContent() {
        progressBar.visibility = View.GONE
        rvPaymentHistory.visibility = View.VISIBLE
        tvNoHistory.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    /**
     * Shows empty state (no payments)
     * Hides loading, error, and content
     */
    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        rvPaymentHistory.visibility = View.GONE
        tvNoHistory.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
    }

    /**
     * Shows error state
     * Hides loading, content, and empty state
     *
     * @param message Error message to display
     */
    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        rvPaymentHistory.visibility = View.GONE
        tvNoHistory.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvErrorMessage.text = message
    }
}

/**
 * PaymentHistoryAdapter
 * RecyclerView adapter for displaying payment history
 *
 * Responsibilities:
 * - Creates ViewHolder for each payment item
 * - Binds payment data to views
 * - Formats dates and currency
 * - Colors status text based on payment status
 */
class PaymentHistoryAdapter(
    // List of payments to display
    private val items: List<Payment>,

    // Date formatter for payment dates
    private val dateFormat: SimpleDateFormat,

    // Currency formatter for amounts
    private val currencyFormat: NumberFormat
) : RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder>() {

    /**
     * PaymentViewHolder
     * Holds references to views in each payment item
     * Binds payment data to those views
     */
    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Binds a Payment object to the view
         * Formats and displays all payment information
         *
         * @param payment Payment object to display
         */
        fun bind(payment: Payment) {
            // Find all views in the item layout
            val tvAmount = itemView.findViewById<TextView>(R.id.tv_payment_amount)
            val tvDate = itemView.findViewById<TextView>(R.id.tv_payment_date)
            val tvMethod = itemView.findViewById<TextView>(R.id.tv_payment_method)
            val tvStatus = itemView.findViewById<TextView>(R.id.tv_payment_status)
            val tvTransactionId = itemView.findViewById<TextView>(R.id.tv_transaction_id)

            // Format and display amount with currency
            tvAmount.text = currencyFormat.format(payment.amount)

            // Parse and format created date
            // Backend sends dates in ISO 8601 format (e.g., "2025-01-15T14:30:00")
            try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = isoFormat.parse(payment.updatedAt ?: payment.createdAt)
                tvDate.text = if (date != null) dateFormat.format(date) else payment.createdAt
            } catch (e: Exception) {
                // If parsing fails, show raw date
                tvDate.text = payment.createdAt
            }

            // Display payment method (e.g., "mpesa", "bank_transfer")
            tvMethod.text = when (payment.method.lowercase()) {
                "mpesa" -> "M-Pesa"
                "bank_transfer" -> "Bank Transfer"
                "cash" -> "Cash"
                else -> payment.method.replaceFirstChar { it.uppercase() }
            }

            // Display payment status
            tvStatus.text = when (payment.status.lowercase()) {
                "completed" -> "Completed"
                "pending" -> "Pending"
                "failed" -> "Failed"
                "refunded" -> "Refunded"
                else -> payment.status.replaceFirstChar { it.uppercase() }
            }

            // Display transaction reference (M-Pesa code or payment ID)
            tvTransactionId.text = if (!payment.reference.isNullOrEmpty()) {
                "Ref: ${payment.reference}"
            } else {
                "ID: ${payment.id}"
            }

            // Set status text color based on payment status
            val statusColor = when (payment.status.lowercase()) {
                "completed" -> ContextCompat.getColor(itemView.context, R.color.success)
                "pending" -> ContextCompat.getColor(itemView.context, R.color.warning)
                "failed" -> ContextCompat.getColor(itemView.context, R.color.error)
                "refunded" -> ContextCompat.getColor(itemView.context, R.color.info)
                else -> ContextCompat.getColor(itemView.context, R.color.text_secondary)
            }
            tvStatus.setTextColor(statusColor)
        }
    }

    /**
     * Creates a new ViewHolder
     * Called by RecyclerView when it needs a new item view
     *
     * @param parent Parent ViewGroup
     * @param viewType View type (unused, we only have one type)
     * @return New PaymentViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_history, parent, false)

        // Create and return ViewHolder
        return PaymentViewHolder(view)
    }

    /**
     * Binds data to a ViewHolder
     * Called by RecyclerView when an item needs to be displayed
     *
     * @param holder ViewHolder to bind data to
     * @param position Position of item in the list
     */
    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        // Get payment at this position
        val payment = items[position]

        // Bind payment data to holder
        holder.bind(payment)
    }

    /**
     * Returns the total number of items in the list
     * Called by RecyclerView to determine how many items to display
     *
     * @return Number of payments in the list
     */
    override fun getItemCount() = items.size
}
