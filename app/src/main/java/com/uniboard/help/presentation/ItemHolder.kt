package com.uniboard.help.presentation

import com.uniboard.databinding.ItemInfoBinding
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ItemHolder(
    private val binding: ItemInfoBinding,
    private val onClick: (ListItem) -> Unit,
) : ViewHolder(binding.root) {

    fun onBind(music: ListItem){
        binding.run {
            textShort.text = music.shortText
            val radius = 12

            root.setOnClickListener {
                onClick(music)
            }


        }
    }
}