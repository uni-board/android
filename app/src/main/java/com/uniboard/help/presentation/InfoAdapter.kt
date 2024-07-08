package com.uniboard.help.presentation

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uniboard.databinding.ItemInfoBinding

class InfoAdapter(
    private val list: List<ListItem>,
    private val onClick: (ListItem) -> Unit,):
    RecyclerView.Adapter<ItemHolder>()
{
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemHolder = ItemHolder(
        ItemInfoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false),
        onClick = onClick

    )
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size
}

