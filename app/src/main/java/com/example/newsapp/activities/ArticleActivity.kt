package com.example.newsapp.activities

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.squareup.picasso.Picasso

/**
 * Activity that displays an official article for the user
 */
class ArticleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        // Get views to display article and its information
        val title = findViewById<TextView>(R.id.article_title)
        val source = findViewById<TextView>(R.id.article_source)
        val date = findViewById<TextView>(R.id.date_published)
        val coverImage = findViewById<ImageView>(R.id.cover_image)
        val content = findViewById<TextView>(R.id.content)

        inflateActivity(title, source, date, coverImage, content)
    }

    /**
     * Inflate the activity with article information from intent values
     */
    private fun inflateActivity(title: TextView, source: TextView, date: TextView, coverImage: ImageView, content: TextView) {
        val titleText = intent.getStringExtra("title")
        if (titleText != "") {
            title.text = titleText
        }
        val sourceIdText = intent.getStringExtra("source_id")
        if (sourceIdText != "") {
            source.text = sourceIdText
        }
        val pubDateText = intent.getStringExtra("pubDate")
        if (pubDateText != "") {
            date.text = pubDateText
        }
        val imageUrl = intent.getStringExtra("image_url")
        if (imageUrl != "") {
            Picasso.get().load(imageUrl).into(coverImage)
        }

        val contentText = intent.getStringExtra("content")
        val descriptionText = intent.getStringExtra("description")
        if (contentText != null) {
            content.text = contentText
        // If the content of the article is empty, display its description instead
        } else if (descriptionText != null) {
            content.text = descriptionText
        // If both description and content of the article are empty
        } else {
            content.text = getString(R.string.default_description)
        }
    }
}
