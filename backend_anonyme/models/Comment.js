const mongoose = require('mongoose');

const commentSchema = new mongoose.Schema({
    postId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Post',
        required: [true, 'Post ID requis']
    },
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: [true, 'User ID requis']
    },
    content: {
        type: String,
        required: [true, 'Le contenu est requis'],
        trim: true,
        minlength: [1, 'Le commentaire doit contenir au moins 1 caractère'],
        maxlength: [300, 'Le commentaire ne peut pas dépasser 300 caractères']
    },
    isAnonymous: {
        type: Boolean,
        default: true
    },
    createdAt: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// Index pour améliorer les performances
commentSchema.index({ postId: 1 });
commentSchema.index({ userId: 1 });
commentSchema.index({ createdAt: -1 });

module.exports = mongoose.model('Comment', commentSchema);