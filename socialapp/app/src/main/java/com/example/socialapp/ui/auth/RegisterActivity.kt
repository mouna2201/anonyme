package com.example.socialapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.socialapp.databinding.ActivityRegisterBinding
import com.example.socialapp.data.repository.SocialRepository
import com.example.socialapp.ui.main.MainActivity
import com.example.socialapp.utils.FirebaseConfigChecker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val repository = SocialRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Vérifier la configuration Firebase au démarrage
        checkFirebaseConfiguration()
        
        setupListeners()
    }
    
    private fun checkFirebaseConfiguration() {
        if (!FirebaseConfigChecker.isFirebaseConfigured()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Configuration Firebase requise")
                .setMessage(FirebaseConfigChecker.getConfigurationHelpMessage())
                .setPositiveButton("Compris", null)
                .setCancelable(true)
                .show()
        } else {
            // Firebase est correctement configuré - pas besoin d'afficher de message
            // L'application est prête à fonctionner
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val displayName = binding.etDisplayName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(username, email, password, confirmPassword)) {
                registerUser(username, displayName, email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        if (username.isEmpty()) {
            binding.tilUsername.error = "Nom d'utilisateur requis"
            return false
        }
        if (username.length < 3) {
            binding.tilUsername.error = "Au moins 3 caractères"
            return false
        }
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email requis"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email invalide"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Mot de passe requis"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Au moins 6 caractères"
            return false
        }
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Les mots de passe ne correspondent pas"
            return false
        }
        return true
    }

    private fun registerUser(username: String, displayName: String, email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            // 1. Créer le compte Firebase
            val firebaseResult = repository.signUpWithEmail(email, password)

            firebaseResult.onSuccess {
                // 2. Enregistrer dans MongoDB
                val finalDisplayName = displayName.ifEmpty { username }
                val backendResult = repository.registerUserInBackend(username, finalDisplayName, email)

                backendResult.onSuccess { user ->
                    Toast.makeText(
                        this@RegisterActivity,
                        "Inscription réussie! Bienvenue ${user.displayName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMain()
                }.onFailure { error ->
                    // Afficher un message d'erreur plus détaillé
                    val errorMessage = error.message ?: "Erreur inconnue"
                    Toast.makeText(
                        this@RegisterActivity,
                        "Erreur lors de l'enregistrement: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    
                    // Optionnel: déconnecter l'utilisateur Firebase si l'enregistrement backend échoue
                    // repository.signOut()
                }
            }.onFailure { error ->
                val errorMessage = error.message ?: "Erreur inconnue"
                
                // Si c'est une erreur de configuration Firebase, afficher une dialog détaillée
                if (errorMessage.contains("API key", ignoreCase = true) || 
                    errorMessage.contains("Configuration Firebase", ignoreCase = true) ||
                    !FirebaseConfigChecker.isFirebaseConfigured()) {
                    
                    MaterialAlertDialogBuilder(this@RegisterActivity)
                        .setTitle("⚠️ Configuration Firebase requise")
                        .setMessage(FirebaseConfigChecker.getConfigurationHelpMessage())
                        .setPositiveButton("Compris", null)
                        .setCancelable(true)
                        .show()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Erreur d'inscription: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
            }
        }
    }

    // CORRECTION: Ajout de la fonction manquante
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}