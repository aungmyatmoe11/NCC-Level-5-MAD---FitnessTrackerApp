package com.example.fitnesstrackingmobileapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackingmobileapp.data.FitnessActivity
import java.util.*

class RecentActivitiesTableAdapter(private val onItemClick: (FitnessActivity) -> Unit) :
        ListAdapter<FitnessActivity, RecentActivitiesTableAdapter.ViewHolder>(
                RecentActivityDiffCallback()
        ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_recent_activity_table, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val activityNameText: TextView = itemView.findViewById(R.id.activityNameText)
        private val activityTypeText: TextView = itemView.findViewById(R.id.activityTypeText)
        private val durationText: TextView = itemView.findViewById(R.id.durationText)
        private val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        private val caloriesText: TextView = itemView.findViewById(R.id.caloriesText)

        fun bind(activity: FitnessActivity) {
            try {
                // Set activity name and type
                activityNameText.text = activity.title
                activityTypeText.text = activity.activityType

                // Set duration
                durationText.text = formatDuration(activity.durationSeconds)

                // Set distance
                distanceText.text = "%.2f km".format(activity.distanceKm)

                // Set calories
                caloriesText.text = "%.0f".format(activity.caloriesBurned)

                // Set click listener
                itemView.setOnClickListener { onItemClick(activity) }
            } catch (e: Exception) {
                // Handle any binding errors gracefully
                activityNameText.text = "Error loading activity"
                activityTypeText.text = "Unknown"
                durationText.text = "--:--"
                distanceText.text = "0.00 km"
                caloriesText.text = "0"
            }
        }

        private fun formatDuration(seconds: Long): String {
            try {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60

                return when {
                    hours > 0 -> "%02d:%02d:%02d".format(hours, minutes, secs)
                    else -> "%02d:%02d".format(minutes, secs)
                }
            } catch (e: Exception) {
                return "--:--"
            }
        }
    }
}

class RecentActivityDiffCallback : DiffUtil.ItemCallback<FitnessActivity>() {
    override fun areItemsTheSame(oldItem: FitnessActivity, newItem: FitnessActivity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FitnessActivity, newItem: FitnessActivity): Boolean {
        return oldItem == newItem
    }
}
