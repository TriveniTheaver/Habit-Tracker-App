package com.example.lab3.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab3.R
import com.example.lab3.adapters.MoodOptionAdapter
import com.example.lab3.data.MoodEntry
import com.example.lab3.data.MoodLevel
import com.example.lab3.data.PredefinedMood
import com.example.lab3.data.PredefinedMoods
import com.example.lab3.databinding.DialogAddMoodBinding
import java.util.*

/**
 * Dialog fragment for adding a new mood entry.
 * Displays predefined mood options with emojis for quick selection.
 * Users can optionally add a note to describe their mood in more detail.
 */
class AddMoodDialog(
    private val onMoodAdded: (MoodEntry) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddMoodBinding? = null
    private val binding get() = _binding!!
    private var selectedMood: PredefinedMood? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupMoodOptionsSelector()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        // Make dialog wider to cover more of the background
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(), // 95% of screen width
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Dim the background more to hide content behind
            setDimAmount(0.7f) // 0.0 (transparent) to 1.0 (opaque)
        }
    }

    /**
     * Sets up the RecyclerView with predefined mood options.
     * Each option displays an emoji, mood name, and description.
     */
    private fun setupMoodOptionsSelector() {
        val moodAdapter = MoodOptionAdapter(PredefinedMoods.MOODS) { mood ->
            selectedMood = mood
        }
        
        binding.recyclerViewMoodOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMoodOptions.adapter = moodAdapter
    }

    /**
     * Sets up click listeners for the Save and Cancel buttons.
     */
    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            if (validateInput()) {
                val moodEntry = createMoodEntry()
                onMoodAdded(moodEntry)
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select a mood",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Validates that the user has selected a mood option.
     * 
     * @return true if a mood is selected, false otherwise
     */
    private fun validateInput(): Boolean {
        return selectedMood != null
    }

    /**
     * Creates a MoodEntry from the selected mood and optional note.
     * 
     * @return MoodEntry object with selected mood data, note, and current timestamp
     */
    private fun createMoodEntry(): MoodEntry {
        val mood = selectedMood ?: throw IllegalStateException("No mood selected")
        val note = binding.editMoodNote.text.toString().trim()
        
        return MoodEntry(
            id = UUID.randomUUID().toString(),
            emoji = mood.emoji,
            moodLevel = mood.level,
            note = note,
            tags = emptyList(),
            date = Date()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
