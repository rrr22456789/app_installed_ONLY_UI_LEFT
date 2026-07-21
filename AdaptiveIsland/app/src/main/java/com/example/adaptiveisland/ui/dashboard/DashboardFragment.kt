package com.example.adaptiveisland.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adaptiveisland.R
import com.example.adaptiveisland.data.history.AppDatabase
import com.example.adaptiveisland.data.history.UsageHistoryRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Passive presentation container layout updating visual grids from Flow observables.
 */
class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    
    private var rvAppUsage: RecyclerView? = null
    private var rvHistory: RecyclerView? = null
    private var progressBar: ProgressBar? = null

    private var appUsageAdapter: AppUsageAdapter? = null
    private var historyAdapter: HistoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        rvAppUsage = view.findViewById(R.id.rvAppUsage)
        rvHistory = view.findViewById(R.id.rvHistory)
        progressBar = view.findViewById(R.id.progressBar)

        // Resolve dependencies dynamically via factory pattern implementation hooks
        val appContext = requireContext().applicationContext
        val database = AppDatabase.getDatabase(appContext)
        val repository = UsageHistoryRepository(database.usageDao())
        
        viewModel = ViewModelProvider(this, DashboardViewModelFactory(repository))[DashboardViewModel::class.java]

        setupRecyclerViews()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    progressBar?.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                    appUsageAdapter?.submitList(uiState.todayAppsUsage)
                    historyAdapter?.submitList(uiState.historicalUsage)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Provide current calendar configurations at structural validation points
        val currentDayString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        viewModel.loadDashboardData(currentDayString)
    }

    override fun onDestroyView() {
        // Zero view references and adapter dependencies entirely to avoid lifecycle memory containment leaks
        rvAppUsage?.adapter = null
        rvHistory?.adapter = null
        rvAppUsage = null
        rvHistory = null
        progressBar = null
        appUsageAdapter = null
        historyAdapter = null
        super.onDestroyView()
    }

    private fun setupRecyclerViews() {
        appUsageAdapter = AppUsageAdapter()
        rvAppUsage?.layoutManager = LinearLayoutManager(requireContext())
        rvAppUsage?.adapter = appUsageAdapter

        historyAdapter = HistoryAdapter()
        rvHistory?.layoutManager = LinearLayoutManager(requireContext())
        rvHistory?.adapter = historyAdapter
    }
}