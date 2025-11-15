package com.example.socialapp.data.models

data class Comment(
    val _id: String = "",
    val postId: String = "",
    val userId: User? = null,
    val content: String = "",
    val isAnonymous: Boolean = true,
    val createdAt: String = ""
)

data class CommentsResponse(
    val comments: List<Comment>
)

data class CreateCommentRequest(
    val postId: String,
    val content: String,
    val isAnonymous: Boolean = true
)

data class CommentResponse(
    val comment: Comment
)