# Guide de Configuration Firebase

## ⚠️ Problème actuel
Le fichier `app/google-services.json` est un placeholder et ne fonctionne pas. Vous avez déjà un projet Firebase (`applicationanonyme-5ed28`), mais vous devez télécharger le fichier `google-services.json` pour l'application Android.

## 📋 Étapes pour corriger

### 1. Accéder à Firebase Console
- Allez sur https://console.firebase.google.com/
- Connectez-vous avec votre compte Google
- Sélectionnez le projet **applicationanonyme-5ed28**

### 2. Ajouter une application Android
1. Dans votre projet Firebase, cliquez sur l'icône **⚙️ Paramètres du projet** (en haut à gauche)
2. Faites défiler jusqu'à la section **"Vos applications"**
3. Cliquez sur l'icône **Android** (ou sur **"Ajouter une application"** puis **Android**)

### 3. Configurer l'application Android
- **Nom du package Android** : `com.example.socialapp`
- Cliquez sur **"Enregistrer l'application"**

### 4. Télécharger google-services.json
1. Téléchargez le fichier `google-services.json`
2. **Remplacez** le fichier `app/google-services.json` dans votre projet par celui que vous venez de télécharger

### 5. Activer Authentication
1. Dans Firebase Console, allez dans **Authentication** (menu de gauche)
2. Cliquez sur **"Commencer"** si ce n'est pas déjà fait
3. Allez dans l'onglet **"Méthodes de connexion"**
4. Activez **"Email/Mot de passe"**
5. Cliquez sur **"Enregistrer"**

### 6. Activer Storage (pour les images)
1. Dans Firebase Console, allez dans **Storage** (menu de gauche)
2. Cliquez sur **"Commencer"** si ce n'est pas déjà fait
3. Suivez les étapes de configuration

### 7. Reconstruire l'application
```bash
./gradlew clean build
```

Ou dans Android Studio :
- **Build** → **Clean Project**
- **Build** → **Rebuild Project**

## ✅ Vérification
Après avoir remplacé le fichier et reconstruit l'application :
- L'application devrait détecter automatiquement la configuration Firebase
- Vous pourrez créer des comptes et vous connecter
- Les messages d'erreur de configuration disparaîtront

## 🔍 Vérifier que le fichier est correct
Le fichier `google-services.json` téléchargé depuis Firebase devrait contenir :
- Un `project_id` différent de "placeholder-project"
- Une `api_key` différente de "AIzaSyPlaceholderKeyForBuildOnly"
- Des informations réelles de votre projet Firebase

## ⚠️ Important
- Ne partagez jamais votre fichier `google-services.json` publiquement
- Ne commitez pas le fichier dans un dépôt public (ajoutez-le au `.gitignore` si nécessaire)
- Le fichier Admin SDK (`applicationanonyme-5ed28-firebase-adminsdk-*.json`) est différent et ne doit PAS être utilisé dans l'application Android

