package com.example.lab3.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.lab3.R
import com.example.lab3.data.Habit
import com.example.lab3.data.HabitCategory
import com.example.lab3.databinding.DialogAddHabitBinding
import java.util.*

/**
 * Dialog for adding new wellness habits to the tracking system.
 * 
 * This dialog provides a comprehensive form for creating new habits with:
 * - Habit name and description input
 * - Category selection from predefined wellness categories
 * - Target count and unit specification
 * - Input validation to ensure data quality
 * - Callback mechanism for habit creation
 * 
 * The dialog handles:
 * - Form validation (required fields, numeric inputs)
 * - Category selection with spinner
 * - Habit creation with unique ID generation
 * - Error display for invalid inputs
 * 
 * @param onHabitAdded Callback function called when a valid habit is created
 */
class AddHabitDialog(
    private val onHabitAdded: (Habit) -> Unit
) : DialogFragment() {

    // View binding for the dialog layout
    private var _binding: DialogAddHabitBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCategorySpinner()
        setupClickListeners()
    }

    private fun setupCategorySpinner() {
        val categories = HabitCategory.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            if (validateInput()) {
                val habit = createHabit()
                onHabitAdded(habit)
                dismiss()
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.editTextName.text.toString().trim()
        val targetCount = binding.editTextTargetCount.text.toString().trim()
        val unit = binding.editTextUnit.text.toString().trim()

        if (name.isEmpty()) {
            binding.editTextName.error = "Habit name is required"
            return false
        }

        if (targetCount.isEmpty() || targetCount.toIntOrNull() == null || targetCount.toInt() <= 0) {
            binding.editTextTargetCount.error = "Please enter a valid target count"
            return false
        }

        if (unit.isEmpty()) {
            binding.editTextUnit.error = "Unit is required"
            return false
        }

        return true
    }

    private fun createHabit(): Habit {
        val name = binding.editTextName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val targetCount = binding.editTextTargetCount.text.toString().toInt()
        val unit = binding.editTextUnit.text.toString().trim()
        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val category = HabitCategory.values()[categoryIndex]

        return Habit(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            category = category,
            targetCount = targetCount,
            unit = unit,
            isActive = true,
            createdAt = Date()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
