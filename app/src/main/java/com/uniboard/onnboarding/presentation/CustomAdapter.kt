package com.uniboard.onnboarding.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uniboard.R

class CustomAdapter(
    private val dataList: List<ItemsViewModel>,
    private val listener1: OnItemClickListener,
    private val listener2: OnItemClickListener
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.titleTextView.text = data.heading
        holder.titleTextView.setOnClickListener {
            listener1.onItemClick(holder.adapterPosition, dataList)
        }
        holder.btnDelview.setOnClickListener {
            listener2.onDelClick(holder.adapterPosition, dataList)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textView)
        val btnDelview: Button = view.findViewById(R.id.delete)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, dataList: List<ItemsViewModel>)
        fun onDelClick(position: Int, dataList: List<ItemsViewModel>)
    }

}