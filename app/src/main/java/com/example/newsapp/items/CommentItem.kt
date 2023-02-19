package com.example.newsapp.items

/**
 * Class that stores data of a single comment under a user article
 */
data class CommentItem (
    var comment: String,
    var time: String,
    var userId: String,
    var articleId: String,
    var username: String
)
{
    constructor() : this("", "", "", "", "")
}