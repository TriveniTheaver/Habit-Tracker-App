package com.example.lab3.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab3.R
import com.example.lab3.adapters.HabitProgressAdapter
import com.example.lab3.data.Habit
import com.example.lab3.data.MoodEntry
import com.example.lab3.data.WellnessDataManager
import com.example.lab3.databinding.FragmentHomeBinding
import com.example.lab3.databinding.ItemHabitProgressBinding
import com.example.lab3.dialogs.AddHabitDialog
import com.example.lab3.dialogs.AddMoodDialog
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Fragment - Main dashboard of the Wellness Tracker application.
 * 
 * This fragment serves as the central hub where users can:
 * - View their daily wellness progress overview
 * - See habit completion status and progress
 * - Check hydration intake for the day
 * - View recent mood entries
 * - Quick access to add new habits or log mood
 * - Access theme settings and app information
 * 
 * The fragment displays a comprehensive dashboard with:
 * - Overall habit completion percentage
 * - Individual habit progress with completion toggles
 * - Hydration progress with quick intake buttons
 * - Latest mood entry with emoji and timestamp
 * - Navigation to detailed sections
 */
class HomeFragment : Fragment() {
    
    // View binding for the home fragment layout
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    // Data manager for accessing wellness data
    private lateinit var dataManager: WellnessDataManager
    
    // Adapter for displaying habit progress in RecyclerView
    private lateinit var habitProgressAdapter: HabitProgressAdapter
    
    // Today's date in YYYY-MM-DD format for data operations
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    /**
     * Creates the view for the home fragment.
     * 
     * @param inflater Layout inflater for creating views
     * @param container Parent view group
     * @param savedInstanceState Saved instance state
     * @return The root view of the fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    /**
     * Called after the view is created. Sets up the fragment's UI components
     * and initializes data loading.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize data manager for accessing wellness data
        dataManager = WellnessDataManager(requireContext())
        
        // Set up UI components
        setupClickListeners()
        loadDashboardData()
    }
    
    private fun setupClickListeners() {
        binding.buttonAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
        
        binding.buttonLogMood.setOnClickListener {
            showAddMoodDialog()
        }
        
        binding.buttonMenu.setOnClickListener {
            showMenu()
        }
        
    }
    
    
    private fun loadDashboardData() {
        updateHabitsProgress()
        updateHydrationProgress()
        updateMoodSummary()
    }
    
    private fun updateHabitsProgress() {
        val habits = dataManager.getHabitsForDate(today)
        val completedHabits = habits.count { it.isCompleted(today) }
        val totalHabits = habits.size
        
        binding.textHabitsProgress?.text = "$completedHabits/$totalHabits"
        
        val overallProgress = if (totalHabits > 0) {
            (completedHabits.toFloat() / totalHabits * 100).toInt()
        } else {
            0
        }
        
        binding.textOverallProgress?.text = "$overallProgress%"
        binding.progressBarOverall?.progress = overallProgress
        
        // Update wellness icon based on progress
        updateWellnessIcon(overallProgress)
    }
    
    /**
     * Updates the wellness icon based on user's daily progress.
     * This provides visual feedback and motivation to users.
     * 
     * @param progress Percentage of habits completed (0-100)
     */
    private fun updateWellnessIcon(progress: Int) {
        val iconText = when {
            progress >= 100 -> "🎉" // All habits completed - celebration
            progress >= 75 -> "🌟"  // Great progress - star
            progress >= 50 -> "💪"  // Good progress - motivation
            progress >= 25 -> "🌱"  // Some progress - growth
            progress > 0 -> "🌅"    // Just started - new beginning
            else -> "💫"            // No progress yet - sparkle of potential
        }
        // App logo is now static in layout
    }
    
    private fun updateHydrationProgress() {
        val hydrationData = dataManager.getOrCreateHydrationData(today)
        val settings = dataManager.getHydrationSettings()
        
        binding.textHydrationProgress?.text = "${hydrationData.totalIntake}ml"
    }
    
    private fun updateMoodSummary() {
        val moodEntries = dataManager.getMoodEntries()
        val todayMoods = moodEntries.filter { 
            it.getFormattedDate() == SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        }
        
        if (todayMoods.isNotEmpty()) {
            val latestMood = todayMoods.maxByOrNull { it.date }
            latestMood?.let { mood ->
                binding.textRecentMoodEmoji?.text = mood.emoji
                binding.textRecentMoodTime?.text = "${mood.moodLevel.displayName} • ${mood.getFormattedTime()}"
                
                // Add click listener for mood sharing
                binding.textRecentMoodEmoji?.setOnClickListener {
                    shareMoodEntry(mood)
                }
                binding.textRecentMoodTime?.setOnClickListener {
                    shareMoodEntry(mood)
                }
            }
        } else {
            binding.textRecentMoodEmoji?.text = "😊"
            binding.textRecentMoodTime?.text = "No mood entries today"
        }
    }
    
    /**
     * Shares a specific mood entry using implicit intent.
     * This fulfills the requirement for implicit intents for sharing mood summary.
     */
    private fun shareMoodEntry(moodEntry: MoodEntry) {
        val moodSummary = """
        ${moodEntry.emoji} My mood today: ${moodEntry.moodLevel.displayName}
        
        ${if (moodEntry.note.isNotEmpty()) "Note: ${moodEntry.note}" else ""}
        
        Time: ${moodEntry.getFormattedTime()}
        
        #WellnessTracker #MoodJournal
        """.trimIndent()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, moodSummary)
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Today")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share Mood"))
    }
    
    private fun showAddHabitDialog() {
        val dialog = AddHabitDialog { habit ->
            dataManager.addHabit(habit)
            loadDashboardData()
        }
        dialog.show(parentFragmentManager, "AddHabitDialog")
    }
    
    private fun showAddMoodDialog() {
        val dialog = AddMoodDialog { moodEntry ->
            dataManager.addMoodEntry(moodEntry)
            loadDashboardData()
        }
        dialog.show(parentFragmentManager, "AddMoodDialog")
    }
    
    private fun showMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.buttonMenu)
        popupMenu.menuInflater.inflate(R.menu.home_menu, popupMenu.menu)
        
        // Set current theme state
        val sharedPreferences = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        popupMenu.menu.findItem(R.id.menu_theme_toggle).title = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode"
        
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_theme_toggle -> {
                    toggleTheme()
                    true
                }
                R.id.menu_about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }
    
    private fun toggleTheme() {
        val sharedPreferences = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        val newMode = !isDarkMode
        
        sharedPreferences.edit().putBoolean("dark_mode", newMode).apply()
        
        if (newMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun showAboutDialog() {
        // Simple about dialog - you can enhance this later
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("About Wellness Tracker")
            .setMessage("Track your daily wellness journey with habits, mood, and hydration tracking.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
