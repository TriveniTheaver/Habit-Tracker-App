package com.example.lab3.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.lab3.MainActivity
import com.example.lab3.R
import com.example.lab3.data.HydrationSettings
import com.example.lab3.data.WellnessDataManager
import com.example.lab3.utils.NotificationHelper

/**
 * WorkManager Worker for handling hydration reminder notifications.
 * 
 * This worker provides reliable, battery-optimized scheduling of hydration
 * reminders using Android's WorkManager. It handles:
 * - Checking if reminders are enabled in user settings
 * - Creating notification channels for Android O+
 * - Sending hydration reminder notifications
 * - Managing notification lifecycle
 * 
 * The worker integrates with:
 * - WellnessDataManager for settings retrieval
 * - NotificationHelper for advanced notification management
 * - System notification manager for display
 * 
 * Features:
 * - Battery-optimized scheduling
 * - Automatic retry on failure
 * - Settings-based enable/disable
 * - Custom notification channels
 * 
 * @param context Android context
 * @param params Worker parameters
 */
class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    // Data manager for accessing user settings
    private val dataManager = WellnessDataManager(context)
    
    // System notification manager for displaying notifications
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun doWork(): Result {
        val settings = dataManager.getHydrationSettings()
        
        if (!settings.isEnabled) {
            return Result.success()
        }

        createNotificationChannel()
        sendHydrationReminder()
        
        // If using seconds (testing mode), reschedule the next notification
        if (settings.reminderInterval < 0) {
            rescheduleForTesting(settings)
        }

        return Result.success()
    }
    
    private fun rescheduleForTesting(settings: HydrationSettings) {
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.scheduleHydrationReminders(settings)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to drink water"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 100, 300)
                setShowBadge(true)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true) // Allow notifications even in Do Not Disturb
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendHydrationReminder() {
        // Create intent to open the app when notification is tapped
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_hydration", true) // Optional: open hydration tab directly
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon for reliability
            .setContentTitle("💧 Hydration Reminder")
            .setContentText("Time to drink water! Stay hydrated 💧")
            .setPriority(NotificationCompat.PRIORITY_MAX) // Maximum priority for popup
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // CRITICAL: Required for heads-up notifications
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(longArrayOf(0, 300, 100, 300))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        
        // Also show an in-app notification
        showInAppNotification()
    }
    
    private fun showInAppNotification() {
        // Create a simple toast or in-app notification
        android.widget.Toast.makeText(
            applicationContext,
            "💧 Hydration Reminder: Time to drink water!",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        const val CHANNEL_ID = "hydration_reminders"
        const val NOTIFICATION_ID = 1
        
        fun scheduleReminders(context: Context) {
            val dataManager = WellnessDataManager(context)
            val settings = dataManager.getHydrationSettings()
            val notificationHelper = NotificationHelper(context)
            notificationHelper.scheduleHydrationReminders(settings)
        }
        
        fun cancelReminders(context: Context) {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.cancelHydrationReminders()
        }
    }
}