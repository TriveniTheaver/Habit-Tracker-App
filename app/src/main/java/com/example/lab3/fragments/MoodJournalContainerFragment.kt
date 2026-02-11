package com.example.lab3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lab3.R
import com.example.lab3.databinding.FragmentMoodJournalContainerBinding

class MoodJournalContainerFragment : Fragment() {
    
    private var _binding: FragmentMoodJournalContainerBinding? = null
    private val binding get() = _binding!!
    
    private var currentView = "list" // "list" or "calendar"
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodJournalContainerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Restore state if available
        savedInstanceState?.let {
            currentView = it.getString("current_view", "list")
        }
        
        setupViewToggle()
        showInitialView()
    }
    
    private fun setupViewToggle() {
        binding.buttonListView.setOnClickListener {
            showListView()
        }
        
        binding.buttonCalendarView.setOnClickListener {
            showCalendarView()
        }
    }
    
    private fun showInitialView() {
        if (currentView == "calendar") {
            showCalendarView()
        } else {
            showListView()
        }
    }
    
    private fun showListView() {
        currentView = "list"
        
        // Show list fragment
        val listFragment = MoodJournalFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, listFragment)
            .commit()
        
        // Update button states
        binding.buttonListView.isEnabled = false
        binding.buttonCalendarView.isEnabled = true
    }
    
    private fun showCalendarView() {
        currentView = "calendar"
        
        // Show calendar fragment
        val calendarFragment = MoodCalendarFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, calendarFragment)
            .commit()
        
        // Update button states
        binding.buttonListView.isEnabled = true
        binding.buttonCalendarView.isEnabled = false
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_view", currentView)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
