package com.example.lab3.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.data.PredefinedMood
import com.example.lab3.databinding.ItemMoodOptionBinding

/**
 * RecyclerView adapter for displaying predefined mood options in the Add Mood dialog.
 * Each mood option shows an emoji, name, and can be selected by the user.
 * 
 * @param moods List of predefined mood options to display
 * @param onMoodSelected Callback invoked when a mood is selected
 */
class MoodOptionAdapter(
    private val moods: List<PredefinedMood>,
    private val onMoodSelected: (PredefinedMood) -> Unit
) : RecyclerView.Adapter<MoodOptionAdapter.MoodOptionViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodOptionViewHolder {
        val binding = ItemMoodOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodOptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodOptionViewHolder, position: Int) {
        holder.bind(moods[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = moods.size

    inner class MoodOptionViewHolder(
        private val binding: ItemMoodOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: PredefinedMood, isSelected: Boolean) {
            binding.textMoodEmoji.text = mood.emoji
            binding.textMoodName.text = mood.name
            binding.textMoodDescription.text = mood.description
            
            // Update selection state
            if (isSelected) {
                binding.cardMoodOption.strokeWidth = 4
                binding.cardMoodOption.strokeColor = Color.parseColor(mood.level.color)
                binding.cardMoodOption.setCardBackgroundColor(
                    Color.parseColor(mood.level.color + "20") // Add alpha for light background
                )
            } else {
                binding.cardMoodOption.strokeWidth = 2
                binding.cardMoodOption.strokeColor = Color.parseColor("#E0E0E0")
                binding.cardMoodOption.setCardBackgroundColor(Color.WHITE)
            }
            
            // Handle click
            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                
                // Notify both positions to update their UI
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition)
                }
                notifyItemChanged(selectedPosition)
                
                // Invoke callback
                onMoodSelected(mood)
            }
        }
    }
}
