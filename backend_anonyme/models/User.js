const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    firebaseUid: {
        type: String,
        required: [true, 'Firebase UID requis'],
        unique: true,
        trim: true
    },
    email: {
        type: String,
        required: [true, 'Email requis'],
        unique: true,
        lowercase: true,
        trim: true,
        match: [/^\S+@\S+\.\S+$/, 'Email invalide']
    },
    username: {
        type: String,
        required: [true, 'Nom d\'utilisateur requis'],
        unique: true,
        trim: true,
        minlength: [3, 'Le nom d\'utilisateur doit contenir au moins 3 caractères'],
        maxlength: [30, 'Le nom d\'utilisateur ne peut pas dépasser 30 caractères']
    },
    displayName: {
        type: String,
        default: 'Anonymous',
        trim: true,
        maxlength: [50, 'Le nom affiché ne peut pas dépasser 50 caractères']
    },
    profilePicture: {
        type: String,
        default: ''
    },
    bio: {
        type: String,
        default: '',
        maxlength: [200, 'La bio ne peut pas dépasser 200 caractères']
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
userSchema.index({ firebaseUid: 1 });
userSchema.index({ email: 1 });
userSchema.index({ username: 1 });

// Méthode pour obtenir un profil public
userSchema.methods.getPublicProfile = function() {
    return {
        _id: this._id,
        username: this.username,
        displayName: this.displayName,
        profilePicture: this.profilePicture,
        bio: this.bio,
        createdAt: this.createdAt
    };
};

module.exports = mongoose.model('User', userSchema);