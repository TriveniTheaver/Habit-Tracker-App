package com.example.lab3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.R
import com.example.lab3.data.Habit
import com.example.lab3.databinding.ItemHabitBinding
import com.google.android.material.checkbox.MaterialCheckBox
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter for displaying habit items in the habits list.
 * 
 * This adapter handles the display and interaction of habit items, including:
 * - Habit information display (name, description, category, targets)
 * - Completion status toggle for today
 * - Weekly progress visualization
 * - Edit and delete actions
 * - Category-based color coding
 * 
 * The adapter provides callbacks for:
 * - Habit completion toggling
 * - Habit editing
 * - Habit deletion
 * 
 * @param habits List of habits to display
 * @param onHabitToggled Callback when habit completion is toggled
 * @param onHabitEdit Callback when habit edit is requested
 * @param onHabitDelete Callback when habit deletion is requested
 */
class HabitsAdapter(
    private val habits: List<Habit>,
    private val onHabitToggled: (Habit, Boolean) -> Unit,
    private val onHabitEdit: (Habit) -> Unit,
    private val onHabitDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    // Today's date for completion tracking
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // No longer need weekly progress calculation

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.apply {
                println("DEBUG: HabitsAdapter.bind() - Binding habit: ${habit.name}, completedDates: ${habit.completedDates}")
                println("DEBUG: HabitsAdapter.bind() - Today: $today, isCompleted: ${habit.isCompleted(today)}")
                
                textHabitEmoji.text = habit.category.emoji
                textHabitName.text = habit.name
                textHabitDescription.text = "${habit.targetCount} ${habit.unit}"
                textHabitCategory.text = habit.category.displayName
                
                // Set category color
                val categoryColor = habit.category.color
                textHabitCategory.setBackgroundColor(
                    android.graphics.Color.parseColor(categoryColor)
                )
                
                // Calculate today's progress
                val isCompletedToday = habit.isCompleted(today)
                val todayProgress = if (isCompletedToday) 1.0f else 0.0f
                progressBarHabit.progress = (todayProgress * 100).toInt()
                textHabitProgress.text = if (isCompletedToday) "Completed today" else "Not completed today"
                
                println("DEBUG: HabitsAdapter.bind() - Setting checkbox to: $isCompletedToday")
                
                // Set completion checkbox listener BEFORE setting checked state to avoid triggering it
                checkboxCompleted.setOnCheckedChangeListener(null) // Clear previous listener
                checkboxCompleted.isChecked = isCompletedToday
                checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                    println("DEBUG: HabitsAdapter - Checkbox clicked for habit: ${habit.name}, ID: ${habit.id}, isChecked: $isChecked")
                    println("DEBUG: HabitsAdapter - Habit completedDates before: ${habit.completedDates}")
                    onHabitToggled(habit, isChecked)
                }
                
                println("DEBUG: HabitsAdapter.bind() - Checkbox is now: ${checkboxCompleted.isChecked}")
                
                // Set edit button listener
                buttonEditHabit.setOnClickListener {
                    onHabitEdit(habit)
                }
                
                // Set delete button listener
                buttonDeleteHabit.setOnClickListener {
                    onHabitDelete(habit)
                }
            }
        }
    }

    // Weekly progress calculation methods removed - now using daily progress only
}
