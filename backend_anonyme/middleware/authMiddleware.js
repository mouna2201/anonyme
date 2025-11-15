const admin = require('firebase-admin');
const User = require('../models/User');
const fs = require('fs');
const path = require('path');

// Initialiser Firebase Admin
let firebaseAdminInitialized = false;

try {
    const configPath = path.join(__dirname, '../config/firebase-admin.json');
    
    // Vérifier si le fichier existe
    if (fs.existsSync(configPath)) {
        const serviceAccount = require(configPath);
        
        // Valider la structure du fichier service account
        if (!serviceAccount.project_id) {
            console.error('❌ Erreur: firebase-admin.json ne contient pas "project_id"');
            console.error('⚠️  Le fichier doit être un service account key JSON téléchargé depuis Firebase Console');
            console.error('⚠️  Ne pas utiliser google-services.json - ce sont deux fichiers différents!');
            console.error('📖 Consultez config/FIREBASE_ADMIN_SETUP.md pour les instructions');
        } else if (!serviceAccount.private_key || !serviceAccount.client_email) {
            console.error('❌ Erreur: firebase-admin.json est incomplet');
            console.error('⚠️  Le fichier doit contenir: project_id, private_key, et client_email');
            console.error('📖 Consultez config/FIREBASE_ADMIN_SETUP.md pour les instructions');
        } else {
            if (!admin.apps.length) {
                admin.initializeApp({
                    credential: admin.credential.cert(serviceAccount)
                });
                firebaseAdminInitialized = true;
                console.log('✅ Firebase Admin initialisé avec succès');
                console.log(`📦 Projet: ${serviceAccount.project_id}`);
            } else {
                firebaseAdminInitialized = true;
            }
        }
    } else {
        console.error('❌ Fichier firebase-admin.json introuvable dans config/');
        console.error('⚠️  Veuillez créer le fichier config/firebase-admin.json avec vos credentials Firebase Admin');
        console.error('📖 Consultez config/FIREBASE_ADMIN_SETUP.md pour les instructions');
    }
} catch (error) {
    console.error('❌ Erreur d\'initialisation Firebase Admin:', error.message);
    if (error.code === 'MODULE_NOT_FOUND') {
        console.error('⚠️  Assurez-vous que firebase-admin.json existe dans le dossier config/');
        console.error('📖 Consultez config/FIREBASE_ADMIN_SETUP.md pour les instructions');
    } else if (error.message.includes('project_id')) {
        console.error('⚠️  Le fichier firebase-admin.json semble être au mauvais format');
        console.error('⚠️  Assurez-vous d\'utiliser un Service Account Key JSON, pas google-services.json');
        console.error('📖 Consultez config/FIREBASE_ADMIN_SETUP.md pour les instructions');
    }
}

const verifyToken = async (req, res, next) => {
    try {
        // Vérifier si Firebase Admin est initialisé
        if (!firebaseAdminInitialized || !admin.apps.length) {
            return res.status(500).json({ 
                error: 'Configuration Firebase manquante',
                message: 'Firebase Admin n\'est pas configuré. Veuillez configurer firebase-admin.json dans le dossier config/'
            });
        }

        // Récupérer le token depuis le header Authorization
        const authHeader = req.headers.authorization;
        
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({ 
                error: 'Token manquant',
                message: 'Veuillez fournir un token d\'authentification'
            });
        }

        const token = authHeader.split('Bearer ')[1];

        if (!token) {
            return res.status(401).json({ 
                error: 'Token invalide',
                message: 'Format du token incorrect'
            });
        }

        // Vérifier le token avec Firebase
        const decodedToken = await admin.auth().verifyIdToken(token);
        req.user = decodedToken;
        
        // Récupérer l'utilisateur depuis MongoDB
        const user = await User.findOne({ firebaseUid: decodedToken.uid });
        
        if (!user) {
            return res.status(404).json({ 
                error: 'Utilisateur non trouvé',
                message: 'Aucun utilisateur correspondant dans la base de données'
            });
        }
        
        req.userId = user._id;
        req.userDoc = user;
        
        next();
    } catch (error) {
        console.error('Erreur de vérification du token:', error.message);
        
        if (error.code === 'auth/id-token-expired') {
            return res.status(401).json({ 
                error: 'Token expiré',
                message: 'Veuillez vous reconnecter'
            });
        }
        
        if (error.code === 'auth/argument-error') {
            return res.status(401).json({ 
                error: 'Token invalide',
                message: 'Le token fourni est invalide'
            });
        }
        
        return res.status(401).json({ 
            error: 'Authentification échouée',
            message: error.message
        });
    }
};

module.exports = { verifyToken };