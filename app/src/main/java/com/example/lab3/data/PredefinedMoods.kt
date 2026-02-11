package com.example.lab3.data

/**
 * Data class representing a predefined mood option in the mood journal.
 * Each mood combines an emoji, mood level, and descriptive name for easy selection.
 * 
 * @param emoji The emoji representing this mood (e.g., 😊, 😢, 😰)
 * @param level The MoodLevel associated with this mood
 * @param name The descriptive name of this mood (e.g., "Happy", "Sad", "Anxious")
 * @param description Optional detailed description of the mood
 */
data class PredefinedMood(
    val emoji: String,
    val level: MoodLevel,
    val name: String,
    val description: String = ""
)

/**
 * Object containing the predefined mood options available for selection.
 * These moods cover a range of emotional states commonly experienced by users.
 */
object PredefinedMoods {
    val MOODS = listOf(
        // Great mood
        PredefinedMood(
            emoji = "😊",
            level = MoodLevel.GREAT,
            name = "Great",
            description = "Feeling wonderful and positive"
        ),
        
        // Good mood
        PredefinedMood(
            emoji = "😌",
            level = MoodLevel.GOOD,
            name = "Good",
            description = "Feeling content and relaxed"
        ),
        
        // Neutral mood
        PredefinedMood(
            emoji = "😐",
            level = MoodLevel.NEUTRAL,
            name = "Neutral",
            description = "Neither good nor bad"
        ),
        
        // Bad mood
        PredefinedMood(
            emoji = "😢",
            level = MoodLevel.BAD,
            name = "Bad",
            description = "Feeling down or unhappy"
        ),
        
        // Terrible mood
        PredefinedMood(
            emoji = "😡",
            level = MoodLevel.TERRIBLE,
            name = "Terrible",
            description = "Feeling very low or upset"
        )
    )
}
