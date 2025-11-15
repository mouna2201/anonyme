package com.example.socialapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialapp.R
import com.example.socialapp.databinding.ItemPostBinding
import com.example.socialapp.data.models.Post
import java.text.SimpleDateFormat
import java.util.*



class PostsAdapter(
    private val currentUserId: String,
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onMoreClick: (Post) -> Unit
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            // Affichage du nom (anonyme ou non)
            binding.tvUsername.text = if (post.isAnonymous) {
                "Utilisateur Anonyme"
            } else {
                post.userId?.displayName ?: "Utilisateur"
            }

            // Avatar
            if (post.isAnonymous || post.userId?.profilePicture.isNullOrEmpty()) {
                binding.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
            } else {
                Glide.with(binding.root.context)
                    .load(post.userId?.profilePicture)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatar)
            }

            // Contenu
            binding.tvContent.text = post.content

            // Image du post
            if (post.imageUrl.isNotEmpty()) {
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(post.imageUrl)
                    .into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // Timestamp
            binding.tvTimestamp.text = formatTimestamp(post.createdAt)

            // Likes
            binding.tvLikesCount.text = post.likesCount.toString()
            val isLiked = post.isLikedByUser(currentUserId)
            binding.btnLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
            )

            // Comments
            binding.tvCommentsCount.text = post.commentsCount.toString()

            // Listeners
            binding.btnLike.setOnClickListener { onLikeClick(post) }
            binding.btnComment.setOnClickListener { onCommentClick(post) }
            binding.btnMore.setOnClickListener { onMoreClick(post) }
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
                    val days = hours / 24

                    when {
                        seconds < 60 -> "À l'instant"
                        minutes < 60 -> "Il y a $minutes min"
                        hours < 24 -> "Il y a $hours h"
                        days < 7 -> "Il y a $days j"
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

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}