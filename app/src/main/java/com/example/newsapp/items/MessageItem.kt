package com.example.newsapp.items

/**
 * Class that stores data about a single message, sent to another user
 */
data class MessageItem (
    var message: String,
    var time: String,
    var userId: String,
    )
{
    constructor() : this("", "", "")
}