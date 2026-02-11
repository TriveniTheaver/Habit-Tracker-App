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

class EditHabitDialog(
    private val habit: Habit,
    private val onHabitUpdated: (Habit) -> Unit
) : DialogFragment() {

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
        
        setupSpinner()
        populateFields()
        setupClickListeners()
    }

    private fun setupSpinner() {
        val categories = HabitCategory.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun populateFields() {
        // Update dialog title
        binding.textDialogTitle.text = "Edit Habit"
        
        binding.editTextName.setText(habit.name)
        binding.editTextDescription.setText(habit.description)
        binding.editTextTargetCount.setText(habit.targetCount.toString())
        binding.editTextUnit.setText(habit.unit)
        
        // Set category selection
        val categoryIndex = HabitCategory.values().indexOf(habit.category)
        if (categoryIndex >= 0) {
            binding.spinnerCategory.setSelection(categoryIndex)
        }
    }

    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            if (validateInput()) {
                val updatedHabit = createUpdatedHabit()
                onHabitUpdated(updatedHabit)
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

    private fun createUpdatedHabit(): Habit {
        val name = binding.editTextName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val targetCount = binding.editTextTargetCount.text.toString().toInt()
        val unit = binding.editTextUnit.text.toString().trim()
        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val category = HabitCategory.values()[categoryIndex]

        return habit.copy(
            name = name,
            description = description,
            category = category,
            targetCount = targetCount,
            unit = unit
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
