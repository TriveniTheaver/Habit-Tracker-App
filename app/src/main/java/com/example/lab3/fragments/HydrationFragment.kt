package com.example.lab3.fragments

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab3.R
import com.example.lab3.adapters.HydrationEntriesAdapter
import com.example.lab3.data.HydrationData
import com.example.lab3.data.HydrationEntry
import com.example.lab3.data.HydrationSettings
import com.example.lab3.data.WellnessDataManager
import com.example.lab3.databinding.FragmentHydrationBinding
import com.example.lab3.dialogs.HydrationReminderSettingsDialog
import com.example.lab3.utils.NotificationHelper
import com.example.lab3.views.WaterBackgroundView
import com.example.lab3.work.HydrationReminderWorker
import java.text.SimpleDateFormat
import java.util.*

class HydrationFragment : Fragment() {
    
    private var _binding: FragmentHydrationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: WellnessDataManager
    private lateinit var hydrationAdapter: HydrationEntriesAdapter
    private var hydrationData: HydrationData? = null
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Mode management
    private var isGlassMode = false // false = amount mode, true = glass mode
    
    // Motivational messages
    private val motivationalMessages: Map<com.example.lab3.views.CircularHydrationView.HydrationLevel, List<String>> = mapOf(
        com.example.lab3.views.CircularHydrationView.HydrationLevel.LOW to listOf(
            "Let's start your hydration journey! 💧",
            "Every drop counts! 💦",
            "Time to hydrate! 🌊"
        ),
        com.example.lab3.views.CircularHydrationView.HydrationLevel.STARTING to listOf(
            "Good start! Keep going! 💪",
            "You're on the right track! 🌟",
            "Keep hydrating! 💧"
        ),
        com.example.lab3.views.CircularHydrationView.HydrationLevel.GOOD to listOf(
            "Great progress! 🎉",
            "You're doing amazing! ⭐",
            "Keep up the great work! 💪"
        ),
        com.example.lab3.views.CircularHydrationView.HydrationLevel.EXCELLENT to listOf(
            "Almost there! 🌟",
            "You're crushing it! 💪",
            "So close to your goal! 🎯"
        ),
        com.example.lab3.views.CircularHydrationView.HydrationLevel.COMPLETE to listOf(
            "Goal achieved! 🎉",
            "You're a hydration champion! 🏆",
            "Perfect hydration day! ⭐"
        )
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            dataManager = WellnessDataManager(requireContext())
            setupRecyclerView()
            setupQuickAddButtons()
            setupQuantityOptionsButton()
            setupWaterDropClick()
            loadHydrationData()
            setupReminderToggle()
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error in onViewCreated: ${e.message}")
        }
    }
    
    private fun setupRecyclerView() {
        hydrationAdapter = HydrationEntriesAdapter(mutableListOf()) { entry ->
            removeHydrationEntry(entry)
        }
        binding.recyclerViewHydrationEntries.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hydrationAdapter
        }
    }
    
    private fun setupWaterDropClick() {
        binding.textHistoryIcon?.setOnClickListener {
            toggleHistoryVisibility()
        }
    }
    
    private fun setupQuickAddButtons() {
        // Amount mode buttons
        binding.buttonQuick50?.setOnClickListener {
            addWater(50)
        }
        
        binding.buttonQuick250?.setOnClickListener {
            addWater(250)
        }
        
        binding.buttonQuick500?.setOnClickListener {
            addWater(500)
        }
        
        // Glass mode button
        binding.buttonAddGlass?.setOnClickListener {
            addWater(250) // Each glass = 250ml (2000ml / 8 glasses)
        }
    }
    
    /**
     * Sets up the mode switch button to toggle between amount and glass modes.
     */
    private fun setupQuantityOptionsButton() {
        binding.buttonModeSwitch?.setOnClickListener {
            if (isGlassMode) {
                showGlassModeOptionsDialog()
            } else {
                showQuantityOptionsDialog()
            }
        }
    }
    
    /**
     * Shows a dialog to switch to glass mode.
     */
    private fun showQuantityOptionsDialog() {
        val options = arrayOf("Switch to Glass Mode")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Options")
            .setItems(options) { _, which ->
                switchToGlassMode() // Switch to Glass Mode
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Shows options when in glass mode.
     */
    private fun showGlassModeOptionsDialog() {
        val options = arrayOf("Switch to ML Mode", "Add 1 Glass (250ml)", "Add 2 Glasses (500ml)", "Add 4 Glasses (1000ml)")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Glass Mode Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> switchToMLMode() // Switch to ML Mode
                    1 -> addWater(250) // 1 Glass
                    2 -> addWater(500) // 2 Glasses
                    3 -> addWater(1000) // 4 Glasses
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Switches back to ML mode.
     */
    private fun switchToMLMode() {
        isGlassMode = false
        
        binding.layoutQuickButtons?.visibility = View.VISIBLE
        binding.layoutGlassButtons?.visibility = View.GONE
        binding.textQuickAddTitle?.text = "Quick Add"
    }
    
    /**
     * Switches to glass mode and shows the glass button.
     */
    private fun switchToGlassMode() {
        isGlassMode = true
        
        // Hide ml buttons and show glass button
        binding.layoutQuickButtons?.visibility = View.GONE
        binding.layoutGlassButtons?.visibility = View.VISIBLE
        binding.textQuickAddTitle?.text = "Quick Add"
        
        // Update glass progress info
        hydrationData?.let { data ->
            updateGlassProgressInfo(data)
        }
    }
    
    private fun addWater(amount: Int) {
        hydrationData?.let { data ->
            val updatedData = data.addEntry(amount)
            dataManager.saveHydrationData(updatedData)
            hydrationData = updatedData
            updateUI()
        }
    }
    
    private fun toggleHistoryVisibility() {
        val isVisible = binding.cardHistory?.visibility == View.VISIBLE
        binding.cardHistory?.visibility = if (isVisible) View.GONE else View.VISIBLE
    }
    
    
    private fun setupReminderToggle() {
        try {
            val settings = dataManager.getHydrationSettings()
            binding.switchReminders?.isChecked = settings.isEnabled
            
            binding.switchReminders?.setOnCheckedChangeListener { _, isChecked ->
                try {
                    val updatedSettings = settings.copy(isEnabled = isChecked)
                    dataManager.saveHydrationSettings(updatedSettings)
                    
                    if (isChecked) {
                        val notificationHelper = NotificationHelper(requireContext())
                        notificationHelper.scheduleHydrationReminders(updatedSettings)
                    } else {
                        val notificationHelper = NotificationHelper(requireContext())
                        notificationHelper.cancelHydrationReminders()
                    }
                } catch (e: Exception) {
                    binding.switchReminders?.isChecked = !isChecked
                }
            }
            
            binding.buttonReminderMenu?.setOnClickListener {
                showReminderSettingsDialog()
            }
        } catch (e: Exception) {
            binding.switchReminders?.isEnabled = false
        }
    }
    
    private fun showReminderSettingsDialog() {
        val currentSettings = dataManager.getHydrationSettings()
        val dialog = HydrationReminderSettingsDialog.newInstance(currentSettings) { newSettings ->
            dataManager.saveHydrationSettings(newSettings)
            
            if (newSettings.isEnabled) {
                val notificationHelper = NotificationHelper(requireContext())
                notificationHelper.scheduleHydrationReminders(newSettings)
            } else {
                val notificationHelper = NotificationHelper(requireContext())
                notificationHelper.cancelHydrationReminders()
            }
            
            // Update the switch state
            binding.switchReminders?.isChecked = newSettings.isEnabled
        }
        dialog.show(parentFragmentManager, "HydrationReminderSettings")
    }
    
    private fun loadHydrationData() {
        hydrationData = dataManager.getOrCreateHydrationData(today)
        updateUI()
    }
    
    private fun updateUI(animate: Boolean = false) {
        hydrationData?.let { data ->
            // Update circular hydration view
            binding.circularHydrationView?.updateProgress(data.totalIntake, data.targetIntake, animate)
            
            // Update motivational message with remaining amount
            val hydrationLevel = binding.circularHydrationView?.getHydrationLevel()
            val messages = hydrationLevel?.let { motivationalMessages[it] } ?: listOf("Keep hydrating! 💧")
            val randomMessage = messages.random()
            val remainingAmount = binding.circularHydrationView?.getRemainingAmount() ?: 0
            val messageWithRemaining = if (remainingAmount > 0) {
                "$randomMessage\nRemaining: ${remainingAmount}ml"
            } else {
                randomMessage
            }
            binding.textMotivationalMessage?.text = messageWithRemaining
            
            // Update entries
            hydrationAdapter.updateEntries(data.entries.toList())
            
            // Update empty state
            binding.textEmptyHistory.visibility = if (data.entries.isEmpty()) View.VISIBLE else View.GONE
            
            // Update water level animation (keep for compatibility)
            updateWaterLevel(data)
            
            // Update glass progress info
            updateGlassProgressInfo(data)
            
            // Check if goal is reached and show success animation
            val percentage = data.getCompletionPercentage().toInt()
            if (percentage >= 100 && !data.goalReachedToday) {
                showSuccessAnimation()
                // Mark goal as reached for today
                data.goalReachedToday = true
                dataManager.updateHydrationData(data)
            }
        }
    }
    
    /**
     * Updates the glass progress information display.
     */
    private fun updateGlassProgressInfo(data: HydrationData) {
        val glassesConsumed = data.totalIntake / 250 // Each glass = 250ml
        val totalGlasses = 8 // 8 glasses = 2000ml
        val glassesRemaining = totalGlasses - glassesConsumed
        
        // Update glass info text
        binding.textGlassInfo?.text = "250ml per glass\n$glassesConsumed/$totalGlasses glasses\n$glassesRemaining remaining"
    }
    
    private fun updateWaterLevel(data: HydrationData) {
        val waterLevel = (data.getCompletionPercentage() / 100f).coerceIn(0f, 1f)
        binding.waterBackgroundView?.setWaterLevel(waterLevel)
    }
    
    private fun showSuccessAnimation() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("🎉 Congratulations! 🎉")
            .setMessage("You've reached your daily hydration goal of 2000ml!\n\n💧 Great job staying hydrated! 💧\n\nKeep up the excellent work for your health and wellness!")
            .setPositiveButton("Awesome!") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
        
        dialog.show()
        
        // Celebratory visual feedback with bounce animation on circular view
        binding.circularHydrationView?.animate()
            ?.scaleX(1.1f)
            ?.scaleY(1.1f)
            ?.setDuration(200)
            ?.withEndAction {
                binding.circularHydrationView?.animate()
                    ?.scaleX(1.0f)
                    ?.scaleY(1.0f)
                    ?.setDuration(200)
                    ?.withEndAction {
                        // Second bounce for extra celebration
                        binding.circularHydrationView?.animate()
                            ?.scaleX(1.05f)
                            ?.scaleY(1.05f)
                            ?.setDuration(150)
                            ?.withEndAction {
                                binding.circularHydrationView?.animate()
                                    ?.scaleX(1.0f)
                                    ?.scaleY(1.0f)
                                    ?.setDuration(150)
                            }
                    }
            }
        
        // Show a toast as well for extra feedback
        android.widget.Toast.makeText(
            requireContext(),
            "🎉 Goal Reached! 2000ml Complete! 💧",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
    
    
    private fun addHydrationEntry(amount: Int) {
        if (hydrationData == null) {
            hydrationData = dataManager.getOrCreateHydrationData(today)
        }
        
        hydrationData?.let { data ->
            val updatedData = data.addEntry(amount)
            dataManager.updateHydrationData(updatedData)
            hydrationData = updatedData
            updateUI(animate = true) // Show dripping animation when water is added
        }
    }
    
    private fun removeHydrationEntry(entry: HydrationEntry) {
        hydrationData?.let { data ->
            val updatedData = data.removeEntry(entry.id)
            dataManager.updateHydrationData(updatedData)
            hydrationData = updatedData
            updateUI()
        }
    }
    
    /**
     * Tests notification by sending one immediately.
     */
    private fun testNotification() {
        showNotificationSettingsGuide()
    }
    
    private fun showNotificationSettingsGuide() {
        val message = """
            📱 To Enable Popup Notifications:
            
            1. Long-press on any notification from this app
            2. Tap "All categories" or notification settings
            3. Find "Hydration Reminders" channel
            4. Enable "Pop on screen" or "Floating notifications"
            5. Set importance to "High" or "Urgent"
            
            OR
            
            1. Go to Settings → Apps → Wellness Tracker
            2. Tap Notifications
            3. Tap "Hydration Reminders"
            4. Enable "Pop on screen"
            
            Note: Android 14 may suppress popup notifications based on system settings. The notifications ARE working - they appear in your notification shade!
        """.trimIndent()
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🔔 Notification Settings")
            .setMessage(message)
            .setPositiveButton("Send Test Notification") { _, _ ->
                sendTestNotification()
            }
            .setNegativeButton("Open App Settings") { _, _ ->
                openAppNotificationSettings()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
    
    private fun sendTestNotification() {
        try {
            // Check and request notification permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        100
                    )
                    android.widget.Toast.makeText(
                        requireContext(),
                        "📱 Permission dialog should appear. Please allow notifications.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }
            
            // Create notification channel first
            createSimpleNotificationChannel()
            
            // Send test notification
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val notification = androidx.core.app.NotificationCompat.Builder(requireContext(), "test_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon
                .setContentTitle("🧪 Test Notification")
                .setContentText("This is a test notification! Notifications are working 💧")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(longArrayOf(0, 300, 100, 300))
                .build()
            
            notificationManager.notify(999, notification)
            
            // Also show toast
            android.widget.Toast.makeText(
                requireContext(),
                "✅ Test notification sent! Check your notification bar 💧",
                android.widget.Toast.LENGTH_LONG
            ).show()
            
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "❌ Error: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun openAppNotificationSettings() {
        try {
            val intent = android.content.Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            } else {
                intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = android.net.Uri.parse("package:" + requireContext().packageName)
            }
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Could not open settings. Please go to Settings → Apps → Wellness Tracker → Notifications",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Creates a simple notification channel for testing.
     */
    private fun createSimpleNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "test_channel",
                "Test Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Test notification channel"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 100, 300)
                setShowBadge(true)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
            }
            
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
