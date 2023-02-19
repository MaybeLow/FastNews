package com.example.newsapp.data

/**
 * A profile information
 */
data class Profile (
    var username: String,
    var profilePicture: String,
    var description: String,
    var friends: MutableList<String>,
    var userId: String
    )