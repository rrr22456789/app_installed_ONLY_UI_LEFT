package com.example.adaptiveisland.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adaptiveisland.data.history.AppUsageEntity
import com.example.adaptiveisland.data.history.DailyUsageEntity
import com.example.adaptiveisland.data.history.UsageHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Immutable representation of the user dashboard UI layer data state.
 */
data class DashboardUiState(
    val todayAppsUsage: List<AppUsageEntity> = emptyList(),
    val historicalUsage: List<DailyUsageEntity> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * Pure architectural ViewModel coordinating background history retrieval tasks.
 */
class DashboardViewModel(
    private val repository: UsageHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Executes repository queries concurrently on an IO dispatcher thread pool.
     * 
     * @param dateStr The pre-formatted yyyy-MM-dd date key supplied by the presentation layer.
     */
    fun loadDashboardData(dateStr: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Leverage structured coroutines to poll database partitions concurrently
            val appsDeferred = async(Dispatchers.IO) { repository.getAllAppsForDate(dateStr) }
            val historyDeferred = async(Dispatchers.IO) { repository.getDailyHistory() }

            val appsList = appsDeferred.await()
            val historyList = historyDeferred.await()

            _uiState.value = DashboardUiState(
                todayAppsUsage = appsList,
                historicalUsage = historyList,
                isLoading = false
            )
        }
    }
}

/**
 * Production-ready factory instance isolating ViewModel injection pathways safely.
 */
class DashboardViewModelFactory(
    private val repository: UsageHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}