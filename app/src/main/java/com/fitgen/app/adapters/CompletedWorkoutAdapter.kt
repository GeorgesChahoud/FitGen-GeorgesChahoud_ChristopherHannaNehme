package com.fitgen.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.models.CompletedWorkout

/**
 * Adapter for displaying completed workout history
 */
class CompletedWorkoutAdapter : RecyclerView.Adapter<CompletedWorkoutAdapter.ViewHolder>() {

    private var completedWorkouts = listOf<CompletedWorkout>()

    fun submitList(newCompletedWorkouts: List<CompletedWorkout>) {
        completedWorkouts = newCompletedWorkouts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_workout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = completedWorkouts[position]
        holder.bind(workout)
    }

    override fun getItemCount(): Int = completedWorkouts.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val workoutNameTextView: TextView = itemView.findViewById(R.id.workoutNameTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)

        fun bind(workout: CompletedWorkout) {
            workoutNameTextView.text = workout.workoutName
            categoryTextView.text = workout.workoutCategory.replaceFirstChar { it.uppercase() }
            dateTextView.text = workout.completedDate
            
            durationTextView.text = itemView.context.getString(
                R.string.min_format,
                workout.duration
            )
            
            caloriesTextView.text = itemView.context.getString(
                R.string.cal_format,
                workout.caloriesBurned
            )
        }
    }
}
