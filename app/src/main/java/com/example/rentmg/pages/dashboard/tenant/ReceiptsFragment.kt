package com.example.rentmg.pages.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class Receipt(
    val id: String,
    val receiptNumber: String,
    val amount: Double,
    val paymentDate: Date,
    val propertyName: String,
    val houseNumber: String,
    val transactionId: String
)

class ReceiptsFragment : Fragment() {
    
    private lateinit var rvReceipts: RecyclerView
    private lateinit var tvNoReceipts: TextView
    
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var currencyFormat: NumberFormat
    private lateinit var receiptAdapter: ReceiptAdapter
    
    private val receipts = mutableListOf<Receipt>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_receipts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        initializeFormatters()
        setupRecyclerView()
        loadReceipts()
    }

    private fun initializeViews(view: View) {
        rvReceipts = view.findViewById(R.id.rv_receipts)
        tvNoReceipts = view.findViewById(R.id.tv_no_receipts)
    }

    private fun initializeFormatters() {
        dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
    }

    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter(receipts, dateFormat, currencyFormat) { receipt ->
            onReceiptActionClicked(receipt)
        }
        rvReceipts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = receiptAdapter
        }
    }

    private fun loadReceipts() {
        // TODO: Fetch from API
        // For now, using sample data
        val sampleData = listOf(
            Receipt(
                id = "1",
                receiptNumber = "RCP-2025-001",
                amount = 25000.0,
                paymentDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -5)
                }.time,
                propertyName = "Greenview Apartments",
                houseNumber = "A-102",
                transactionId = "TX001"
            ),
            Receipt(
                id = "2",
                receiptNumber = "RCP-2025-002",
                amount = 25000.0,
                paymentDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                }.time,
                propertyName = "Greenview Apartments",
                houseNumber = "A-102",
                transactionId = "TX002"
            ),
            Receipt(
                id = "3",
                receiptNumber = "RCP-2025-003",
                amount = 25000.0,
                paymentDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -2)
                }.time,
                propertyName = "Greenview Apartments",
                houseNumber = "A-102",
                transactionId = "TX003"
            )
        )
        
        receipts.addAll(sampleData)
        receiptAdapter.notifyDataSetChanged()
        
        // Show/hide no receipts message
        if (receipts.isEmpty()) {
            rvReceipts.visibility = View.GONE
            tvNoReceipts.visibility = View.VISIBLE
        } else {
            rvReceipts.visibility = View.VISIBLE
            tvNoReceipts.visibility = View.GONE
        }
    }

    private fun onReceiptActionClicked(receipt: Receipt) {
        // TODO: Implement actions (View, Download, Share, Print)
        Toast.makeText(requireContext(), "Receipt ${receipt.receiptNumber} clicked", Toast.LENGTH_SHORT).show()
    }
}

// Adapter for RecyclerView
class ReceiptAdapter(
    private val items: List<Receipt>,
    private val dateFormat: SimpleDateFormat,
    private val currencyFormat: NumberFormat,
    private val onActionClick: (Receipt) -> Unit
) : RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {

    inner class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(receipt: Receipt) {
            val tvReceiptNumber = itemView.findViewById<TextView>(R.id.tv_receipt_number)
            val tvAmount = itemView.findViewById<TextView>(R.id.tv_receipt_amount)
            val tvDate = itemView.findViewById<TextView>(R.id.tv_receipt_date)
            val tvProperty = itemView.findViewById<TextView>(R.id.tv_receipt_property)
            val btnViewReceipt = itemView.findViewById<Button>(R.id.btn_view_receipt)
            val btnDownloadReceipt = itemView.findViewById<Button>(R.id.btn_download_receipt)
            
            tvReceiptNumber.text = receipt.receiptNumber
            tvAmount.text = currencyFormat.format(receipt.amount)
            tvDate.text = "Date: ${dateFormat.format(receipt.paymentDate)}"
            tvProperty.text = "${receipt.propertyName} - ${receipt.houseNumber}"
            
            btnViewReceipt.setOnClickListener {
                onActionClick(receipt)
                Toast.makeText(itemView.context, "View Receipt ${receipt.receiptNumber}", Toast.LENGTH_SHORT).show()
            }
            
            btnDownloadReceipt.setOnClickListener {
                Toast.makeText(itemView.context, "Download Receipt ${receipt.receiptNumber}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receipt, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}