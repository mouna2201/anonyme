package com.example.socialapp.data.repository

import com.example.socialapp.data.api.RetrofitClient
import com.example.socialapp.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class SocialRepository {

    private val apiService = RetrofitClient.apiService
    private val auth = FirebaseAuth.getInstance()

    // ==================== Authentication ====================

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Échec de la connexion"))
        } catch (e: Exception) {
            // Améliorer les messages d'erreur Firebase
            val errorMessage = when {
                e.message?.contains("API key", ignoreCase = true) == true -> {
                    "Configuration Firebase invalide. Veuillez configurer google-services.json avec votre projet Firebase."
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    "Erreur de connexion. Vérifiez votre connexion internet."
                }
                e.message?.contains("invalid", ignoreCase = true) == true && 
                e.message?.contains("credential", ignoreCase = true) == true -> {
                    "Email ou mot de passe incorrect."
                }
                e.message?.contains("user", ignoreCase = true) == true && 
                e.message?.contains("not found", ignoreCase = true) == true -> {
                    "Aucun compte trouvé avec cet email."
                }
                else -> e.message ?: "Erreur lors de la connexion"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Échec de l'inscription"))
        } catch (e: Exception) {
            // Améliorer les messages d'erreur Firebase
            val errorMessage = when {
                e.message?.contains("API key", ignoreCase = true) == true -> {
                    "Configuration Firebase invalide. Veuillez configurer google-services.json avec votre projet Firebase."
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    "Erreur de connexion. Vérifiez votre connexion internet."
                }
                e.message?.contains("email", ignoreCase = true) == true -> {
                    "Email invalide ou déjà utilisé."
                }
                e.message?.contains("password", ignoreCase = true) == true -> {
                    "Mot de passe trop faible. Utilisez au moins 6 caractères."
                }
                else -> e.message ?: "Erreur lors de l'inscription"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun registerUserInBackend(username: String, displayName: String, email: String): Result<User> {
        return try {
            // Forcer l'obtention d'un nouveau token après la création du compte Firebase
            val user = auth.currentUser
            if (user != null) {
                // Obtenir un nouveau token (forceRefresh = true)
                val tokenResult = user.getIdToken(true).await()
                // Le token sera automatiquement ajouté par l'intercepteur
            }
            
            val response = apiService.register(RegisterRequest(username, displayName, email))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.user)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message()
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur lors de l'enregistrement: ${e.message}"))
        }
    }

    suspend fun getUserProfile(): Result<User> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.user)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // ==================== Posts ====================

    suspend fun getPost(postId: String): Result<Post> {
        return try {
            val response = apiService.getPost(postId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.post)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }

    suspend fun getPosts(page: Int = 1, limit: Int = 20): Result<PostsResponse> {
        return try {
            val response = apiService.getPosts(page, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }

    suspend fun createPost(content: String, imageUrl: String = "", isAnonymous: Boolean = true): Result<Post> {
        return try {
            val response = apiService.createPost(CreatePostRequest(content, imageUrl, isAnonymous))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.post)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }

    suspend fun likePost(postId: String): Result<LikeResponse> {
        return try {
            val response = apiService.likePost(postId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }

    suspend fun deletePost(postId: String): Result<String> {
        return try {
            val response = apiService.deletePost(postId)
            if (response.isSuccessful) {
                Result.success("Post supprimé")
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }

    // ==================== Comments ====================

    suspend fun getComments(postId: String): Result<List<Comment>> {
        return try {
            val response = apiService.getComments(postId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.comments)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }

    suspend fun createComment(postId: String, content: String, isAnonymous: Boolean = true): Result<Comment> {
        return try {
            val response = apiService.createComment(CreateCommentRequest(postId, content, isAnonymous))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.comment)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }

    suspend fun deleteComment(commentId: String): Result<String> {
        return try {
            val response = apiService.deleteComment(commentId)
            if (response.isSuccessful) {
                Result.success("Commentaire supprimé")
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: response.message() ?: "Erreur inconnue"
                Result.failure(Exception("Erreur serveur: $errorMessage (Code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau: ${e.message ?: "Connexion impossible"}"))
        }
    }
}