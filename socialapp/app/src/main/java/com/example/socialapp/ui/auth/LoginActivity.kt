package com.example.socialapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.socialapp.databinding.ActivityLoginBinding
import com.example.socialapp.data.repository.SocialRepository
import com.example.socialapp.ui.main.MainActivity
import com.example.socialapp.utils.FirebaseConfigChecker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repository = SocialRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (repository.getCurrentUser() != null) {
            navigateToMain()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email requis"
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
        return true
    }

    private fun loginUser(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            val result = repository.signInWithEmail(email, password)

            result.onSuccess {
                // Récupérer le profil utilisateur depuis MongoDB
                val profileResult = repository.getUserProfile()

                profileResult.onSuccess { user ->
                    if (!isFinishing) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Bienvenue ${user.displayName}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToMain()
                    }
                }.onFailure { error ->
                    if (!isFinishing) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Erreur: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                }
            }.onFailure { error ->
                if (!isFinishing) {
                    val errorMessage = error.message ?: "Erreur inconnue"
                    
                    // Si c'est une erreur de configuration Firebase, afficher une dialog détaillée
                    if (errorMessage.contains("API key", ignoreCase = true) || 
                        errorMessage.contains("Configuration Firebase", ignoreCase = true) ||
                        !FirebaseConfigChecker.isFirebaseConfigured()) {
                        
                        MaterialAlertDialogBuilder(this@LoginActivity)
                            .setTitle("⚠️ Configuration Firebase requise")
                            .setMessage(FirebaseConfigChecker.getConfigurationHelpMessage())
                            .setPositiveButton("Compris", null)
                            .setCancelable(true)
                            .show()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Erreur de connexion: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
