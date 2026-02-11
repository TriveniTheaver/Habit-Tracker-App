package com.example.lab3.data

import java.io.Serializable
import java.util.Date

/**
 * Data class representing a mood journal entry in the wellness tracking system.
 * 
 * A mood entry captures the user's emotional state at a specific moment in time.
 * It includes:
 * - Visual representation (emoji)
 * - Mood level (terrible to great scale)
 * - Optional notes and tags
 * - Timestamp information
 * 
 * Mood entries are used for:
 * - Daily mood tracking and journaling
 * - Mood trend analysis over time
 * - Wellness insights and patterns
 * - Emotional state awareness
 * 
 * @param id Unique identifier for the mood entry
 * @param emoji Emoji representing the mood (😊, 😢, etc.)
 * @param moodLevel Categorized mood level from terrible to great
 * @param note Optional text note about the mood
 * @param tags List of tags for categorization (work, family, health, etc.)
 * @param date When the mood was recorded
 * @param time Time string for display purposes
 */
data class MoodEntry(
    val id: String = "",
    val emoji: String = "",
    val moodLevel: MoodLevel = MoodLevel.NEUTRAL,
    val note: String = "",
    val tags: List<String> = emptyList(),
    val date: Date = Date(),
    val time: String = ""
) : Serializable {
    /**
     * Gets the color associated with the mood level for UI display.
     * 
     * @return Hex color string (e.g., "#4ECDC4" for good mood)
     */
    fun getMoodColor(): String {
        return moodLevel.color
    }
    
    /**
     * Gets a formatted date string for display purposes.
     * 
     * @return Date in format "MMM dd, yyyy" (e.g., "Dec 15, 2023")
     */
    fun getFormattedDate(): String {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Gets a formatted time string for display purposes.
     * 
     * @return Time in format "h:mm a" (e.g., "2:30 PM")
     */
    fun getFormattedTime(): String {
        val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}

/**
 * Enum representing different mood levels on a 5-point scale.
 * 
 * Each mood level has a display name and associated color for UI representation.
 * The scale ranges from terrible (0) to great (4) for trend analysis.
 */
enum class MoodLevel(val displayName: String, val color: String) {
    /** Lowest mood level - very negative emotional state */
    TERRIBLE("Terrible", "#FA5252"),
    
    /** Low mood level - negative emotional state */
    BAD("Bad", "#FF922B"),
    
    /** Neutral mood level - neither positive nor negative */
    NEUTRAL("Neutral", "#FFE66D"),
    
    /** High mood level - positive emotional state */
    GOOD("Good", "#4ECDC4"),
    
    /** Highest mood level - very positive emotional state */
    GREAT("Great", "#51CF66")
}

/**
 * Object containing available mood emojis for user selection.
 * 
 * Provides a comprehensive set of emojis representing various emotional states
 * for users to choose from when logging their mood. The emojis are organized
 * to cover the full spectrum of human emotions.
 */
object MoodEmojis {
    /** List of emoji strings available for mood selection */
    val EMOJIS = listOf(
        // Sad/negative emotions
        "😢", "😔", "😐", "🙂", "😊", "😄", "🤗", "😌", "😴", "😵",
        // Confused/uncertain emotions
        "🤔", "😕", "😟", "😮", "😯", "😲", "😳", "😱", "😨", "😰",
        // More negative emotions
        "😥", "😢", "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥺",
        // Happy/positive emotions
        "😌", "😊", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂",
        // Playful/cheerful emotions
        "🙃", "😉", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😙",
        // Fun/silly emotions
        "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
        // Neutral/thinking emotions
        "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
        // Tired/sleepy emotions
        "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧",
        // Physical state emotions
        "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "😎", "🤓", "🧐"
    )
}
