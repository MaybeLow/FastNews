package com.example.newsapp.data

/**
 * The news headline data for the official news articles
 */
data class Headline (
    var title: String,
    var link: String,
    var keywords: MutableList<String>,
    var creator: MutableList<String>,
    var video_url: String,
    var description: String,
    var content: String,
    var pubDate: String,
    var image_url: String,
    var source_id: String,
    var country: MutableList<String>,
    var category: MutableList<String>,
    var language: String,
    var sourceImageUrl: String,
)