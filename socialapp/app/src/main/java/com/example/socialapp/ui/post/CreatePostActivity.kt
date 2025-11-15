package com.example.socialapp.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.socialapp.databinding.ActivityCreatePostBinding
import com.example.socialapp.data.repository.SocialRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private val repository = SocialRepository()
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String = ""

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showImagePreview(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            uploadedImageUrl = ""
            binding.ivPreview.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
            binding.btnAddImage.visibility = View.VISIBLE
        }

        binding.btnPost.setOnClickListener {
            createPost()
        }
    }

    private fun showImagePreview(uri: Uri) {
        binding.ivPreview.visibility = View.VISIBLE
        binding.btnRemoveImage.visibility = View.VISIBLE
        binding.btnAddImage.visibility = View.GONE

        Glide.with(this)
            .load(uri)
            .into(binding.ivPreview)
    }

    private fun createPost() {
        val content = binding.etContent.text.toString().trim()

        if (content.isEmpty()) {
            binding.tilContent.error = "Le contenu ne peut pas être vide"
            return
        }

        if (content.length < 3) {
            binding.tilContent.error = "Le contenu doit contenir au moins 3 caractères"
            return
        }

        binding.tilContent.error = null
        binding.btnPost.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            // Upload image si nécessaire
            if (selectedImageUri != null) {
                val uploadResult = uploadImage(selectedImageUri!!)
                if (uploadResult == null) {
                    if (!isFinishing) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnPost.isEnabled = true
                        Toast.makeText(
                            this@CreatePostActivity,
                            "Erreur lors de l'upload de l'image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }
                uploadedImageUrl = uploadResult
            }

            if (isFinishing) return@launch

            // Créer le post
            val isAnonymous = binding.switchAnonymous.isChecked
            val result = repository.createPost(content, uploadedImageUrl, isAnonymous)

            if (isFinishing) return@launch

            binding.progressBar.visibility = View.GONE
            binding.btnPost.isEnabled = true

            result.onSuccess {
                if (!isFinishing) {
                    Toast.makeText(
                        this@CreatePostActivity,
                        "Post publié avec succès!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }.onFailure { error ->
                if (!isFinishing) {
                    Toast.makeText(
                        this@CreatePostActivity,
                        "Erreur: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("posts/${UUID.randomUUID()}.jpg")

            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}