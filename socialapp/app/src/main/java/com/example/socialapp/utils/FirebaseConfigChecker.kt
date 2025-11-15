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
            2. Créer ou sélectionner un projet
            3. Ajouter une app Android avec le package :
               com.example.socialapp
            4. Télécharger google-services.json
            5. Remplacer app/google-services.json
            6. Activer Authentication → Email/Password
            7. Reconstruire l'application
            
            Le fichier actuel est un placeholder et ne fonctionne pas.
        """.trimIndent()
    }
}

