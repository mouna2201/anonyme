package com.example.socialapp.utils

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

object FirebaseConfigChecker {
    
    /**
     * Vérifie si Firebase est correctement configuré
     * @return true si Firebase est configuré, false sinon
     */
    fun isFirebaseConfigured(): Boolean {
        return try {
            val app = FirebaseApp.getInstance()
            val projectId = app.options.projectId
            
            // Vérifier si c'est un placeholder
            projectId != null && 
            projectId != "placeholder-project" &&
            !projectId.contains("placeholder", ignoreCase = true) &&
            app.options.apiKey != null &&
            app.options.apiKey != "AIzaSyPlaceholderKeyForBuildOnly"
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtient un message d'aide pour configurer Firebase
     */
    fun getConfigurationHelpMessage(): String {
        return """
            ⚠️ Configuration Firebase requise
            
            Pour que l'application fonctionne, vous devez :
            
            1. Aller sur https://console.firebase.google.com/
            2. Sélectionner le projet : socialapp-28101
            3. Activer Authentication → Email/Password
            4. Activer Storage (pour les images)
            5. Reconstruire l'application
            
            Si le problème persiste, vérifiez que google-services.json 
            est correctement configuré dans app/google-services.json
        """.trimIndent()
    }
}

