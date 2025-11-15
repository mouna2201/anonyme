# Configuration Firebase Admin SDK pour le Backend

## 📋 Vue d'ensemble

Le backend utilise Firebase Admin SDK pour vérifier les tokens d'authentification Firebase générés par l'application Android. Ce fichier explique comment configurer Firebase Admin SDK.

## 🔧 Étapes de configuration

### 1. Accéder à Firebase Console

1. Allez sur [Firebase Console](https://console.firebase.google.com/)
2. Sélectionnez votre projet Firebase (`socialapp-28101`)

### 2. Générer une clé de compte de service

1. Dans Firebase Console, cliquez sur l'icône **⚙️ Paramètres du projet** (en haut à gauche)
2. Allez dans l'onglet **"Comptes de service"**
3. Cliquez sur **"Générer une nouvelle clé privée"**
4. Une boîte de dialogue s'affichera - cliquez sur **"Générer la clé"**
5. Un fichier JSON sera téléchargé (par exemple: `socialapp-28101-firebase-adminsdk-xxxxx.json`)

### 3. Placer le fichier dans le projet

1. Renommez le fichier téléchargé en `firebase-admin.json`
2. Placez-le dans le dossier `backend_anonyme/config/`
3. Le chemin final devrait être : `backend_anonyme/config/firebase-admin.json`

### 4. Structure du fichier

Le fichier `firebase-admin.json` devrait ressembler à ceci :

```json
{
  "type": "service_account",
  "project_id": "socialapp-28101",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xxxxx@socialapp-28101.iam.gserviceaccount.com",
  "client_id": "...",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "..."
}
```

## ⚠️ Sécurité importante

- **NE COMMITEZ JAMAIS** le fichier `firebase-admin.json` dans Git
- Ajoutez-le au `.gitignore` si ce n'est pas déjà fait
- Ce fichier contient des credentials sensibles qui donnent un accès complet à votre projet Firebase

## ✅ Vérification

Une fois le fichier en place, redémarrez le serveur backend. Vous devriez voir dans les logs :

```
✅ Firebase Admin initialisé avec succès
```

Si vous voyez une erreur, vérifiez que :
- Le fichier existe bien dans `backend_anonyme/config/firebase-admin.json`
- Le fichier est un JSON valide
- Le fichier contient toutes les clés nécessaires (type, project_id, private_key, client_email, etc.)

## 🔍 Dépannage

### Erreur : "Fichier firebase-admin.json introuvable"
- Vérifiez que le fichier est dans `backend_anonyme/config/`
- Vérifiez l'orthographe du nom de fichier (doit être exactement `firebase-admin.json`)

### Erreur : "Invalid credential"
- Vérifiez que le fichier JSON est valide
- Vérifiez que vous avez téléchargé la clé depuis le bon projet Firebase
- Régénérez une nouvelle clé si nécessaire

### Erreur : "Permission denied"
- Vérifiez les permissions du fichier
- Assurez-vous que le serveur Node.js peut lire le fichier

