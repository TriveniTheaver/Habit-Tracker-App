package com.example.lab3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab3.databinding.ItemEmojiSelectorBinding

class EmojiSelectorAdapter(
    private val emojis: List<String>,
    private val onEmojiSelected: (String) -> Unit
) : RecyclerView.Adapter<EmojiSelectorAdapter.EmojiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val binding = ItemEmojiSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmojiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(emojis[position])
    }

    override fun getItemCount(): Int = emojis.size

    inner class EmojiViewHolder(private val binding: ItemEmojiSelectorBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(emoji: String) {
            binding.textEmoji.text = emoji
            binding.root.setOnClickListener {
                onEmojiSelected(emoji)
            }
        }
    }
}
