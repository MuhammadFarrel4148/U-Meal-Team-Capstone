package com.example.umeal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.umeal.R
import com.example.umeal.data.response.DataItem
import com.google.android.material.imageview.ShapeableImageView

class HistoryResultAdapter(private val results: List<DataItem>) :
    RecyclerView.Adapter<HistoryResultAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ShapeableImageView = itemView.findViewById(R.id.iv_food)
        val mealTime: TextView = itemView.findViewById(R.id.tv_meal_time)
        val calories: TextView = itemView.findViewById(R.id.tv_calorie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = results[position]
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.foodImage)
        holder.mealTime.text = getMealTimeLabel(item.scanTimestamp)
        holder.calories.text = item.totalKalori.toString()
    }

    private fun getMealTimeLabel(scanTimestamp: String): String {
        // Parse the timestamp
        val dateTime = scanTimestamp.split(" ")
        val time = dateTime[1].split(":")
        val hour = time[0].toInt()

        return when (hour) {
            in 6..10 -> "Sarapan"
            in 11..14 -> "Makan Siang"
            in 18..21 -> "Makan Malam"
            else -> "Waktu Lain"
        }
    }

}