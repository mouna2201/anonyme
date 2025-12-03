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

### 2. Configurer MongoDB Atlas

**Recommandé pour production :** Utilisez MongoDB Atlas (base de données cloud sécurisée).

#### Créer un cluster MongoDB Atlas :
1. allez sur [MongoDB Atlas](https://cloud.mongodb.com/)
2. Créez un compte et un nouveau projet
3. Cliquez sur "Build a Database" → "Choose a free cluster"
4. Sélectionnez votre fournisseur (AWS/Google/MongoDB)
5. Créez le cluster sans changer les options
6. Dans "Database Access" :
   - Ajoutez un utilisateur : `anonyme-user` avec mot de passe fort
   - Rôles : `Read and write to any database`
7. Dans "Network Access" :
   - Ajoutez une IP : `0.0.0.0/0` (tous les accès) ou votre IP
8. Dans "Clusters" → "Connect" :
   - Choisissez "Connect your application"
   - Copiez la connection string

### 3. Configurer les variables d'environnement

Créez un fichier `.env` à la racine du dossier `backend_anonyme` :

#### Pour MongoDB Atlas (recommandé) :
```env
# MongoDB Atlas Connection
# Remplacez par votre vraie connection string
MONGODB_URI=mongodb+srv://anonyme-user:VOTRE_MOT_DE_PASSE@cluster0.mongodb.net/anonyme-db?retryWrites=true&w=majority

# Server Port
PORT=3000

# Environment
NODE_ENV=development
```

#### Pour MongoDB local (développement seulement) :
```env
# MongoDB Local Connection
MONGODB_URI=mongodb://localhost:27017/socialapp

# Server Port
PORT=3000

# Environment
NODE_ENV=development
```

### 3. Configurer Firebase Admin SDK

Suivez les instructions dans `config/FIREBASE_ADMIN_SETUP.md` pour configurer Firebase Admin SDK.

Le fichier `config/firebase-admin.json` doit être créé avec vos credentials Firebase Admin.

## � Démarrage

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
�📡 Port: 3000
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
