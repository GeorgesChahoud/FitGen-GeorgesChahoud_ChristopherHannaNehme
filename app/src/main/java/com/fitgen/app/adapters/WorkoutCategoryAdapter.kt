package com.fitgen.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R

data class WorkoutCategory(
    val name: String,
    val count: Int,
    val color: Int,
    val categoryKey: String
)

class WorkoutCategoryAdapter(
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<WorkoutCategoryAdapter.ViewHolder>() {

    private var categories = listOf<WorkoutCategory>()

    fun submitList(newCategories: List<WorkoutCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val categoryCountTextView: TextView = itemView.findViewById(R.id.categoryCountTextView)
        private val categoryColorView: View = itemView.findViewById(R.id.categoryColorView)

        fun bind(category: WorkoutCategory) {
            categoryNameTextView.text = category.name
            categoryCountTextView.text = itemView.context.getString(
                R.string.workouts_count,
                category.count
            )
            categoryColorView.setBackgroundColor(category.color)

            itemView.setOnClickListener {
                onCategoryClick(category.categoryKey)
            }
        }
    }
}
