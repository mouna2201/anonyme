package com.example.socialapp.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialapp.R
import com.example.socialapp.databinding.ItemCommentBinding
import com.example.socialapp.data.models.Comment
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private val currentUserId: String,
    private val onMoreClick: (Comment) -> Unit
) : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            // Username
            binding.tvUsername.text = if (comment.isAnonymous) {
                "Utilisateur Anonyme"
            } else {
                comment.userId?.displayName ?: "Utilisateur"
            }

            // Avatar
            if (comment.isAnonymous || comment.userId?.profilePicture.isNullOrEmpty()) {
                binding.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
            } else {
                Glide.with(binding.root.context)
                    .load(comment.userId?.profilePicture)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatar)
            }

            // Content
            binding.tvContent.text = comment.content

            // Timestamp
            binding.tvTimestamp.text = formatTimestamp(comment.createdAt)

            // More button
            binding.btnMore.setOnClickListener { onMoreClick(comment) }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(timestamp)

                if (date != null) {
                    val now = Date()
                    val diff = now.time - date.time

                    val seconds = diff / 1000
                    val minutes = seconds / 60
                    val hours = minutes / 60

                    when {
                        seconds < 60 -> "À l'instant"
                        minutes < 60 -> "Il y a $minutes min"
                        hours < 24 -> "Il y a $hours h"
                        else -> {
                            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            outputFormat.format(date)
                        }
                    }
                } else {
                    "Date inconnue"
                }
            } catch (e: Exception) {
                "Date inconnue"
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}