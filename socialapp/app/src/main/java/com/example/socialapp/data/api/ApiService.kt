package com.example.socialapp.data.api

import com.example.socialapp.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth endpoints
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/profile")
    suspend fun getProfile(): Response<AuthResponse>

    // Posts endpoints
    @GET("posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PostsResponse>

    @GET("posts/{postId}")
    suspend fun getPost(@Path("postId") postId: String): Response<PostResponse>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostResponse>

    @POST("posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: String): Response<LikeResponse>

    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String): Response<Map<String, String>>

    // Comments endpoints
    @GET("comments/{postId}")
    suspend fun getComments(@Path("postId") postId: String): Response<CommentsResponse>

    @POST("comments")
    suspend fun createComment(@Body request: CreateCommentRequest): Response<CommentResponse>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: String): Response<Map<String, String>>
}