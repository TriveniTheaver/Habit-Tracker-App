package com.example.lab3.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Central data manager for the Wellness Tracker application.
 * 
 * This class handles all data persistence operations using SharedPreferences.
 * It manages:
 * - Habit tracking data (creation, updates, completion tracking)
 * - Mood journal entries (logging, retrieval, trend analysis)
 * - Hydration data (intake tracking, settings, reminders)
 * - User preferences and settings
 * 
 * The manager uses JSON serialization with Gson to store complex data structures
 * and provides a clean API for data operations across the application.
 * 
 * @param context Android context for accessing SharedPreferences
 */
class WellnessDataManager(private val context: Context) {
    
    // SharedPreferences instance for data persistence
    private val prefs: SharedPreferences = context.getSharedPreferences("wellness_data", Context.MODE_PRIVATE)
    
    // Gson instance for JSON serialization/deserialization
    private val gson = Gson()
    
    companion object {
        // SharedPreferences keys for different data types
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_DATA = "hydration_data"
        private const val KEY_HYDRATION_SETTINGS = "hydration_settings"
        private const val KEY_LAST_SYNC = "last_sync"
    }
    
    // ==================== HABIT MANAGEMENT ====================
    
    /**
     * Saves a list of habits to SharedPreferences.
     * 
     * @param habits List of Habit objects to save
     */
    fun saveHabits(habits: List<Habit>) {
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, habitsJson).apply()
        updateLastSync()
    }
    
    /**
     * Retrieves all saved habits from SharedPreferences.
     * 
     * @return List of Habit objects, empty list if none found or error occurs
     */
    fun getHabits(): List<Habit> {
        val habitsJson = prefs.getString(KEY_HABITS, null)
        return if (habitsJson != null) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                val habits: List<Habit> = gson.fromJson(habitsJson, type) ?: emptyList()
                habits
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Adds a new habit to the existing list.
     * 
     * @param habit The habit to add
     */
    fun addHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        habits.add(habit)
        saveHabits(habits)
    }
    
    /**
     * Updates an existing habit in the list.
     * 
     * @param habit The updated habit object
     */
    fun updateHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
        }
    }
    
    /**
     * Deletes a habit by its ID.
     * 
     * @param habitId The ID of the habit to delete
     */
    fun deleteHabit(habitId: String) {
        val habits = getHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }
    
    // Mood Entry Management
    fun saveMoodEntries(moodEntries: List<MoodEntry>) {
        val entriesJson = gson.toJson(moodEntries)
        prefs.edit().putString(KEY_MOOD_ENTRIES, entriesJson).apply()
        updateLastSync()
    }
    
    fun getMoodEntries(): List<MoodEntry> {
        val entriesJson = prefs.getString(KEY_MOOD_ENTRIES, null)
        return if (entriesJson != null) {
            try {
                val type = object : TypeToken<List<MoodEntry>>() {}.type
                gson.fromJson(entriesJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun addMoodEntry(moodEntry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        entries.add(moodEntry)
        saveMoodEntries(entries)
    }
    
    fun updateMoodEntry(moodEntry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == moodEntry.id }
        if (index != -1) {
            entries[index] = moodEntry
            saveMoodEntries(entries)
        }
    }
    
    fun deleteMoodEntry(entryId: String) {
        val entries = getMoodEntries().toMutableList()
        entries.removeAll { it.id == entryId }
        saveMoodEntries(entries)
    }
    
    // Hydration Data Management
    fun saveHydrationData(hydrationData: HydrationData) {
        val dataJson = gson.toJson(hydrationData)
        prefs.edit().putString(KEY_HYDRATION_DATA, dataJson).apply()
        updateLastSync()
    }
    
    fun getHydrationData(): HydrationData? {
        val dataJson = prefs.getString(KEY_HYDRATION_DATA, null)
        return if (dataJson != null) {
            try {
                gson.fromJson(dataJson, HydrationData::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    fun getOrCreateHydrationData(date: String): HydrationData {
        val existingData = getHydrationData()
        return if (existingData != null && existingData.date == date) {
            existingData
        } else {
            HydrationData(
                id = System.currentTimeMillis().toString(),
                date = date,
                targetIntake = getHydrationSettings().targetIntake
            )
        }
    }
    
    fun updateHydrationData(hydrationData: HydrationData) {
        saveHydrationData(hydrationData)
    }
    
    // Hydration Settings Management
    fun saveHydrationSettings(settings: HydrationSettings) {
        val settingsJson = gson.toJson(settings)
        prefs.edit().putString(KEY_HYDRATION_SETTINGS, settingsJson).apply()
    }
    
    fun getHydrationSettings(): HydrationSettings {
        val settingsJson = prefs.getString(KEY_HYDRATION_SETTINGS, null)
        return if (settingsJson != null) {
            try {
                gson.fromJson(settingsJson, HydrationSettings::class.java)
            } catch (e: Exception) {
                HydrationSettings() // Return default settings
            }
        } else {
            HydrationSettings() // Return default settings
        }
    }
    
    // Utility Methods
    private fun updateLastSync() {
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
    }
    
    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0)
    }
    
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Reset goal reached flag for a new day
     */
    fun resetHydrationGoalReached(date: String) {
        val hydrationData = getOrCreateHydrationData(date)
        val resetData = hydrationData.resetGoalReached()
        updateHydrationData(resetData)
    }
    
    // Helper methods for getting data by date
    fun getHabitsForDate(date: String): List<Habit> {
        return getHabits().filter { it.isActive }
    }
    
    fun getMoodEntriesForDate(date: String): List<MoodEntry> {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return getMoodEntries().filter { 
            formatter.format(it.date) == date 
        }
    }
    
    fun getTodayHabitCompletionPercentage(): Float {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(Date())
        val habits = getHabitsForDate(today)
        
        if (habits.isEmpty()) return 0f
        
        val completedCount = habits.count { it.isCompleted(today) }
        return (completedCount.toFloat() / habits.size * 100)
    }
}
