package com.example.lab3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.R
import com.example.lab3.data.MoodEntry
import com.example.lab3.databinding.ItemCalendarDayBinding
import java.util.*

class CalendarDayAdapter(
    private val currentMonth: Calendar,
    private val onDayClick: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.CalendarDayViewHolder>() {
    
    private val moodEntries = mutableListOf<MoodEntry>()
    private val calendarDays = mutableListOf<CalendarDay>()
    
    init {
        generateCalendarDays()
    }
    
    private fun generateCalendarDays() {
        calendarDays.clear()
        
        // Get first day of month and adjust to start of week
        val firstDayOfMonth = Calendar.getInstance().apply {
            time = currentMonth.time
            set(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Get the first Sunday of the calendar
        val firstSunday = Calendar.getInstance().apply {
            time = firstDayOfMonth.time
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
        }
        
        // Generate 42 days (6 weeks)
        for (i in 0..41) {
            val day = Calendar.getInstance().apply {
                time = firstSunday.time
                add(Calendar.DAY_OF_MONTH, i)
            }
            
            val isCurrentMonth = day.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
            val isToday = isSameDay(day, Calendar.getInstance())
            
            calendarDays.add(CalendarDay(
                calendar = day,
                dayNumber = day.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = isCurrentMonth,
                isToday = isToday
            ))
        }
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
    
    fun updateMoodEntries(entries: List<MoodEntry>) {
        moodEntries.clear()
        moodEntries.addAll(entries)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarDayViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        val day = calendarDays[position]
        val dayMoods = getMoodsForDay(day.calendar)
        
        holder.bind(day, dayMoods)
    }
    
    override fun getItemCount(): Int = calendarDays.size
    
    private fun getMoodsForDay(day: Calendar): List<MoodEntry> {
        return moodEntries.filter { entry ->
            val entryCalendar = Calendar.getInstance()
            entryCalendar.time = entry.date
            entryCalendar.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
            entryCalendar.get(Calendar.MONTH) == day.get(Calendar.MONTH) &&
            entryCalendar.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH)
        }
    }
    
    inner class CalendarDayViewHolder(private val binding: ItemCalendarDayBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(day: CalendarDay, moods: List<MoodEntry>) {
            binding.textDayNumber.text = day.dayNumber.toString()
            
            // Set text color based on month
            val textColor = if (day.isCurrentMonth) {
                if (day.isToday) R.color.primary else R.color.text_primary
            } else {
                R.color.text_secondary
            }
            binding.textDayNumber.setTextColor(binding.root.context.getColor(textColor))
            
            // Show mood indicator
            if (moods.isNotEmpty() && day.isCurrentMonth) {
                val latestMood = moods.maxByOrNull { it.date }
                latestMood?.let { mood ->
                    binding.textMoodEmoji.text = mood.emoji
                    binding.textMoodEmoji.visibility = View.VISIBLE
                    binding.viewMoodIndicator.visibility = View.GONE
                }
            } else {
                binding.textMoodEmoji.visibility = View.GONE
                binding.viewMoodIndicator.visibility = View.GONE
            }
            
            // Set click listener
            binding.root.setOnClickListener {
                onDayClick(day.calendar)
            }
        }
    }
    
    data class CalendarDay(
        val calendar: Calendar,
        val dayNumber: Int,
        val isCurrentMonth: Boolean,
        val isToday: Boolean
    )
}
