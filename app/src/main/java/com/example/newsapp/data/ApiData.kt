package com.example.newsapp.data

/**
 * The data that receives news api calls
 */
data class ApiData (
    var status: String,
    var totalResults: Int,
    var results: MutableList<Headline>
)