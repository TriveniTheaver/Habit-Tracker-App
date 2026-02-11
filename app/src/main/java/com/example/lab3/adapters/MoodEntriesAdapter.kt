package com.example.lab3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.data.MoodEntry
import com.example.lab3.databinding.ItemMoodEntryBinding

class MoodEntriesAdapter(
    private val moodEntries: MutableList<MoodEntry>,
    private val onMoodEntryClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodEntriesAdapter.MoodEntryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val binding = ItemMoodEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size

    inner class MoodEntryViewHolder(private val binding: ItemMoodEntryBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(moodEntry: MoodEntry) {
            binding.apply {
                textMoodEmoji.text = moodEntry.emoji
                textMoodLevel.text = moodEntry.moodLevel.displayName
                textMoodTime.text = "${moodEntry.getFormattedDate()} • ${moodEntry.getFormattedTime()}"
                
                // Show note if available
                if (moodEntry.note.isNotEmpty()) {
                    textMoodNote.text = moodEntry.note
                    textMoodNote.visibility = View.VISIBLE
                } else {
                    textMoodNote.visibility = View.GONE
                }
                
                // Show tags if available
                if (moodEntry.tags.isNotEmpty()) {
                    layoutMoodTags.visibility = View.VISIBLE
                    // For simplicity, showing first two tags
                    // In a real implementation, you'd create dynamic tag views
                } else {
                    layoutMoodTags.visibility = View.GONE
                }
                
                // Set click listener
                root.setOnClickListener {
                    onMoodEntryClick(moodEntry)
                }
            }
        }
    }
}
