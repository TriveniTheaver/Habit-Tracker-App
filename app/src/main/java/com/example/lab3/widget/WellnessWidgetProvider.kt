package com.example.lab3.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.lab3.MainActivity
import com.example.lab3.R
import com.example.lab3.data.WellnessDataManager
import java.text.SimpleDateFormat
import java.util.*

class WellnessWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val dataManager = WellnessDataManager(context)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Calculate today's progress
            val habits = dataManager.getHabitsForDate(today)
            val completedHabits = habits.count { it.isCompleted(today) }
            val totalHabits = habits.size
            val progressPercentage = if (totalHabits > 0) {
                (completedHabits.toFloat() / totalHabits * 100).toInt()
            } else {
                0
            }

            // Create RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_wellness)

            // Update text
            views.setTextViewText(R.id.text_widget_progress, "$progressPercentage%")
            views.setTextViewText(R.id.text_widget_habits, "$completedHabits/$totalHabits")
            
            // Update progress bar
            views.setProgressBar(R.id.progress_bar_widget, 100, progressPercentage, false)

            // Set click intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}