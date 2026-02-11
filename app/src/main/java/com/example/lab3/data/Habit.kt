package com.example.lab3.data

import java.util.Date

/**
 * Data class representing a wellness habit in the tracking system.
 * 
 * A habit represents a daily wellness activity that users want to track and complete.
 * Each habit has:
 * - Basic information (name, description, category)
 * - Target metrics (count and unit)
 * - Completion tracking (dates when completed)
 * - Status management (active/inactive)
 * 
 * The habit system supports different categories like fitness, nutrition, mental health,
 * sleep, social activities, and hydration tracking.
 * 
 * @param id Unique identifier for the habit
 * @param name Display name of the habit
 * @param description Optional description of the habit
 * @param category Wellness category this habit belongs to
 * @param targetCount How many times/units to complete per day
 * @param unit Unit of measurement (times, minutes, glasses, etc.)
 * @param isActive Whether the habit is currently being tracked
 * @param createdAt When the habit was created
 * @param completedDates Set of dates (YYYY-MM-DD format) when habit was completed
 */
data class Habit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: HabitCategory = HabitCategory.FITNESS,
    val targetCount: Int = 1,
    val unit: String = "times",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val completedDates: MutableSet<String> = mutableSetOf()
) {
    /**
     * Calculates the completion percentage for a specific date.
     * 
     * @param date Date string in YYYY-MM-DD format
     * @return 100.0f if completed, 0.0f if not completed
     */
    fun getCompletionPercentage(date: String): Float {
        return if (completedDates.contains(date)) 100f else 0f
    }
    
    /**
     * Marks the habit as completed for a specific date.
     * Adds the date to the completed dates set.
     * 
     * @param date Date string in YYYY-MM-DD format
     */
    fun markCompleted(date: String) {
        completedDates.add(date)
    }
    
    /**
     * Marks the habit as not completed for a specific date.
     * Removes the date from the completed dates set.
     * 
     * @param date Date string in YYYY-MM-DD format
     */
    fun markIncomplete(date: String) {
        completedDates.remove(date)
    }
    
    /**
     * Checks if the habit was completed on a specific date.
     * 
     * @param date Date string in YYYY-MM-DD format
     * @return true if completed on that date, false otherwise
     */
    fun isCompleted(date: String): Boolean {
        return completedDates.contains(date)
    }
}

/**
 * Enum representing different wellness habit categories.
 * 
 * Each category has a display name, emoji icon, and color for UI representation.
 * Categories help users organize their habits by wellness domain.
 */
enum class HabitCategory(val displayName: String, val emoji: String, val color: String) {
    /** Physical fitness and exercise activities */
    FITNESS("Fitness", "💪", "#FF6B6B"),
    
    /** Nutrition and dietary habits */
    NUTRITION("Nutrition", "🥗", "#51CF66"),
    
    /** Mental health and mindfulness activities */
    MENTAL("Mental", "🧠", "#4ECDC4"),
    
    /** Sleep and rest-related habits */
    SLEEP("Sleep", "😴", "#845EF7"),
    
    /** Social activities and relationships */
    SOCIAL("Social", "👥", "#FFE66D"),
    
    /** Hydration and water intake tracking */
    HYDRATION("Hydration", "💧", "#339AF0")
}
