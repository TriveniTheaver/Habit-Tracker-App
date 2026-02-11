package com.example.lab3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.data.Habit
import com.example.lab3.databinding.ItemHabitProgressBinding
import java.text.SimpleDateFormat
import java.util.*

class HabitProgressAdapter(
    private val habits: List<Habit>,
    private val onHabitToggled: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitProgressAdapter.HabitProgressViewHolder>() {

    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitProgressViewHolder {
        val binding = ItemHabitProgressBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitProgressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitProgressViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitProgressViewHolder(
        private val binding: ItemHabitProgressBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.apply {
                // Set habit details
                textHabitIcon.text = habit.category.emoji
                textHabitName.text = habit.name
                textProgressTarget.text = habit.targetCount.toString()
                textProgressUnit.text = habit.unit
                
                // Set completion state
                checkboxCompleted.isChecked = habit.isCompleted(today)
                
                // Calculate progress
                val currentProgress = if (habit.isCompleted(today)) habit.targetCount else 0
                val progressPercentage = (currentProgress.toFloat() / habit.targetCount.toFloat() * 100).toInt()
                
                textProgressCurrent.text = currentProgress.toString()
                progressBarHabit.progress = progressPercentage
                
                // Set completion checkbox listener
                checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                    onHabitToggled(habit, isChecked)
                }
            }
        }
    }
}
