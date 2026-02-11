package com.example.lab3.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.lab3.R

/**
 * BroadcastReceiver for handling hydration reminder notifications.
 * 
 * This receiver is triggered by scheduled alarms to send hydration reminders
 * to users throughout the day. It handles:
 * - Creating and displaying hydration reminder notifications
 * - Providing quick action buttons (Drink Water, Snooze)
 * - Customizing notification content based on user settings
 * - Managing notification channels for Android O+
 * 
 * The receiver integrates with:
 * - WorkManager for reliable scheduling
 * - HydrationActionReceiver for handling user actions
 * - User settings for customization
 * 
 * Notification features:
 * - Custom hydration reminder message
 * - Action buttons for user interaction
 * - Sound and vibration based on settings
 * - Persistent notification for ongoing reminders
 */
class HydrationReminderReceiver : BroadcastReceiver() {
    
    /**
     * Called when the receiver receives a broadcast intent.
     * 
     * @param context The context in which the receiver is running
     * @param intent The intent being received
     */
    override fun onReceive(context: Context, intent: Intent) {
        val customMessage = intent.getStringExtra("custom_message") ?: "Time to hydrate! 💧"
        val soundEnabled = intent.getBooleanExtra("sound_enabled", true)
        val vibrationEnabled = intent.getBooleanExtra("vibration_enabled", true)
        
        createNotificationChannel(context)
        sendNotification(context, customMessage, soundEnabled, vibrationEnabled)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to drink water at regular intervals"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun sendNotification(
        context: Context,
        message: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("💧 Hydration Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        
        // Add sound and vibration based on settings
        if (soundEnabled) {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        }
        
        if (vibrationEnabled) {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        }
        
        // Add action buttons
        val drinkIntent = Intent(context, HydrationActionReceiver::class.java).apply {
            action = "DRINK_WATER"
        }
        val drinkPendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            2001,
            drinkIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeIntent = Intent(context, HydrationActionReceiver::class.java).apply {
            action = "SNOOZE_REMINDER"
        }
        val snoozePendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            2002,
            snoozeIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        notificationBuilder
            .addAction(
                R.drawable.ic_water_drop,
                "I Drank Water",
                drinkPendingIntent
            )
            .addAction(
                R.drawable.ic_more_vert,
                "Snooze 30min",
                snoozePendingIntent
            )
        
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    
    companion object {
        const val CHANNEL_ID = "hydration_reminders"
        const val NOTIFICATION_ID = 1001
    }
}
