package com.example.lab3.receivers

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.util.*

class HydrationActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "DRINK_WATER" -> {
                // User confirmed they drank water
                dismissNotification(context)
                showToast(context, "Great! Keep staying hydrated! 💧")
            }
            "SNOOZE_REMINDER" -> {
                // Snooze for 30 minutes
                dismissNotification(context)
                scheduleSnooze(context)
                showToast(context, "Reminder snoozed for 30 minutes")
            }
        }
    }
    
    private fun dismissNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(HydrationReminderReceiver.NOTIFICATION_ID)
    }
    
    private fun scheduleSnooze(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val snoozeIntent = Intent(context, HydrationReminderReceiver::class.java).apply {
            putExtra("custom_message", "Time to hydrate! 💧 (Snoozed)")
            putExtra("sound_enabled", true)
            putExtra("vibration_enabled", true)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1002,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeTime = System.currentTimeMillis() + (30 * 60 * 1000L) // 30 minutes
        
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
    }
    
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
