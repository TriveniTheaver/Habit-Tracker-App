package com.example.lab3.data

import java.util.Date

/**
 * Data class representing hydration tracking data
 */
data class HydrationData(
    val id: String = "",
    val date: String = "",
    val totalIntake: Int = 0, // in milliliters
    val targetIntake: Int = 2000, // default 2L
    val entries: MutableList<HydrationEntry> = mutableListOf(),
    var goalReachedToday: Boolean = false // Track if goal was reached today
) {
    /**
     * Get completion percentage
     */
    fun getCompletionPercentage(): Float {
        return if (targetIntake > 0) {
            (totalIntake.toFloat() / targetIntake * 100).coerceAtMost(100f)
        } else 0f
    }
    
    /**
     * Add a hydration entry
     */
    fun addEntry(amount: Int, time: Date = Date()): HydrationData {
        val entry = HydrationEntry(
            id = System.currentTimeMillis().toString(),
            amount = amount,
            time = time
        )
        val newEntries = entries.toMutableList()
        newEntries.add(entry)
        return copy(
            totalIntake = totalIntake + amount,
            entries = newEntries
        )
    }
    
    /**
     * Remove a hydration entry
     */
    fun removeEntry(entryId: String): HydrationData {
        val entry = entries.find { it.id == entryId }
        return if (entry != null) {
            val newEntries = entries.toMutableList()
            newEntries.remove(entry)
            copy(
                totalIntake = totalIntake - entry.amount,
                entries = newEntries
            )
        } else {
            this
        }
    }
    
    /**
     * Get remaining intake needed
     */
    fun getRemainingIntake(): Int {
        return (targetIntake - totalIntake).coerceAtLeast(0)
    }
    
    /**
     * Reset goal reached flag for new day
     */
    fun resetGoalReached(): HydrationData {
        return copy(goalReachedToday = false)
    }
}

/**
 * Data class representing a single hydration entry
 */
data class HydrationEntry(
    val id: String = "",
    val amount: Int = 0, // in milliliters
    val time: Date = Date()
) {
    /**
     * Get formatted time string
     */
    fun getFormattedTime(): String {
        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        return formatter.format(time)
    }
    
    /**
     * Get formatted amount string
     */
    fun getFormattedAmount(): String {
        return if (amount >= 1000) {
            "${amount / 1000f}L"
        } else {
            "${amount}ml"
        }
    }
}

/**
 * Hydration reminder settings
 */
data class HydrationSettings(
    val isEnabled: Boolean = true,
    val reminderInterval: Int = 2, // hours
    val startTime: String = "08:00",
    val endTime: String = "22:00",
    val targetIntake: Int = 2000, // milliliters
    val quickAddAmounts: List<Int> = listOf(250, 500, 750), // milliliters
    val reminderTypes: List<String> = listOf("notification", "alarm"), // notification types
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val customMessage: String = "Time to hydrate! 💧"
) : java.io.Serializable
