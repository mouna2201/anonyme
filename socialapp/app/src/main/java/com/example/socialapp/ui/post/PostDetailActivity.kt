package com.example.socialapp.ui.post

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socialapp.R
import com.example.socialapp.databinding.ActivityPostDetailBinding
import com.example.socialapp.databinding.ItemPostBinding
import com.example.socialapp.data.models.Comment
import com.example.socialapp.data.models.Post
import com.example.socialapp.data.repository.SocialRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private val repository = SocialRepository()
    private var commentsAdapter: CommentsAdapter? = null
    private var currentUserId: String = ""
    private var postId: String = ""
    private var currentPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("POST_ID") ?: ""
        if (postId.isEmpty()) {
            Toast.makeText(this, "Erreur: Post introuvable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadPost()
        loadUserProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        commentsAdapter = CommentsAdapter(
            currentUserId = currentUserId,
            onMoreClick = { comment -> showCommentOptions(comment) }
        )

        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@PostDetailActivity)
            adapter = commentsAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSendComment.setOnClickListener {
            addComment()
        }
    }

    private fun loadPost() {
        if (isFinishing || !::binding.isInitialized) return
        
        lifecycleScope.launch {
            val result = repository.getPost(postId)
            
            if (isFinishing || !::binding.isInitialized) return@launch
            
            result.onSuccess { post ->
                if (isFinishing || !::binding.isInitialized) return@onSuccess
                
                currentPost = post
                displayPost(post)
            }.onFailure { error ->
                if (!isFinishing) {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "Erreur lors du chargement du post: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun displayPost(post: Post) {
        val postLayout = binding.postLayout
        val postBinding = ItemPostBinding.bind(postLayout.root)
        
        // Username
        postBinding.tvUsername.text = if (post.isAnonymous) {
            "Utilisateur Anonyme"
        } else {
            post.userId?.displayName ?: "Utilisateur"
        }

        // Avatar
        if (post.isAnonymous || post.userId?.profilePicture.isNullOrEmpty()) {
            postBinding.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
        } else {
            Glide.with(this)
                .load(post.userId?.profilePicture)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(postBinding.ivAvatar)
        }

        // Content
        postBinding.tvContent.text = post.content

        // Image
        if (post.imageUrl.isNotEmpty()) {
            postBinding.ivPostImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(post.imageUrl)
                .into(postBinding.ivPostImage)
        } else {
            postBinding.ivPostImage.visibility = View.GONE
        }

        // Timestamp
        postBinding.tvTimestamp.text = formatTimestamp(post.createdAt)

        // Likes
        postBinding.tvLikesCount.text = post.likesCount.toString()
        val isLiked = post.isLikedByUser(currentUserId)
        postBinding.btnLike.setImageResource(
            if (isLiked) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )

        // Comments count
        postBinding.tvCommentsCount.text = post.commentsCount.toString()

        // Hide buttons in detail view (they're not needed here)
        postBinding.btnLike.visibility = View.GONE
        postBinding.btnComment.visibility = View.GONE
        postBinding.btnMore.visibility = View.GONE
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

    private fun loadUserProfile() {
        if (isFinishing || !::binding.isInitialized) return
        
        lifecycleScope.launch {
            val result = repository.getUserProfile()
            
            if (isFinishing || !::binding.isInitialized) return@launch
            
            result.onSuccess { user ->
                if (isFinishing || !::binding.isInitialized) return@onSuccess
                
                currentUserId = user._id
                commentsAdapter = CommentsAdapter(
                    currentUserId = currentUserId,
                    onMoreClick = { comment -> showCommentOptions(comment) }
                )
                binding.rvComments.adapter = commentsAdapter
                
                // Re-display post with updated currentUserId for like status
                currentPost?.let { displayPost(it) }
                
                loadComments()
            }.onFailure { error ->
                if (!isFinishing && ::binding.isInitialized) {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "Erreur: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadComments() {
        if (isFinishing || !::binding.isInitialized) return
        
        lifecycleScope.launch {
            val result = repository.getComments(postId)

            if (isFinishing || !::binding.isInitialized) return@launch

            binding.progressBar.visibility = View.GONE

            result.onSuccess { comments ->
                if (isFinishing || !::binding.isInitialized) return@onSuccess
                
                if (comments.isEmpty()) {
                    binding.tvNoComments.visibility = View.VISIBLE
                    binding.rvComments.visibility = View.GONE
                } else {
                    binding.tvNoComments.visibility = View.GONE
                    binding.rvComments.visibility = View.VISIBLE
                    commentsAdapter?.submitList(comments)
                }
            }.onFailure { error ->
                if (!isFinishing) {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "Erreur: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addComment() {
        if (isFinishing || !::binding.isInitialized) return
        
        val content = binding.etComment.text.toString().trim()

        if (content.isEmpty()) {
            binding.tilComment.error = "Le commentaire ne peut pas être vide"
            return
        }

        if (content.length < 2) {
            binding.tilComment.error = "Le commentaire doit contenir au moins 2 caractères"
            return
        }

        binding.tilComment.error = null
        binding.btnSendComment.isEnabled = false

        lifecycleScope.launch {
            val result = repository.createComment(postId, content, true)

            if (isFinishing || !::binding.isInitialized) return@launch

            binding.btnSendComment.isEnabled = true

            result.onSuccess {
                if (!isFinishing && ::binding.isInitialized) {
                    binding.etComment.text?.clear()
                    Toast.makeText(
                        this@PostDetailActivity,
                        "Commentaire ajouté",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadComments()
                    // Refresh post to update comments count
                    loadPost()
                }
            }.onFailure { error ->
                if (!isFinishing) {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "Erreur: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showCommentOptions(comment: Comment) {
        val options = if (comment.userId?._id == currentUserId) {
            arrayOf("Supprimer")
        } else {
            arrayOf("Signaler")
        }

        MaterialAlertDialogBuilder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (comment.userId?._id == currentUserId) {
                            deleteComment(comment)
                        } else {
                            Toast.makeText(
                                this,
                                "Fonctionnalité à venir",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .show()
    }

    private fun deleteComment(comment: Comment) {
        if (isFinishing) return
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Supprimer le commentaire")
            .setMessage("Voulez-vous vraiment supprimer ce commentaire ?")
            .setPositiveButton("Supprimer") { _, _ ->
                if (isFinishing) return@setPositiveButton
                
                lifecycleScope.launch {
                    val result = repository.deleteComment(comment._id)

                    if (isFinishing || !::binding.isInitialized) return@launch

                    result.onSuccess {
                        if (!isFinishing) {
                            Toast.makeText(
                                this@PostDetailActivity,
                                "Commentaire supprimé",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadComments()
                            // Refresh post to update comments count
                            loadPost()
                        }
                    }.onFailure { error ->
                        if (!isFinishing) {
                            Toast.makeText(
                                this@PostDetailActivity,
                                "Erreur: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}