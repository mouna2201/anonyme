package com.example.socialapp.data.models

data class User(
    val _id: String = "",
    val firebaseUid: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "Anonymous",
    val profilePicture: String = "",
    val bio: String = "",
    val createdAt: String = ""
)

data class AuthResponse(
    val user: User,
    val message: String
)

data class RegisterRequest(
    val username: String,
    val displayName: String,
    val email: String
)