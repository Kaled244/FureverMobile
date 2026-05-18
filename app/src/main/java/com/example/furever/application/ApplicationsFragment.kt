package com.example.furever.application

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.furever.R
import com.example.furever.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.util.Locale

class ApplicationsFragment : Fragment() {

    private lateinit var rvApplications: RecyclerView
    private lateinit var adapter: ApplicationAdapter
    private var allApplications: List<ApplicationResponse> = emptyList()
    private var activeFilter = "ALL"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_applications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvApplications = view.findViewById(R.id.rv_applications)
        rvApplications.layoutManager = LinearLayoutManager(context)

        adapter = ApplicationAdapter(emptyList()) { app ->
            showApplicationDetail(app)
        }
        rvApplications.adapter = adapter

        // Setup Filter Pills
        val filterAll = view.findViewById<TextView>(R.id.filter_app_all)
        val filterPending = view.findViewById<TextView>(R.id.filter_app_pending)
        val filterApproved = view.findViewById<TextView>(R.id.filter_app_approved)
        val filterRejected = view.findViewById<TextView>(R.id.filter_app_rejected)

        val filters = mapOf(
            "ALL" to filterAll,
            "PENDING" to filterPending,
            "APPROVED" to filterApproved,
            "REJECTED" to filterRejected
        )

        filters.forEach { (type, tv) ->
            tv?.setOnClickListener {
                activeFilter = type
                filters.values.forEach {
                    it?.setBackgroundResource(R.drawable.bg_filter_inactive)
                    it?.setTextColor(Color.parseColor("#3A2525"))
                }
                tv?.setBackgroundResource(R.drawable.bg_filter_active)
                tv?.setTextColor(Color.WHITE)
                applyFilters()
            }
        }

        fetchMyApplications()
    }

    private fun applyFilters() {
        val filtered = if (activeFilter == "ALL") allApplications
        // FIX: Changed appStatus to status
        else allApplications.filter { it.status?.uppercase(Locale.ROOT) == activeFilter }
        adapter.updateApplications(filtered)
    }

    private fun showApplicationDetail(app: ApplicationResponse) {
        val ctx = context ?: return
        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_application_detail, null)

        // FIX: Changed appId to id
        val appIdText = app.id?.let { String.format(Locale.ROOT, "%04d", it) } ?: "????"
        dialogView.findViewById<TextView>(R.id.tv_detail_app_id).text = "Application #$appIdText"

        // FIX: Changed appPet to pet
        dialogView.findViewById<TextView>(R.id.tv_detail_title).text = "Review for ${app.pet?.pName ?: "Pet"}"

        // FIX: Changed appNewName to newPetName
        dialogView.findViewById<TextView>(R.id.tv_detail_new_name).text = app.newPetName ?: app.pet?.pName ?: "N/A"

        // FIX: Changed appContact to contactNumber
        dialogView.findViewById<TextView>(R.id.tv_detail_contact).text = app.contactNumber ?: "N/A"

        // FIX: Changed appHomeType to homeType
        dialogView.findViewById<TextView>(R.id.tv_detail_home).text = app.homeType ?: "N/A"

        // FIX: Changed appExperience to experience
        val expText = app.experience?.name?.lowercase(Locale.ROOT)?.replace("_", " ")?.let {
            it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString() }
        } ?: "N/A"
        dialogView.findViewById<TextView>(R.id.tv_detail_exp).text = expText

        // FIX: Changed appAnswers to answers
        dialogView.findViewById<TextView>(R.id.tv_detail_answer).text = app.answers ?: "No details provided."

        val btnClaim = dialogView.findViewById<MaterialButton>(R.id.btn_detail_claim)

        // FIX: Changed appStatus to status
        if (app.status?.uppercase(Locale.ROOT) == "APPROVED") {
            btnClaim.visibility = View.VISIBLE
            btnClaim.setOnClickListener {
                // FIX: Changed appId to id
                app.id?.let { handleClaim(it) }
            }
        }

        MaterialAlertDialogBuilder(ctx).setView(dialogView).show()
    }

    private fun handleClaim(appId: Int) {
        val token = context?.getSharedPreferences("furever_prefs", Context.MODE_PRIVATE)?.getString("auth_token", "") ?: ""
        if (token.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.updateApplicationStatus("Bearer $token", appId, mapOf("status" to "READY_TO_CLAIM"))
                }

                if (response.isSuccessful && isAdded) {
                    Toast.makeText(context, "Confirmed! Ready for pickup.", Toast.LENGTH_SHORT).show()
                    fetchMyApplications()
                }
            } catch (e: Exception) {
                Log.e("AppsFragment", "Claim error", e)
            }
        }
    }

    private fun fetchMyApplications() {
        val token = context?.getSharedPreferences("furever_prefs", Context.MODE_PRIVATE)?.getString("auth_token", "") ?: ""
        if (token.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMyApplications("Bearer $token")
                }

                if (response.isSuccessful && isAdded) {
                    // This assumes your ApiResponse has a .data field that is a List
                    allApplications = response.body()?.data ?: emptyList()

                    // FIX: Changed appId to id
                    allApplications = allApplications.sortedByDescending { it.id ?: 0 }
                    applyFilters()
                }
            } catch (e: Exception) {
                Log.e("AppsFragment", "Fetch failed", e)
            }
        }
    }
}