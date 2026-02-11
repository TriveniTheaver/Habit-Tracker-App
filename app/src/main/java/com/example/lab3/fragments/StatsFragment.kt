package com.example.lab3.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lab3.R
import com.example.lab3.data.Habit
import com.example.lab3.data.HydrationData
import com.example.lab3.data.MoodEntry
import com.example.lab3.data.MoodLevel
import com.example.lab3.data.WellnessDataManager
import com.example.lab3.databinding.FragmentStatsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class StatsFragment : Fragment() {
    
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: WellnessDataManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dataManager = WellnessDataManager(requireContext())
        setupMoodChart()
        setupHabitStats()
        setupHydrationStats()
    }
    
    private fun setupMoodChart() {
        val moodEntries = dataManager.getMoodEntries()
        val last7Days = getLast7Days()
        
        // Create mood trend data for the last 7 days
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        for (i in last7Days.indices) {
            val date = last7Days[i]
            val dayMoods = moodEntries.filter { 
                val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date)
                entryDate == date 
            }
            
            // Get the latest mood for this day, or use neutral if no mood
            val moodValue = if (dayMoods.isNotEmpty()) {
                val latestMood = dayMoods.maxByOrNull { it.date }
                latestMood?.moodLevel?.ordinal?.toFloat() ?: 2f // Neutral
            } else {
                2f // Neutral - no mood entry
            }
            
            entries.add(Entry(i.toFloat(), moodValue))
            
            // Create day labels (Mon, Tue, etc.)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -6 + i)
            val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            labels.add(dayLabel)
        }
        
        // Create line dataset
        val dataSet = LineDataSet(entries, "Mood Level").apply {
            color = Color.parseColor("#4ECDC4")
            setCircleColor(Color.parseColor("#4ECDC4"))
            lineWidth = 3f
            circleRadius = 6f
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = Color.parseColor("#804ECDC4")
            fillAlpha = 100
        }
        
        val lineData = LineData(dataSet)
        binding.chartMoodTrend.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            
            // Configure X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() < labels.size) {
                            labels[value.toInt()]
                        } else ""
                    }
                }
            }
            
            // Configure Y-axis
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawAxisLine(true)
                granularity = 1f
                axisMinimum = 0f
                axisMaximum = 4f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            0 -> "Terrible"
                            1 -> "Bad"
                            2 -> "Neutral"
                            3 -> "Good"
                            4 -> "Great"
                            else -> ""
                        }
                    }
                }
            }
            
            axisRight.isEnabled = false
            animateX(1000)
        }
        
        binding.chartMoodTrend.visibility = View.VISIBLE
    }
    
    private fun setupHabitStats() {
        val habits = dataManager.getHabits()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val completedHabits = habits.count { it.isCompleted(today) }
        val totalHabits = habits.size
        
        binding.textHabitStats.text = "$completedHabits of $totalHabits habits completed today"
        
        val completionPercentage = if (totalHabits > 0) {
            (completedHabits.toFloat() / totalHabits * 100).toInt()
        } else {
            0
        }
        
        binding.progressBarHabitStats.progress = completionPercentage
        binding.textHabitPercentage.text = "$completionPercentage%"
    }
    
    private fun setupHydrationStats() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hydrationData = dataManager.getOrCreateHydrationData(today)
        val settings = dataManager.getHydrationSettings()
        
        val percentage = hydrationData.getCompletionPercentage().toInt()
        binding.textHydrationStats.text = "${hydrationData.totalIntake}ml / ${settings.targetIntake}ml"
        binding.progressBarHydrationStats.progress = percentage
        binding.textHydrationPercentage.text = "$percentage%"
        
        // Add share button functionality
        setupShareButton()
    }
    
    /**
     * Sets up the share button to allow users to share their wellness summary.
     * This implements the implicit intent requirement for sharing mood summary.
     */
    private fun setupShareButton() {
        binding.buttonShareSummary.setOnClickListener {
            shareWellnessSummary()
        }
    }
    
    /**
     * Shares the user's wellness summary using implicit intent.
     * This fulfills the requirement for implicit intents for sharing (e.g., share mood summary).
     */
    private fun shareWellnessSummary() {
        val moodEntries = dataManager.getMoodEntries()
        val habits = dataManager.getHabits()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hydrationData = dataManager.getOrCreateHydrationData(today)
        
        // Generate wellness summary
        val summary = generateWellnessSummary(moodEntries, habits, hydrationData)
        
        // Create implicit intent for sharing
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
            putExtra(Intent.EXTRA_SUBJECT, "My Wellness Summary")
        }
        
        // Start activity with chooser
        startActivity(Intent.createChooser(shareIntent, "Share Wellness Summary"))
    }
    
    /**
     * Generates a comprehensive wellness summary for sharing.
     * 
     * @param moodEntries List of mood entries for analysis
     * @param habits List of habits for progress tracking
     * @param hydrationData Current hydration data
     * @return Formatted wellness summary string
     */
    private fun generateWellnessSummary(
        moodEntries: List<MoodEntry>,
        habits: List<Habit>,
        hydrationData: HydrationData
    ): String {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val completedHabits = habits.count { it.isCompleted(today) }
        val totalHabits = habits.size
        val habitPercentage = if (totalHabits > 0) (completedHabits.toFloat() / totalHabits * 100).toInt() else 0
        
        // Get latest mood
        val latestMood = moodEntries.maxByOrNull { it.date }
        val moodText = latestMood?.let { "${it.emoji} ${it.moodLevel.displayName}" } ?: "No mood logged today"
        
        // Get hydration progress
        val hydrationPercentage = hydrationData.getCompletionPercentage().toInt()
        
        return """
        🌟 My Wellness Summary 🌟
        
        📅 Today's Progress:
        • Habits: $completedHabits/$totalHabits completed ($habitPercentage%)
        • Mood: $moodText
        • Hydration: ${hydrationData.totalIntake}ml (${hydrationData.targetIntake}ml) - $hydrationPercentage%
        
        💪 Keep up the great work on your wellness journey!
        
        #WellnessTracker #HealthyLiving
        """.trimIndent()
    }
    
    private fun getLast7Days(): List<String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        return (0..6).map { daysAgo ->
            calendar.add(Calendar.DAY_OF_MONTH, -daysAgo)
            dateFormat.format(calendar.time)
        }.reversed()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
