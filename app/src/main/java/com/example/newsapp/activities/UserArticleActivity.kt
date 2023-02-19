package com.example.newsapp.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.example.newsapp.fragments.CommentSectionFragment
import com.example.newsapp.items.UserArticle
import com.example.newsapp.managers.UserArticleDataRequestManager
import com.squareup.picasso.Picasso

/**
 * Activity that displays the article, written by a Fast News user
 */
class UserArticleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_article)

        val title = findViewById<TextView>(R.id.user_article_title)
        val source = findViewById<TextView>(R.id.user_article_source)
        val date = findViewById<TextView>(R.id.user_date_published)
        val coverImage = findViewById<ImageView>(R.id.user_cover_image)
        val content = findViewById<TextView>(R.id.user_content)

        inflateActivity(title, source, date, coverImage, content)
    }

    /**
     * Inflate the activity with the user article information
     */
    private fun inflateActivity(title: TextView, source: TextView, date: TextView, coverImage: ImageView, content: TextView) {
        val userId = intent.getStringExtra("user_id")
        val articleId = intent.getStringExtra("article_id")

        val userArticles = userId?.let { UserArticleDataRequestManager(it).requestData() }

        // Search for the article
        var article: UserArticle? = null
        if (userArticles != null) {
            for (userArticle in userArticles) {
                if (userArticle.articleId == articleId) {
                    article = userArticle
                }
            }
        }

        // Inflate the article values
        if (article != null) {
            title.text = article.title
            source.text = article.username
            date.text = article.articleId
            Picasso.get().load(article.articleImage).into(coverImage)
            content.text = article.content

            // Inflate the fragment, containing the comment section of the user article
            val commentSection = CommentSectionFragment(articleId = article.articleId)

            supportFragmentManager
                .beginTransaction()
                .add(R.id.comment_section_fragment, commentSection)
                .commit()
        } else {
            finish()
        }
    }
}
