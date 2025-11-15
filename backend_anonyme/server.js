const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const dotenv = require('dotenv');
const bodyParser = require('body-parser');

// Charger les variables d'environnement
dotenv.config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));
app.use(bodyParser.json());

// Routes
const authRoutes = require('./routes/auth');
const postRoutes = require('./routes/posts');
const commentRoutes = require('./routes/comments');

app.use('/api/auth', authRoutes);
app.use('/api/posts', postRoutes);
app.use('/api/comments', commentRoutes);

// Route de test
app.get('/', (req, res) => {
    res.json({ 
        message: '🚀 API Social App fonctionne!',
        version: '1.0.0',
        endpoints: {
            auth: '/api/auth',
            posts: '/api/posts',
            comments: '/api/comments'
        }
    });
});

// Route de santé
app.get('/health', (req, res) => {
    res.json({ 
        status: 'OK',
        mongodb: mongoose.connection.readyState === 1 ? 'Connected' : 'Disconnected',
        timestamp: new Date().toISOString()
    });
});

// Gestion des erreurs 404
app.use((req, res) => {
    res.status(404).json({ 
        error: 'Route non trouvée',
        path: req.path 
    });
});

// Gestion globale des erreurs
app.use((err, req, res, next) => {
    console.error('Erreur serveur:', err);
    res.status(500).json({ 
        error: 'Erreur interne du serveur',
        message: process.env.NODE_ENV === 'development' ? err.message : undefined
    });
});

// Connexion à MongoDB
mongoose.connect(process.env.MONGODB_URI)
.then(() => {
    console.log('✅ MongoDB connecté avec succès');
    console.log('📊 Base de données:', mongoose.connection.name);
})
.catch(err => {
    console.error('❌ Erreur de connexion MongoDB:', err.message);
    process.exit(1);
});

// Gestion des événements MongoDB
mongoose.connection.on('disconnected', () => {
    console.log('⚠️  MongoDB déconnecté');
});

mongoose.connection.on('error', (err) => {
    console.error('❌ Erreur MongoDB:', err);
});

// Démarrage du serveur
const PORT = process.env.PORT || 3000;
const server = app.listen(PORT, () => {
    console.log('═══════════════════════════════════════');
    console.log(`🚀 Serveur démarré avec succès`);
    console.log(`📡 Port: ${PORT}`);
    console.log(`🌍 URL: http://localhost:${PORT}`);
    console.log(`📝 Documentation: http://localhost:${PORT}/`);
    console.log(`💚 Santé: http://localhost:${PORT}/health`);
    console.log('═══════════════════════════════════════');
});

// Gestion de l'arrêt gracieux
process.on('SIGTERM', () => {
    console.log('⚠️  SIGTERM reçu, arrêt gracieux...');
    server.close(() => {
        console.log('✅ Serveur fermé');
        mongoose.connection.close(false)
            .then(() => {
                console.log('✅ Connexion MongoDB fermée');
                process.exit(0);
            })
            .catch(err => {
                console.error('❌ Erreur fermeture MongoDB:', err);
                process.exit(1);
            });
    });
});

process.on('SIGINT', () => {
    console.log('⚠️  SIGINT reçu, arrêt gracieux...');
    server.close(() => {
        console.log('✅ Serveur fermé');
        mongoose.connection.close(false)
            .then(() => {
                console.log('✅ Connexion MongoDB fermée');
                process.exit(0);
            })
            .catch(err => {
                console.error('❌ Erreur fermeture MongoDB:', err);
                process.exit(1);
            });
    });
});

module.exports = app;