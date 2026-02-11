package com.example.lab3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.data.HydrationEntry
import com.example.lab3.databinding.ItemHydrationEntryBinding

class HydrationEntriesAdapter(
    private var entries: List<HydrationEntry>,
    private val onEntryClick: (HydrationEntry) -> Unit
) : RecyclerView.Adapter<HydrationEntriesAdapter.HydrationEntryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HydrationEntryViewHolder {
        val binding = ItemHydrationEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HydrationEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HydrationEntryViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    fun updateEntries(newEntries: List<HydrationEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    inner class HydrationEntryViewHolder(private val binding: ItemHydrationEntryBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: HydrationEntry) {
            binding.apply {
                textHydrationAmount.text = entry.getFormattedAmount()
                textHydrationTime.text = entry.getFormattedTime()
                
                imageDelete.setOnClickListener {
                    onEntryClick(entry)
                }
            }
        }
    }
}
