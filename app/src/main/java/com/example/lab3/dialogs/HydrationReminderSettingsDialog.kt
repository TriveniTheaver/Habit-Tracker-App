package com.example.lab3.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.lab3.data.HydrationSettings
import com.example.lab3.databinding.DialogHydrationReminderSettingsBinding

class HydrationReminderSettingsDialog : DialogFragment() {
    
    private var _binding: DialogHydrationReminderSettingsBinding? = null
    private val binding get() = _binding!!
    
    private var onSettingsSaved: ((HydrationSettings) -> Unit)? = null
    
    companion object {
        fun newInstance(
            currentSettings: HydrationSettings,
            onSettingsSaved: (HydrationSettings) -> Unit
        ): HydrationReminderSettingsDialog {
            val dialog = HydrationReminderSettingsDialog()
            dialog.arguments = Bundle().apply {
                putSerializable("current_settings", currentSettings)
            }
            dialog.onSettingsSaved = onSettingsSaved
            return dialog
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogHydrationReminderSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val currentSettings = arguments?.getSerializable("current_settings") as? HydrationSettings
            ?: HydrationSettings()
        
        setupUI(currentSettings)
        setupClickListeners()
    }
    
    private fun setupUI(settings: HydrationSettings) {
        binding.switchEnableReminders.isChecked = settings.isEnabled
        binding.editReminderInterval.setText(settings.reminderInterval.toString())
        binding.editStartTime.setText(settings.startTime)
        binding.editEndTime.setText(settings.endTime)
        binding.switchSound.isChecked = settings.soundEnabled
        binding.switchVibration.isChecked = settings.vibrationEnabled
        binding.editCustomMessage.setText(settings.customMessage)
        
        // Setup interval unit spinner
        setupIntervalUnitSpinner(settings.reminderInterval)
    }
    
    private fun setupIntervalUnitSpinner(currentInterval: Int) {
        val units = listOf("seconds", "minutes", "hours")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIntervalUnit.adapter = adapter
        
        // Set default selection based on current interval
        // If interval is less than 1 minute (stored as seconds), show seconds
        // If interval is less than 60 minutes, show minutes
        // Otherwise show hours
        when {
            currentInterval < 1 -> {
                // Stored in seconds (less than 1 minute)
                binding.spinnerIntervalUnit.setSelection(0) // seconds
                binding.editReminderInterval.setText((currentInterval * 60).toString()) // Convert to seconds for display
            }
            currentInterval < 60 -> {
                // Stored in minutes
                binding.spinnerIntervalUnit.setSelection(1) // minutes
            }
            else -> {
                // Stored in minutes but display as hours
                binding.spinnerIntervalUnit.setSelection(2) // hours
                binding.editReminderInterval.setText((currentInterval / 60).toString())
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
        
        binding.buttonSave.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun saveSettings() {
        try {
            val isEnabled = binding.switchEnableReminders.isChecked
            val intervalValue = binding.editReminderInterval.text.toString().toIntOrNull() ?: 2
            val selectedUnit = binding.spinnerIntervalUnit.selectedItem?.toString() ?: "hours"
            val startTime = binding.editStartTime.text.toString().ifEmpty { "08:00" }
            val endTime = binding.editEndTime.text.toString().ifEmpty { "22:00" }
            val soundEnabled = binding.switchSound.isChecked
            val vibrationEnabled = binding.switchVibration.isChecked
            val customMessage = binding.editCustomMessage.text.toString().ifEmpty { "Time to hydrate! 💧" }
            
            // Convert to minutes for internal storage
            val intervalInMinutes = when (selectedUnit) {
                "seconds" -> {
                    // Convert seconds to minutes (with decimal support)
                    // Minimum 10 seconds = 0.166... minutes, but we'll store as fraction
                    // For WorkManager, we'll use the minimum of 15 minutes or convert properly
                    val seconds = intervalValue.coerceIn(10, 3600) // 10 seconds to 1 hour
                    // Store as negative to indicate seconds (hack for testing)
                    // We'll handle this specially in the scheduler
                    -seconds // Negative indicates seconds
                }
                "minutes" -> {
                    intervalValue.coerceIn(1, 1440) // 1 minute to 24 hours
                }
                else -> { // hours
                    (intervalValue * 60).coerceIn(60, 1440) // 1 hour to 24 hours in minutes
                }
            }
            
            val newSettings = HydrationSettings(
                isEnabled = isEnabled,
                reminderInterval = intervalInMinutes, // Store in minutes (or negative for seconds)
                startTime = startTime,
                endTime = endTime,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                customMessage = customMessage
            )
            
            onSettingsSaved?.invoke(newSettings)
            dismiss()
        } catch (e: Exception) {
            // Show error toast
            android.widget.Toast.makeText(requireContext(), "Error saving settings: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
