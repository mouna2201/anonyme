const mongoose = require('mongoose');

const postSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: [true, 'User ID requis']
    },
    content: {
        type: String,
        required: [true, 'Le contenu est requis'],
        trim: true,
        minlength: [1, 'Le contenu doit contenir au moins 1 caractère'],
        maxlength: [500, 'Le contenu ne peut pas dépasser 500 caractères']
    },
    imageUrl: {
        type: String,
        default: ''
    },
    likes: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User'
    }],
    likesCount: {
        type: Number,
        default: 0
    },
    commentsCount: {
        type: Number,
        default: 0
    },
    isAnonymous: {
        type: Boolean,
        default: true
    },
    createdAt: {
        type: Date,
        default: Date.now
    },
    updatedAt: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// Index pour améliorer les performances
postSchema.index({ userId: 1 });
postSchema.index({ createdAt: -1 });
postSchema.index({ likesCount: -1 });

// Middleware pour mettre à jour likesCount avant la sauvegarde
postSchema.pre('save', function(next) {
    this.likesCount = this.likes.length;
    next();
});

module.exports = mongoose.model('Post', postSchema);