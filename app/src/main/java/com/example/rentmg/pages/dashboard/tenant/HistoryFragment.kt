package com.example.rentmg.pages.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class PaymentHistory(
    val id: String,
    val amount: Double,
    val paymentDate: Date,
    val paymentMethod: String,
    val status: String, // "Completed", "Pending", "Failed"
    val transactionId: String
)

class HistoryFragment : Fragment() {
    
    private lateinit var rvPaymentHistory: RecyclerView
    private lateinit var tvNoHistory: TextView
    
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var currencyFormat: NumberFormat
    private lateinit var historyAdapter: PaymentHistoryAdapter
    
    private val paymentHistory = mutableListOf<PaymentHistory>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        initializeFormatters()
        setupRecyclerView()
        loadPaymentHistory()
    }

    private fun initializeViews(view: View) {
        rvPaymentHistory = view.findViewById(R.id.rv_payment_history)
        tvNoHistory = view.findViewById(R.id.tv_no_history)
    }

    private fun initializeFormatters() {
        dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
    }

    private fun setupRecyclerView() {
        historyAdapter = PaymentHistoryAdapter(paymentHistory, dateFormat, currencyFormat)
        rvPaymentHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun loadPaymentHistory() {
        // TODO: Fetch from API
        // For now, using sample data
        val sampleData = listOf(
            PaymentHistory(
                id = "1",
                amount = 25000.0,
                paymentDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -5)
                }.time,
                paymentMethod = "M-Pesa",
                status = "Completed",
                transactionId = "TX001"
            ),
            PaymentHistory(
                id = "2",
                amount = 25000.0,
                paymentDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                }.time,
                paymentMethod = "Bank Transfer",
                status = "Completed",
                transactionId = "TX002"
            ),
            PaymentHistory(
                id = "3",
                amount = 25000.0,
                paymentDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -2)
                }.time,
                paymentMethod = "M-Pesa",
                status = "Completed",
                transactionId = "TX003"
            ),
            PaymentHistory(
                id = "4",
                amount = 25000.0,
                paymentDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -3)
                }.time,
                paymentMethod = "Card",
                status = "Completed",
                transactionId = "TX004"
            )
        )
        
        paymentHistory.addAll(sampleData)
        historyAdapter.notifyDataSetChanged()
        
        // Show/hide no history message
        if (paymentHistory.isEmpty()) {
            rvPaymentHistory.visibility = View.GONE
            tvNoHistory.visibility = View.VISIBLE
        } else {
            rvPaymentHistory.visibility = View.VISIBLE
            tvNoHistory.visibility = View.GONE
        }
    }
}

// Adapter for RecyclerView
class PaymentHistoryAdapter(
    private val items: List<PaymentHistory>,
    private val dateFormat: SimpleDateFormat,
    private val currencyFormat: NumberFormat
) : RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(payment: PaymentHistory) {
            val tvAmount = itemView.findViewById<TextView>(R.id.tv_payment_amount)
            val tvDate = itemView.findViewById<TextView>(R.id.tv_payment_date)
            val tvMethod = itemView.findViewById<TextView>(R.id.tv_payment_method)
            val tvStatus = itemView.findViewById<TextView>(R.id.tv_payment_status)
            val tvTransactionId = itemView.findViewById<TextView>(R.id.tv_transaction_id)
            
            tvAmount.text = currencyFormat.format(payment.amount)
            tvDate.text = dateFormat.format(payment.paymentDate)
            tvMethod.text = payment.paymentMethod
            tvStatus.text = payment.status
            tvTransactionId.text = "ID: ${payment.transactionId}"
            
            // Change status color based on payment status
            val statusColor = when (payment.status) {
                "Completed" -> itemView.context.getColor(android.R.color.holo_green_dark)
                "Pending" -> itemView.context.getColor(android.R.color.holo_orange_dark)
                "Failed" -> itemView.context.getColor(android.R.color.holo_red_dark)
                else -> itemView.context.getColor(android.R.color.darker_gray)
            }
            tvStatus.setTextColor(statusColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_history, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}