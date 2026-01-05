package com.fitgen.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.models.Workout
import com.fitgen.app.utils.Constants

class WorkoutAdapter(
    private val onWorkoutClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    private var workouts = listOf<Workout>()

    fun submitList(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        holder.bind(workout)
    }

    override fun getItemCount(): Int = workouts.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val workoutNameTextView: TextView = itemView.findViewById(R.id.workoutNameTextView)
        private val difficultyTextView: TextView = itemView.findViewById(R.id.difficultyTextView)
        private val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)

        fun bind(workout: Workout) {
            workoutNameTextView.text = workout.name
            
            // Set difficulty text
            difficultyTextView.text = when (workout.difficulty) {
                Constants.DIFFICULTY_BEGINNER -> itemView.context.getString(R.string.beginner)
                Constants.DIFFICULTY_INTERMEDIATE -> itemView.context.getString(R.string.intermediate)
                Constants.DIFFICULTY_ADVANCED -> itemView.context.getString(R.string.advanced)
                else -> workout.difficulty
            }
            
            // Set difficulty background drawable
            val difficultyBackground = when (workout.difficulty) {
                Constants.DIFFICULTY_BEGINNER -> R.drawable.bg_difficulty_beginner
                Constants.DIFFICULTY_INTERMEDIATE -> R.drawable.bg_difficulty_intermediate
                Constants.DIFFICULTY_ADVANCED -> R.drawable.bg_difficulty_advanced
                else -> R.drawable.bg_difficulty_default
            }
            difficultyTextView.setBackgroundResource(difficultyBackground)
            
            durationTextView.text = itemView.context.getString(
                R.string.min_format,
                workout.duration
            )
            
            caloriesTextView.text = itemView.context.getString(
                R.string.cal_format,
                workout.caloriesBurn
            )

            itemView.setOnClickListener {
                onWorkoutClick(workout)
            }
        }
    }
}
