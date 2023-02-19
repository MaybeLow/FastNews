package com.example.newsapp.items

import android.net.Uri

/**
 * Data class that stores information about an official news article
 */
data class UserArticle (
    var articleId: String,
    var content: String,
    var userId: String,
    var articleImage: Uri?,
    var comments: MutableList<String>?,
    var title: String,
    var description: String,
    var username: String
)