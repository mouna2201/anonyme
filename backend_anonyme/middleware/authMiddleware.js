const admin = require('firebase-admin');
const User = require('../models/User');

// Initialiser Firebase Admin
try {
    const serviceAccount = require('../config/firebase-admin.json');
    
    if (!admin.apps.length) {
        admin.initializeApp({
            credential: admin.credential.cert(serviceAccount)
        });
        console.log('✅ Firebase Admin initialisé avec succès');
    }
} catch (error) {
    console.error('❌ Erreur d\'initialisation Firebase Admin:', error.message);
    console.error('⚠️  Assurez-vous que firebase-admin.json existe dans le dossier config/');
}

const verifyToken = async (req, res, next) => {
    try {
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