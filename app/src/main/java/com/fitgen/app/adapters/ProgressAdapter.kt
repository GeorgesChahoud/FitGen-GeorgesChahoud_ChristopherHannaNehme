package com.fitgen.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.models.ProgressEntry

class ProgressAdapter : RecyclerView.Adapter<ProgressAdapter.ViewHolder>() {

    private var entries = listOf<ProgressEntry>()

    fun submitList(newEntries: List<ProgressEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = entries.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val weightTextView: TextView = itemView.findViewById(R.id.weightTextView)
        private val notesTextView: TextView = itemView.findViewById(R.id.notesTextView)

        fun bind(entry: ProgressEntry) {
            dateTextView.text = entry.date
            weightTextView.text = itemView.context.getString(
                R.string.kg_format,
                entry.weight
            )
            
            if (entry.notes.isNotEmpty()) {
                notesTextView.text = entry.notes
                notesTextView.visibility = View.VISIBLE
            } else {
                notesTextView.visibility = View.GONE
            }
        }
    }
}
