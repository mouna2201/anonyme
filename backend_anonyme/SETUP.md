# Backend Setup Guide

## 📋 Prérequis

- Node.js (v14 ou supérieur)
- MongoDB (local ou MongoDB Atlas)
- Firebase Admin SDK credentials

## 🔧 Configuration

### 1. Installer les dépendances

```bash
npm install
```

### 2. Configurer les variables d'environnement

Créez un fichier `.env` à la racine du dossier `backend_anonyme` avec le contenu suivant :

```env
# MongoDB Connection
# Format: mongodb://[username:password@]host[:port][/database][?options]
# Exemple local: mongodb://localhost:27017/socialapp
# Exemple MongoDB Atlas: mongodb+srv://username:password@cluster.mongodb.net/socialapp?retryWrites=true&w=majority
MONGODB_URI=mongodb://localhost:27017/socialapp

# Server Port
PORT=3000

# Environment
NODE_ENV=development
```

### 3. Configurer Firebase Admin SDK

Suivez les instructions dans `config/FIREBASE_ADMIN_SETUP.md` pour configurer Firebase Admin SDK.

Le fichier `config/firebase-admin.json` doit être créé avec vos credentials Firebase Admin.

## 🚀 Démarrage

### Mode développement (avec auto-reload)

```bash
npm run dev
```

### Mode production

```bash
npm start
```

## ✅ Vérification

Une fois le serveur démarré, vous devriez voir :

```
✅ MongoDB connecté avec succès
✅ Firebase Admin initialisé avec succès
🚀 Serveur démarré avec succès
📡 Port: 3000
```

## 🔍 Endpoints

- `GET /` - Documentation de l'API
- `GET /health` - Vérification de l'état du serveur
- `POST /api/auth/register` - Inscription d'un utilisateur
- `GET /api/auth/profile` - Récupérer le profil utilisateur
- `GET /api/posts` - Liste des posts
- `POST /api/posts` - Créer un post
- `GET /api/comments/:postId` - Liste des commentaires d'un post
- `POST /api/comments` - Créer un commentaire

## ⚠️ Notes importantes

- Le fichier `.env` ne doit jamais être commité dans Git
- Le fichier `config/firebase-admin.json` ne doit jamais être commité dans Git
- Assurez-vous que MongoDB est en cours d'exécution avant de démarrer le serveur

