package com.example.newsapp.managers

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.items.UserArticle
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Data request manager that accesses Firebase database and makes a user news api call
 * The class has the current user id as the default configuration
 */
class UserArticleDataRequestManager(val userId: String = Firebase.auth.currentUser!!.uid, private val db: FirebaseFirestore = Firebase.firestore) : AppCompatActivity() {
    /**
     * Method that starts a coroutine that requests data from the database
     * about articles, written by a user
     */
    fun requestData(): MutableList<UserArticle> {
        var userArticles: MutableList<UserArticle>? = null
        GlobalScope.launch {
            userArticles = requestUserArticleData()
        }

        while (userArticles == null) {
            Thread.sleep(10)
            Log.w(ContentValues.TAG, "Waiting for user article call")
        }

        return userArticles as MutableList<UserArticle>
    }

    /**
     * Method that requests the user article information from the firebase database
     */
    private suspend fun requestUserArticleData(): MutableList<UserArticle> {
        val userArticles = mutableListOf<UserArticle>()
        val userDatabase = db.collection("user").document(userId).get()
        // Wait for the database call
        userDatabase.await()

        // Get the list of articles, written by the user
        if (userDatabase.result != null) {
            val writtenArticles = userDatabase.result.data!!["written_articles"] as MutableList<String>
            Log.w(ContentValues.TAG, "content of writtenArticles: $writtenArticles")

            // Access article text content
            if (writtenArticles.isNotEmpty()) {
                for (articleId in writtenArticles) {
                    if (articleId != "") {
                        val articleDatabase = db.collection("user_article").document(articleId).get()
                        articleDatabase.await()

                        Log.w(ContentValues.TAG, "Article ID: $articleId")
                        val currentArticleId = articleDatabase.result.data!!["article_id"] as String
                        val content = articleDatabase.result.data!!["content"] as String
                        val userId = articleDatabase.result.data!!["user_id"] as String
                        val title = articleDatabase.result.data!!["title"] as String
                        val description = articleDatabase.result.data!!["description"] as String
                        val username = articleDatabase.result.data!!["username"] as String

                        // Add the information to the data class. Null values are filled later
                        userArticles.add(UserArticle(currentArticleId, content, userId, null, null, title, description, username))
                    }
                }

                // Access the article cover image from the firebase storage
                val storage = Firebase.storage
                for (userArticle in userArticles) {
                    val currentArticle = userArticle.articleId
                    val currentUser = userArticle.userId

                    // Attempt to fetch the image, in case it exists
                    var storageRef: Task<Uri>?
                    try {
                        storageRef = storage.reference.child("ArticleImages/$currentUser/$currentArticle").downloadUrl
                        storageRef.await()
                    } catch (e: StorageException) {
                        storageRef = null
                    }
                    if (storageRef != null) {
                        userArticle.articleImage = storageRef.result
                        Log.w(ContentValues.TAG, "Image debugging: " + storageRef.result)
                    }
                }
            }
        }
        return userArticles
    }
}
