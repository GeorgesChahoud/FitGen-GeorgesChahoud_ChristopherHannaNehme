package com.fitgen.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.models.Friend

class FriendAdapter(
    private val onFriendClick: (Friend) -> Unit,
    private val onRemoveClick: (Friend) -> Unit
) : ListAdapter<Friend, FriendAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view, onFriendClick, onRemoveClick)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FriendViewHolder(
        itemView: View,
        private val onFriendClick: (Friend) -> Unit,
        private val onRemoveClick: (Friend) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvFriendEmail: TextView = itemView.findViewById(R.id.tvFriendEmail)
        private val tvStreak: TextView = itemView.findViewById(R.id.tvStreak)
        private val ivRemove: ImageView = itemView.findViewById(R.id.ivRemove)

        fun bind(friend: Friend) {
            val displayName = if (friend.friendName.isNotEmpty()) {
                "@${friend.friendName}"
            } else {
                "@${friend.friendUsername}"
            }
            
            tvFriendEmail.text = displayName
            
            val streakText = if (friend.currentStreak == 1) {
                "${friend.currentStreak} day streak"
            } else {
                "${friend.currentStreak} day streak"
            }
            tvStreak.text = streakText

            itemView.setOnClickListener { onFriendClick(friend) }
            ivRemove.setOnClickListener { onRemoveClick(friend) }
        }
    }

    class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem == newItem
        }
    }
}
