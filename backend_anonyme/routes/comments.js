const express = require('express');
const router = express.Router();
const Comment = require('../models/Comment');
const Post = require('../models/Post');
const { verifyToken } = require('../middleware/authMiddleware');

// Ajouter un commentaire
router.post('/', verifyToken, async (req, res) => {
    try {
        const { postId, content, isAnonymous } = req.body;

        // Validation
        if (!postId) {
            return res.status(400).json({ 
                error: 'Post ID requis' 
            });
        }

        if (!content || content.trim().length === 0) {
            return res.status(400).json({ 
                error: 'Le contenu est requis' 
            });
        }

        const post = await Post.findById(postId);
        if (!post) {
            return res.status(404).json({ 
                error: 'Post non trouvé' 
            });
        }

        const comment = new Comment({
            postId,
            userId: req.userId,
            content: content.trim(),
            isAnonymous: isAnonymous !== undefined ? isAnonymous : true
        });

        await comment.save();
        
        // Incrémenter le compteur de commentaires
        post.commentsCount += 1;
        await post.save();

        const populatedComment = await Comment.findById(comment._id)
            .populate('userId', 'username displayName profilePicture');
        
        console.log('✅ Commentaire ajouté par:', req.userDoc.username);
        
        res.status(201).json({ 
            comment: populatedComment,
            message: 'Commentaire ajouté avec succès'
        });
    } catch (error) {
        console.error('Erreur création commentaire:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Récupérer les commentaires d'un post
router.get('/:postId', verifyToken, async (req, res) => {
    try {
        const comments = await Comment.find({ postId: req.params.postId })
            .populate('userId', 'username displayName profilePicture')
            .sort({ createdAt: -1 });

        res.json({ comments });
    } catch (error) {
        console.error('Erreur récupération commentaires:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

// Supprimer un commentaire
router.delete('/:commentId', verifyToken, async (req, res) => {
    try {
        const comment = await Comment.findById(req.params.commentId);
        
        if (!comment) {
            return res.status(404).json({ 
                error: 'Commentaire non trouvé' 
            });
        }

        // Vérifier que l'utilisateur est le propriétaire du commentaire
        if (comment.userId.toString() !== req.userId.toString()) {
            return res.status(403).json({ 
                error: 'Non autorisé',
                message: 'Vous ne pouvez supprimer que vos propres commentaires'
            });
        }

        const post = await Post.findById(comment.postId);
        if (post) {
            post.commentsCount = Math.max(0, post.commentsCount - 1);
            await post.save();
        }

        await comment.deleteOne();
        
        console.log('✅ Commentaire supprimé par:', req.userDoc.username);
        
        res.json({ message: 'Commentaire supprimé avec succès' });
    } catch (error) {
        console.error('Erreur suppression commentaire:', error);
        res.status(500).json({ 
            error: 'Erreur serveur',
            message: error.message 
        });
    }
});

module.exports = router;