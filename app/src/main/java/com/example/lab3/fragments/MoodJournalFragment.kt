package com.example.lab3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab3.R
import com.example.lab3.adapters.MoodEntriesAdapter
import com.example.lab3.data.MoodEntry
import com.example.lab3.data.WellnessDataManager
import com.example.lab3.databinding.FragmentMoodJournalBinding
import com.example.lab3.dialogs.AddMoodDialog
import java.text.SimpleDateFormat
import java.util.*

class MoodJournalFragment : Fragment() {
    
    private var _binding: FragmentMoodJournalBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: WellnessDataManager
    private lateinit var moodAdapter: MoodEntriesAdapter
    private val moodEntries = mutableListOf<MoodEntry>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodJournalBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            dataManager = WellnessDataManager(requireContext())
            setupRecyclerView()
            setupFloatingActionButton()
            loadMoodEntries()
        } catch (e: Exception) {
            android.util.Log.e("MoodJournalFragment", "Error in onViewCreated: ${e.message}")
        }
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodEntriesAdapter(moodEntries) { moodEntry ->
            // Handle mood entry click (e.g., edit or delete)
            showMoodEntryOptions(moodEntry)
        }
        
        binding.recyclerViewMoods.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMoods.adapter = moodAdapter
        
        // Performance optimizations to reduce flickering
        binding.recyclerViewMoods.setHasFixedSize(true)
        binding.recyclerViewMoods.setItemViewCacheSize(15)
        binding.recyclerViewMoods.isDrawingCacheEnabled = true
        binding.recyclerViewMoods.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
    }
    
    private fun setupFloatingActionButton() {
        binding.fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }
    }
    
    
    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(dataManager.getMoodEntries().sortedByDescending { it.date })
        moodAdapter.notifyDataSetChanged()
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        if (moodEntries.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerViewMoods.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerViewMoods.visibility = View.VISIBLE
        }
    }
    
    private fun showAddMoodDialog() {
        val dialog = AddMoodDialog { moodEntry ->
            dataManager.addMoodEntry(moodEntry)
            loadMoodEntries()
        }
        dialog.show(parentFragmentManager, "AddMoodDialog")
    }
    
    private fun showMoodEntryOptions(moodEntry: MoodEntry) {
        val options = arrayOf("Edit", "Delete")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Mood Entry Options")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> editMoodEntry(moodEntry)
                1 -> deleteMoodEntry(moodEntry)
            }
        }
        builder.show()
    }
    
    private fun editMoodEntry(moodEntry: MoodEntry) {
        // Open edit dialog
        val dialog = AddMoodDialog { newMoodEntry ->
            dataManager.updateMoodEntry(newMoodEntry)
            loadMoodEntries()
        }
        dialog.arguments = Bundle().apply {
            putSerializable("moodEntry", moodEntry)
        }
        dialog.show(parentFragmentManager, "EditMoodDialog")
    }
    
    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Mood Entry")
        builder.setMessage("Are you sure you want to delete this mood entry?")
        builder.setPositiveButton("Delete") { _, _ ->
            dataManager.deleteMoodEntry(moodEntry.id)
            loadMoodEntries()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
