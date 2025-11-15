const express = require('express');
const router = express.Router();
const User = require('../models/User');
const { verifyToken } = require('../middleware/authMiddleware');

// Créer ou récupérer un utilisateur
router.post('/register', verifyToken, async (req, res) => {
    try {
        const { username, displayName, email } = req.body;
        const firebaseUid = req.user.uid;

        // Validation
        if (!username || !email) {
            return res.status(400).json({ 
                error: 'Données manquantes',
                message: 'Le nom d\'utilisateur et l\'email sont requis'
            });
        }

        // Vérifier si le nom d'utilisateur existe déjà
        const existingUsername = await User.findOne({ username: username.toLowerCase() });
        if (existingUsername && existingUsername.firebaseUid !== firebaseUid) {
            return res.status(400).json({ 
                error: 'Nom d\'utilisateur déjà pris',
                message: 'Veuillez choisir un autre nom d\'utilisateur'
            });
        }

        // Vérifier si l'utilisateur existe déjà avec cet UID Firebase
        let user = await User.findOne({ firebaseUid });

        if (user) {
            return res.json({ 
                user: user.getPublicProfile(), 
                message: 'Utilisateur existant' 
            });
        }

        // Créer un nouvel utilisateur
        user = new User({
            firebaseUid,
            email: email.toLowerCase(),
            username: username.toLowerCase(),
            displayName: displayName || username
        });

        await user.save();
        
        console.log('✅ Nouvel utilisateur créé:', username);
        
        res.status(201).json({ 
            user: user.getPublicProfile(), 
            message: 'Utilisateur créé avec succès' 
        });
    } catch (error) {
        console.error('Erreur registration:', error);
        
        if (error.code === 11000) {
            const field = Object.keys(error.keyPattern)[0];
            return res.status(400).json({ 
                error: 'Données en double',
                message: `Ce ${field} est déjà utilisé`
            });
        }
        
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Récupérer le profil utilisateur
router.get('/profile', verifyToken, async (req, res) => {
    try {
        const user = await User.findById(req.userId);
        
        if (!user) {
            return res.status(404).json({ 
                error: 'Utilisateur non trouvé' 
            });
        }
        
        res.json({ user: user.getPublicProfile() });
    } catch (error) {
        console.error('Erreur récupération profil:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Mettre à jour le profil utilisateur
router.put('/profile', verifyToken, async (req, res) => {
    try {
        const { displayName, bio, profilePicture } = req.body;
        
        const user = await User.findById(req.userId);
        
        if (!user) {
            return res.status(404).json({ 
                error: 'Utilisateur non trouvé' 
            });
        }
        
        if (displayName) user.displayName = displayName;
        if (bio !== undefined) user.bio = bio;
        if (profilePicture !== undefined) user.profilePicture = profilePicture;
        
        await user.save();
        
        res.json({ 
            user: user.getPublicProfile(),
            message: 'Profil mis à jour avec succès' 
        });
    } catch (error) {
        console.error('Erreur mise à jour profil:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

module.exports = router;