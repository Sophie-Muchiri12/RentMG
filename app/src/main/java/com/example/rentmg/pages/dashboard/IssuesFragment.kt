package com.example.rentmg.pages.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentmg.R
import com.example.rentmg.data.model.Issue
import com.example.rentmg.data.model.IssueCreateRequest
import com.example.rentmg.data.model.IssueUpdateRequest
import com.example.rentmg.util.AppManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * IssuesFragment
 * Shows list of maintenance/service issues
 * Tenants can report issues, landlords can update status
 */
class IssuesFragment : Fragment() {

    private lateinit var rvIssues: RecyclerView
    private lateinit var tvNoIssues: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: Button
    private lateinit var fabAddIssue: FloatingActionButton

    private val issues = mutableListOf<Issue>()
    private lateinit var issuesAdapter: IssuesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_issues, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupListeners()
        loadIssues()
    }

    private fun initializeViews(view: View) {
        rvIssues = view.findViewById(R.id.rv_issues)
        tvNoIssues = view.findViewById(R.id.tv_no_issues)
        progressBar = view.findViewById(R.id.progress_bar)
        contentLayout = view.findViewById(R.id.content_layout)
        errorLayout = view.findViewById(R.id.error_layout)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)
        btnRetry = view.findViewById(R.id.btn_retry)
        fabAddIssue = view.findViewById(R.id.fab_add_issue)
    }

    private fun setupRecyclerView() {
        issuesAdapter = IssuesAdapter(issues) { issue ->
            showIssueDetails(issue)
        }

        rvIssues.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = issuesAdapter
        }
    }

    private fun setupListeners() {
        btnRetry.setOnClickListener {
            loadIssues()
        }

        fabAddIssue.setOnClickListener {
            showCreateIssueDialog()
        }
    }

    private fun loadIssues() {
        showLoading()

        AppManager.getApiService().listIssues().enqueue(object : Callback<List<Issue>> {
            override fun onResponse(call: Call<List<Issue>>, response: Response<List<Issue>>) {
                if (response.isSuccessful) {
                    val issuesList = response.body()
                    if (issuesList != null) {
                        issues.clear()
                        issues.addAll(issuesList)
                        issuesAdapter.notifyDataSetChanged()

                        if (issues.isEmpty()) {
                            showEmptyState()
                        } else {
                            showContent()
                        }
                    } else {
                        showEmptyState()
                    }
                } else {
                    showError("Failed to load issues: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Issue>>, t: Throwable) {
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun showCreateIssueDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_issue, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.et_issue_title)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_issue_description)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinner_priority)

        // Setup priority spinner
        val priorities = listOf("Low", "Medium", "High", "Urgent")
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter
        spinnerPriority.setSelection(1) // Default to "Medium"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Report Issue")
            .setView(dialogView)
            .setPositiveButton("Report") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val priority = priorities[spinnerPriority.selectedItemPosition].lowercase()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter issue title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                createIssue(title, description, priority)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun createIssue(title: String, description: String, priority: String) {
        Toast.makeText(requireContext(), "Reporting issue...", Toast.LENGTH_SHORT).show()

        val request = IssueCreateRequest(
            title = title,
            description = description,
            priority = priority
        )

        AppManager.getApiService().createIssue(request).enqueue(object : Callback<Issue> {
            override fun onResponse(call: Call<Issue>, response: Response<Issue>) {
                if (response.isSuccessful) {
                    val issue = response.body()
                    if (issue != null) {
                        Toast.makeText(
                            requireContext(),
                            "Issue reported successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        issues.add(0, issue)
                        issuesAdapter.notifyItemInserted(0)

                        if (issues.size == 1) {
                            showContent()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to report issue: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Issue>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showIssueDetails(issue: Issue) {
        val message = """
            Title: ${issue.title}
            Description: ${issue.description ?: "N/A"}
            Status: ${issue.status}
            Priority: ${issue.priority}
            Created: ${issue.createdAt}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Issue Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Update Status") { _, _ ->
                showUpdateStatusDialog(issue)
            }
            .show()
    }

    private fun showUpdateStatusDialog(issue: Issue) {
        val statuses = arrayOf("open", "in_progress", "resolved", "closed")
        val currentIndex = statuses.indexOf(issue.status)

        AlertDialog.Builder(requireContext())
            .setTitle("Update Status")
            .setSingleChoiceItems(statuses, currentIndex) { dialog, which ->
                val newStatus = statuses[which]
                updateIssueStatus(issue.id, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateIssueStatus(issueId: Int, newStatus: String) {
        Toast.makeText(requireContext(), "Updating status...", Toast.LENGTH_SHORT).show()

        val request = IssueUpdateRequest(status = newStatus)

        AppManager.getApiService().updateIssue(issueId, request).enqueue(object : Callback<Issue> {
            override fun onResponse(call: Call<Issue>, response: Response<Issue>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Status updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadIssues() // Refresh list
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update status: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Issue>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        tvNoIssues.visibility = View.GONE
        rvIssues.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        tvNoIssues.visibility = View.VISIBLE
        rvIssues.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        contentLayout.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvErrorMessage.text = message
    }
}

class IssuesAdapter(
    private val items: List<Issue>,
    private val onIssueClick: (Issue) -> kotlin.Unit
) : RecyclerView.Adapter<IssuesAdapter.IssueViewHolder>() {

    inner class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(issue: Issue) {
            val tvIssueTitle = itemView.findViewById<TextView>(R.id.tv_issue_title)
            val tvIssueDescription = itemView.findViewById<TextView>(R.id.tv_issue_description)
            val tvIssueStatus = itemView.findViewById<TextView>(R.id.tv_issue_status)
            val tvIssuePriority = itemView.findViewById<TextView>(R.id.tv_issue_priority)

            tvIssueTitle.text = issue.title
            tvIssueDescription.text = issue.description ?: "No description"
            tvIssueStatus.text = issue.status.uppercase()
            tvIssuePriority.text = issue.priority.uppercase()

            // Set status color
            when (issue.status.lowercase()) {
                "open" -> tvIssueStatus.setBackgroundColor(0xFFF44336.toInt())
                "in_progress" -> tvIssueStatus.setBackgroundColor(0xFF2196F3.toInt())
                "resolved" -> tvIssueStatus.setBackgroundColor(0xFF4CAF50.toInt())
                "closed" -> tvIssueStatus.setBackgroundColor(0xFF9E9E9E.toInt())
                else -> tvIssueStatus.setBackgroundColor(0xFF9E9E9E.toInt())
            }

            // Set priority color
            when (issue.priority.lowercase()) {
                "urgent" -> tvIssuePriority.setBackgroundColor(0xFFD32F2F.toInt())
                "high" -> tvIssuePriority.setBackgroundColor(0xFFF57C00.toInt())
                "medium" -> tvIssuePriority.setBackgroundColor(0xFFFFC107.toInt())
                "low" -> tvIssuePriority.setBackgroundColor(0xFF388E3C.toInt())
                else -> tvIssuePriority.setBackgroundColor(0xFF9E9E9E.toInt())
            }

            itemView.setOnClickListener {
                onIssueClick(issue)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
