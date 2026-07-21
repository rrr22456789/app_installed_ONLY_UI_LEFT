package com.example.adaptiveisland.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adaptiveisland.data.settings.AdaptiveIslandPreferences
import com.example.adaptiveisland.data.settings.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Pure architectural ViewModel orchestrating layout preference update actions.
 * Leverages the frozen repository data lane exclusively to complete updates.
 */
class SettingsViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {

    /**
     * Exposes the system user tracking preferences profile downstream as an immutable state tree.
     */
    val preferencesState: StateFlow<AdaptiveIslandPreferences?> = repository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    /**
     * Routes toggle mutations safely into the backing preference persistence layer.
     */
    fun toggleOverlayEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateOverlayEnabled(isEnabled)
        }
    }

    /**
     * Modifies overlay display bounds parameters programmatic constraints safely.
     */
    fun updateDimensions(width: Int, height: Int) {
        viewModelScope.launch {
            repository.updateCapsuleDimensions(width, height)
        }
    }
}

/**
 * Production-ready factory supplying context dependencies to the settings presentation flow.
 */
class SettingsViewModelFactory(
    private val repository: PreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}