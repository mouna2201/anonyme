package com.example.socialapp.data.models

data class Post(
    val _id: String = "",
    val userId: User? = null,
    val content: String = "",
    val imageUrl: String = "",
    val likes: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isAnonymous: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun isLikedByUser(currentUserId: String): Boolean {
        return likes.contains(currentUserId)
    }
}

data class PostsResponse(
    val posts: List<Post>,
    val pagination: Pagination
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)

data class CreatePostRequest(
    val content: String,
    val imageUrl: String = "",
    val isAnonymous: Boolean = true
)

data class PostResponse(
    val post: Post,
    val message: String? = null
)

data class LikeResponse(
    val post: Post,
    val liked: Boolean
)