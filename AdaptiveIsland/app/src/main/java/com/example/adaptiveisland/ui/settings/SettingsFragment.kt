package com.example.adaptiveisland.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.adaptiveisland.R
import com.example.adaptiveisland.data.settings.PreferencesRepository
import kotlinx.coroutines.launch

/**
 * Passive presentation container capturing user layout preference options.
 */
class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel

    private var switchOverlay: SwitchCompat? = null
    private var etWidth: EditText? = null
    private var etHeight: EditText? = null
    private var btnSave: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        switchOverlay = view.findViewById(R.id.switchOverlay)
        etWidth = view.findViewById(R.id.etWidth)
        etHeight = view.findViewById(R.id.etHeight)
        btnSave = view.findViewById(R.id.btnSave)

        val repository = PreferencesRepository(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, SettingsViewModelFactory(repository))[SettingsViewModel::class.java]

        setupListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        switchOverlay = null
        etWidth = null
        etHeight = null
        btnSave = null
        super.onDestroyView()
    }

    private fun setupListeners() {
        switchOverlay?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleOverlayEnabled(isChecked)
        }

        btnSave?.setOnClickListener {
            val width = etWidth?.text?.toString()?.toIntOrNull() ?: 500
            val height = etHeight?.text?.toString()?.toIntOrNull() ?: 120
            viewModel.updateDimensions(width, height)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.preferencesState.collect { prefs ->
                    prefs?.let {
                        if (switchOverlay?.isChecked != it.isOverlayEnabled) {
                            switchOverlay?.isChecked = it.isOverlayEnabled
                        }
                        // Update values if focus fields are not actively capturing input strings
                        if (etWidth?.hasFocus() == false) {
                            etWidth?.setText(it.capsuleWidth.toString())
                        }
                        if (etHeight?.hasFocus() == false) {
                            etHeight?.setText(it.capsuleHeight.toString())
                        }
                    }
                }
            }
        }
    }
}