const express = require('express');
const router = express.Router();
const Post = require('../models/Post');
const { verifyToken } = require('../middleware/authMiddleware');

// Créer un post
router.post('/', verifyToken, async (req, res) => {
    try {
        const { content, imageUrl, isAnonymous } = req.body;

        // Validation
        if (!content || content.trim().length === 0) {
            return res.status(400).json({ 
                error: 'Le contenu est requis' 
            });
        }

        const post = new Post({
            userId: req.userId,
            content: content.trim(),
            imageUrl: imageUrl || '',
            isAnonymous: isAnonymous !== undefined ? isAnonymous : true
        });

        await post.save();
        
        const populatedPost = await Post.findById(post._id)
            .populate('userId', 'username displayName profilePicture');
        
        console.log('✅ Post créé par:', req.userDoc.username);
        
        res.status(201).json({ 
            post: populatedPost,
            message: 'Post créé avec succès' 
        });
    } catch (error) {
        console.error('Erreur création post:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Récupérer tous les posts (avec pagination)
router.get('/', verifyToken, async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 20;
        const skip = (page - 1) * limit;

        const posts = await Post.find()
            .populate('userId', 'username displayName profilePicture')
            .sort({ createdAt: -1 })
            .skip(skip)
            .limit(limit)
            .lean();

        const total = await Post.countDocuments();

        res.json({ 
            posts, 
            pagination: {
                page,
                limit,
                total,
                pages: Math.ceil(total / limit)
            }
        });
    } catch (error) {
        console.error('Erreur récupération posts:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Récupérer un post spécifique
router.get('/:postId', verifyToken, async (req, res) => {
    try {
        const post = await Post.findById(req.params.postId)
            .populate('userId', 'username displayName profilePicture');
        
        if (!post) {
            return res.status(404).json({ 
                error: 'Post non trouvé' 
            });
        }

        res.json({ post });
    } catch (error) {
        console.error('Erreur récupération post:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Liker/Unliker un post
router.post('/:postId/like', verifyToken, async (req, res) => {
    try {
        const post = await Post.findById(req.params.postId);
        
        if (!post) {
            return res.status(404).json({ 
                error: 'Post non trouvé' 
            });
        }

        const likeIndex = post.likes.indexOf(req.userId);
        
        if (likeIndex > -1) {
            // Retirer le like
            post.likes.splice(likeIndex, 1);
            post.likesCount = post.likes.length;
        } else {
            // Ajouter le like
            post.likes.push(req.userId);
            post.likesCount = post.likes.length;
        }

        await post.save();
        
        const populatedPost = await Post.findById(post._id)
            .populate('userId', 'username displayName profilePicture');

        res.json({ 
            post: populatedPost, 
            liked: likeIndex === -1 
        });
    } catch (error) {
        console.error('Erreur like post:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Supprimer un post
router.delete('/:postId', verifyToken, async (req, res) => {
    try {
        const post = await Post.findById(req.params.postId);
        
        if (!post) {
            return res.status(404).json({ 
                error: 'Post non trouvé' 
            });
        }

        // Vérifier que l'utilisateur est le propriétaire du post
        if (post.userId.toString() !== req.userId.toString()) {
            return res.status(403).json({ 
                error: 'Non autorisé',
                message: 'Vous ne pouvez supprimer que vos propres posts'
            });
        }

        await post.deleteOne();
        
        console.log('✅ Post supprimé par:', req.userDoc.username);
        
        res.json({ message: 'Post supprimé avec succès' });
    } catch (error) {
        console.error('Erreur suppression post:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Récupérer les posts d'un utilisateur
router.get('/user/:userId', verifyToken, async (req, res) => {
    try {
        const posts = await Post.find({ userId: req.params.userId })
            .populate('userId', 'username displayName profilePicture')
            .sort({ createdAt: -1 });

        res.json({ posts });
    } catch (error) {
        console.error('Erreur récupération posts utilisateur:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

module.exports = router;