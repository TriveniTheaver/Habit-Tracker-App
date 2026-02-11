package com.example.lab3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab3.R
import com.example.lab3.adapters.HabitsAdapter
import com.example.lab3.data.Habit
import com.example.lab3.data.WellnessDataManager
import com.example.lab3.databinding.FragmentHabitsBinding
import com.example.lab3.dialogs.AddHabitDialog
import com.example.lab3.dialogs.EditHabitDialog
import java.text.SimpleDateFormat
import java.util.*

/**
 * Habits Fragment - Dedicated screen for managing daily wellness habits.
 * 
 * This fragment provides comprehensive habit management functionality:
 * - Display all active habits with completion status
 * - Toggle habit completion for today
 * - Add new habits with detailed configuration
 * - Edit existing habits (name, description, category, targets)
 * - Delete habits with confirmation
 * - View weekly progress for each habit
 * - Responsive layout (grid for tablets, list for phones)
 * 
 * The fragment adapts its layout based on screen size:
 * - Phones: Linear layout with single column
 * - Tablets: Grid layout with multiple columns
 * - Landscape: Optimized layout for wider screens
 */
class HabitsFragment : Fragment() {
    
    // View binding for the habits fragment layout
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    
    // Data manager for accessing habit data
    private lateinit var dataManager: WellnessDataManager
    
    // Adapter for displaying habits in RecyclerView
    private lateinit var habitsAdapter: HabitsAdapter
    
    // Local list of habits for the adapter
    private val habits = mutableListOf<Habit>()
    
    // Today's date in YYYY-MM-DD format for completion tracking
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Flag to prevent recursive updates
    private var isUpdating = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dataManager = WellnessDataManager(requireContext())
        setupRecyclerView()
        setupFloatingActionButton()
        loadHabits()
    }
    
    override fun onResume() {
        super.onResume()
        loadHabits()
    }
    
    private fun setupRecyclerView() {
        try {
            habitsAdapter = HabitsAdapter(
                habits,
                onHabitToggled = { habit, isCompleted ->
                    if (isUpdating) {
                        return@HabitsAdapter
                    }
                    
                    isUpdating = true
                    
                    if (isCompleted) {
                        habit.markCompleted(today)
                    } else {
                        habit.markIncomplete(today)
                    }
                    
                    dataManager.updateHabit(habit)
                    loadHabits()
                    isUpdating = false
                },
                onHabitEdit = { habit ->
                    showEditHabitDialog(habit)
                },
                onHabitDelete = { habit ->
                    showDeleteConfirmation(habit)
                }
            )
            
            val layoutManager = if (resources.configuration.screenWidthDp >= 600) {
                GridLayoutManager(requireContext(), 2)
            } else {
                LinearLayoutManager(requireContext())
            }
            
            binding.recyclerViewHabits.layoutManager = layoutManager
            binding.recyclerViewHabits.adapter = habitsAdapter
            
            binding.recyclerViewHabits.setHasFixedSize(true)
            binding.recyclerViewHabits.setItemViewCacheSize(20)
            binding.recyclerViewHabits.isDrawingCacheEnabled = true
            binding.recyclerViewHabits.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error setting up RecyclerView: ${e.message}")
        }
    }
    
    private fun setupFloatingActionButton() {
        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }
    
    private fun loadHabits() {
        habits.clear()
        habits.addAll(dataManager.getHabitsForDate(today))
        habitsAdapter.notifyDataSetChanged()
        updateProgress()
    }
    
    private fun updateProgress() {
        // Progress is shown on the Home page
    }
    
    private fun showAddHabitDialog() {
        val dialog = AddHabitDialog { habit ->
            dataManager.addHabit(habit)
            loadHabits()
        }
        dialog.show(parentFragmentManager, "AddHabitDialog")
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialog = EditHabitDialog(habit) { updatedHabit ->
            dataManager.updateHabit(updatedHabit)
            loadHabits()
        }
        dialog.show(parentFragmentManager, "EditHabitDialog")
    }
    
    private fun showDeleteConfirmation(habit: Habit) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                dataManager.deleteHabit(habit.id)
                loadHabits()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
