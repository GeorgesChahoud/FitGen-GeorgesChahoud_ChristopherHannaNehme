package com.fitgen.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.models.LeaderboardEntry

class LeaderboardAdapter : ListAdapter<LeaderboardEntry, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val ivRankBadge: ImageView = itemView.findViewById(R.id.ivRankBadge)
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvCurrentStreak: TextView = itemView.findViewById(R.id.tvCurrentStreak)
        private val tvLongestStreak: TextView = itemView.findViewById(R.id.tvLongestStreak)
        private val tvTotalChallenges: TextView = itemView.findViewById(R.id.tvTotalChallenges)

        fun bind(entry: LeaderboardEntry) {
            tvUserName.text = if (entry.userName.isNotEmpty()) {
                "@${entry.userName}"
            } else {
                "@${entry.username}"
            }

            val streakText = if (entry.currentStreak == 1) {
                "${entry.currentStreak} day"
            } else {
                "${entry.currentStreak} days"
            }
            tvCurrentStreak.text = streakText

            tvLongestStreak.text = "Best: ${entry.longestStreak}"
            tvTotalChallenges.text = "Total: ${entry.totalChallengesCompleted}"

            // Show rank badge for top 3, otherwise show rank number
            when (entry.rank) {
                1 -> {
                    ivRankBadge.visibility = View.VISIBLE
                    tvRank.visibility = View.GONE
                    ivRankBadge.setImageResource(R.drawable.ic_medal_gold)
                }
                2 -> {
                    ivRankBadge.visibility = View.VISIBLE
                    tvRank.visibility = View.GONE
                    ivRankBadge.setImageResource(R.drawable.ic_medal_silver)
                }
                3 -> {
                    ivRankBadge.visibility = View.VISIBLE
                    tvRank.visibility = View.GONE
                    ivRankBadge.setImageResource(R.drawable.ic_medal_bronze)
                }
                else -> {
                    ivRankBadge.visibility = View.GONE
                    tvRank.visibility = View.VISIBLE
                    tvRank.text = entry.rank.toString()
                }
            }

            // Highlight current user
            if (entry.isCurrentUser) {
                cardView.setCardBackgroundColor(
                    itemView.context.getColor(android.R.color.holo_blue_light).let {
                        Color.argb(50, Color.red(it), Color.green(it), Color.blue(it))
                    }
                )
            } else {
                cardView.setCardBackgroundColor(
                    itemView.context.getColor(android.R.color.white)
                )
            }
        }
    }

    class LeaderboardDiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem == newItem
        }
    }
}
