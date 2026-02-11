package com.example.lab3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lab3.R
import com.example.lab3.adapters.CalendarDayAdapter
import com.example.lab3.data.MoodEntry
import com.example.lab3.data.WellnessDataManager
import com.example.lab3.databinding.FragmentMoodCalendarBinding
import java.text.SimpleDateFormat
import java.util.*

class MoodCalendarFragment : Fragment() {
    
    private var _binding: FragmentMoodCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: WellnessDataManager
    private lateinit var calendarAdapter: CalendarDayAdapter
    private val moodEntries = mutableListOf<MoodEntry>()
    
    private var currentDate = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dataManager = WellnessDataManager(requireContext())
        setupCalendar()
        loadMoodEntries()
    }
    
    private fun setupCalendar() {
        calendarAdapter = CalendarDayAdapter(currentDate) { selectedDate ->
            showSelectedDayDetails(selectedDate)
        }
        
        binding.recyclerViewCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.recyclerViewCalendar.adapter = calendarAdapter
        
        // Performance optimizations to reduce flickering
        binding.recyclerViewCalendar.setHasFixedSize(true)
        binding.recyclerViewCalendar.setItemViewCacheSize(42) // 6 weeks * 7 days
        binding.recyclerViewCalendar.isDrawingCacheEnabled = true
        binding.recyclerViewCalendar.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        
        updateMonthDisplay()
        setupNavigationButtons()
    }
    
    private fun setupNavigationButtons() {
        binding.buttonPrevMonth.setOnClickListener {
            currentDate.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            loadMoodEntries()
        }
        
        binding.buttonNextMonth.setOnClickListener {
            currentDate.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            loadMoodEntries()
        }
    }
    
    
    private fun updateMonthDisplay() {
        binding.textMonthYear.text = dateFormat.format(currentDate.time)
    }
    
    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(dataManager.getMoodEntries())
        calendarAdapter.updateMoodEntries(moodEntries)
    }
    
    private fun showSelectedDayDetails(selectedDate: Calendar) {
        val dayMoods = moodEntries.filter { entry ->
            val entryCalendar = Calendar.getInstance()
            entryCalendar.time = entry.date
            entryCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            entryCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
            entryCalendar.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
        }
        
        if (dayMoods.isNotEmpty()) {
            val latestMood = dayMoods.maxByOrNull { it.date }
            latestMood?.let { mood ->
                binding.textSelectedDate.text = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selectedDate.time)
                binding.textSelectedMood.text = "${mood.emoji} ${mood.moodLevel.displayName}"
                
                if (mood.note.isNotEmpty()) {
                    binding.textSelectedNote?.text = mood.note
                    binding.textSelectedNote?.visibility = View.VISIBLE
                } else {
                    binding.textSelectedNote?.visibility = View.GONE
                }
                
                binding.cardSelectedDay.visibility = View.VISIBLE
            }
        } else {
            binding.cardSelectedDay.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
