package com.example.fitnesstrackingmobileapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackingmobileapp.data.FitnessActivity
import java.text.SimpleDateFormat
import java.util.*

class ActivityHistoryAdapter(
        private val onViewDetails: (FitnessActivity) -> Unit,
        private val onShare: (FitnessActivity) -> Unit
) : ListAdapter<FitnessActivity, ActivityHistoryAdapter.ViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_activity_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val activityIcon: ImageView = itemView.findViewById(R.id.activityIcon)
        private val activityTitle: TextView = itemView.findViewById(R.id.activityTitle)
        private val activityDate: TextView = itemView.findViewById(R.id.activityDate)
        private val activityTypeBadge: TextView = itemView.findViewById(R.id.activityTypeBadge)
        private val durationText: TextView = itemView.findViewById(R.id.durationText)
        private val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        private val caloriesText: TextView = itemView.findViewById(R.id.caloriesText)

        fun bind(activity: FitnessActivity) {
            try {
                // Set activity icon based on type
                val iconRes =
                        when (activity.activityType) {
                            "RUNNING" -> R.drawable.baseline_directions_run_24
                            "CYCLING" -> R.drawable.baseline_directions_bike_24
                            "WEIGHTLIFTING" -> R.drawable.baseline_fitness_center_24
                            else -> R.drawable.baseline_directions_run_24
                        }
                activityIcon.setImageResource(iconRes)

                // Set activity title and date
                activityTitle.text = activity.title
                activityDate.text = formatDate(activity.startTime)

                // Set activity type badge
                activityTypeBadge.text = activity.activityType

                // Set duration
                durationText.text = formatDuration(activity.durationSeconds)

                // Set distance
                distanceText.text = "%.2f km".format(activity.distanceKm)

                // Set calories
                caloriesText.text = "%.0f".format(activity.caloriesBurned)
            } catch (e: Exception) {
                // Handle any binding errors gracefully
                activityTitle.text = "Error loading activity"
                activityDate.text = "Unknown date"
                durationText.text = "--:--"
                distanceText.text = "0.00 km"
                caloriesText.text = "0"
            }
        }

        private fun formatDate(timestamp: Long): String {
            try {
                val date = Date(timestamp)
                val today = Calendar.getInstance()
                val activityDate = Calendar.getInstance().apply { timeInMillis = timestamp }

                return when {
                    isSameDay(today, activityDate) ->
                            "Today, ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)}"
                    isSameDay(today.apply { add(Calendar.DAY_OF_YEAR, -1) }, activityDate) ->
                            "Yesterday, ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)}"
                    else -> SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                return "Unknown date"
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

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return try {
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
            } catch (e: Exception) {
                false
            }
        }
    }
}

class ActivityDiffCallback : DiffUtil.ItemCallback<FitnessActivity>() {
    override fun areItemsTheSame(oldItem: FitnessActivity, newItem: FitnessActivity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FitnessActivity, newItem: FitnessActivity): Boolean {
        return oldItem == newItem
    }
}
