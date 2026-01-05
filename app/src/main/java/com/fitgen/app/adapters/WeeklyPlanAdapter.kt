package com.fitgen.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.models.DailyWorkoutPlan
import com.fitgen.app.models.Workout

class WeeklyPlanAdapter(
    private val onWorkoutClick: (Workout) -> Unit
) : ListAdapter<DailyWorkoutPlan, WeeklyPlanAdapter.DayViewHolder>(DayDiffCallback()) {

    private val expandedDays = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_plan, parent, false)
        return DayViewHolder(view, onWorkoutClick, expandedDays)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(
        itemView: View,
        private val onWorkoutClick: (Workout) -> Unit,
        private val expandedDays: MutableSet<String>
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val dayNameTextView: TextView = itemView.findViewById(R.id.dayNameTextView)
        private val dayTitleTextView: TextView = itemView.findViewById(R.id.dayTitleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        private val workoutsRecyclerView: RecyclerView = itemView.findViewById(R.id.workoutsRecyclerView)
        private val expandButton: TextView = itemView.findViewById(R.id.expandButton)
        
        // Reuse the same adapter instance for this ViewHolder
        private val workoutAdapter = WorkoutAdapter(onWorkoutClick)
        
        init {
            // Setup nested RecyclerView once
            workoutsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            workoutsRecyclerView.adapter = workoutAdapter
        }
        
        fun bind(dailyPlan: DailyWorkoutPlan) {
            dayNameTextView.text = dailyPlan.dayOfWeek
            dayTitleTextView.text = dailyPlan.dayName
            descriptionTextView.text = dailyPlan.description
            
            if (dailyPlan.isRestDay) {
                durationTextView.visibility = View.GONE
                caloriesTextView.visibility = View.GONE
                workoutsRecyclerView.visibility = View.GONE
                expandButton.visibility = View.GONE
            } else {
                durationTextView.visibility = View.VISIBLE
                caloriesTextView.visibility = View.VISIBLE
                expandButton.visibility = View.VISIBLE
                
                durationTextView.text = "${dailyPlan.totalDuration} min"
                caloriesTextView.text = "${dailyPlan.totalCalories} cal"
                
                // Update adapter with new workout list
                workoutAdapter.submitList(dailyPlan.workouts)
                
                // Restore expansion state
                val isExpanded = expandedDays.contains(dailyPlan.dayOfWeek)
                workoutsRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
                expandButton.text = if (isExpanded) "Hide Exercises ▲" else "View Exercises ▼"
                
                // Expand/collapse functionality
                expandButton.setOnClickListener {
                    val newExpandedState = !expandedDays.contains(dailyPlan.dayOfWeek)
                    if (newExpandedState) {
                        expandedDays.add(dailyPlan.dayOfWeek)
                    } else {
                        expandedDays.remove(dailyPlan.dayOfWeek)
                    }
                    workoutsRecyclerView.visibility = if (newExpandedState) View.VISIBLE else View.GONE
                    expandButton.text = if (newExpandedState) "Hide Exercises ▲" else "View Exercises ▼"
                }
            }
        }
    }

    class DayDiffCallback : DiffUtil.ItemCallback<DailyWorkoutPlan>() {
        override fun areItemsTheSame(oldItem: DailyWorkoutPlan, newItem: DailyWorkoutPlan): Boolean {
            return oldItem.dayOfWeek == newItem.dayOfWeek
        }

        override fun areContentsTheSame(oldItem: DailyWorkoutPlan, newItem: DailyWorkoutPlan): Boolean {
            return oldItem == newItem
        }
    }
}
