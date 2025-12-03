# 🚨 Firebase Admin Configuration Missing

**ERROR IDENTIFIED:** The backend is missing the required `firebase-admin.json` configuration file.

## The Problem
The current `google-services (2).json` file in this directory is a **mobile client config** (for Android app), not a **service account key** (for backend server).

## Solution
To fix this error, you need to download the correct Firebase Service Account Key:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `socialapp-28101`
3. Click the ⚙️ settings icon (top left) → **"Service accounts"**
4. Click **"Generate new private key"** button
5. The JSON file will download (e.g., `socialapp-28101-firebase-adminsdk-xxxxx.json`)
6. Rename it to `firebase-admin.json`
7. Place it in `backend_anonyme/config/firebase-admin.json`
8. **IMPORTANT:** Add this file to `.gitignore` - NEVER commit it!

## Verification
Once the file is in place, the backend server will log:
```
✅ Firebase Admin initialisé avec succès
```

Without this file, authentication will fail and the backend cannot verify Firebase tokens from the Android app.
