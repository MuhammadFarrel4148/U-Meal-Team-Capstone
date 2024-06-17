package com.example.umeal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.umeal.R
import com.example.umeal.data.response.DetectedFoodsItem

class ScanResultAdapter(private val results: List<DetectedFoodsItem?>) :
    RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodItemTextView: TextView = itemView.findViewById(R.id.textViewFood)
        val calorieTextView: TextView = itemView.findViewById(R.id.textViewCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = results[position]
        if (item != null) {
            holder.foodItemTextView.text = item.jenis
        }
        if (item != null) {
            holder.calorieTextView.text = item.kalori.toString()
        }
    }

    override fun getItemCount(): Int {
        return results.size
    }
}
