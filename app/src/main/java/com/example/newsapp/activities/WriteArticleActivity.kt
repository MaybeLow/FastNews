package com.example.newsapp.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.example.newsapp.managers.ProfileDataRequestManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Activity that allows the users to write their own articles, that are displayed in a separate section
 * from the official news articles
 */
class WriteArticleActivity: AppCompatActivity() {
    private lateinit var articleId: String
    private lateinit var userId: String
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_article)

        // Create the user article id, based on the current time
        val formatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss", Locale.GERMANY)
        articleId = LocalDateTime.now().format(formatter)
        userId = Firebase.auth.currentUser!!.uid
        username = ProfileDataRequestManager(userId).requestData().username

        addImageButton()
        GlobalScope.launch {
            addSubmitButton()
        }
    }

    /**
     * Add the button that allows the users to select a cover image for the news article
     */
    private fun addImageButton() {
        val button = findViewById<Button>(R.id.add_image_button)
        button.setOnClickListener {
            selectImage()
        }
    }

    /**
     * Send the intent for image selection
     */
    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 100)
    }

    /**
     * On result of the intent, inflate the image in the current activity
     * and send it to the Firebase storage
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && data != null && data.data != null) {
            val imageUri = data.data
            val storage = Firebase.storage

            Picasso.get().load(imageUri).into(findViewById<ImageView>(R.id.article_image))

            val storageRef = storage.reference.child("ArticleImages/$userId/$articleId")
            if (imageUri != null) {
                storageRef.putFile(imageUri)
            }
        }
    }

    /**
     * On submit, send the article variable to the database
     */
    private suspend fun addSubmitButton() {
        val db = Firebase.firestore
        val dbDocument = db.collection("user").document(userId).get()
        dbDocument.await()

        val submitButton = findViewById<Button>(R.id.article_submit_button)
        submitButton.setOnClickListener {

            val contentView = findViewById<TextView>(R.id.input_article)
            val content = contentView.text.toString()
            val titleView = findViewById<TextView>(R.id.user_article_title)
            val title = titleView.text.toString()
            val descriptionView = findViewById<TextView>(R.id.user_article_description)
            val description = descriptionView.text.toString()


            val article = hashMapOf(
                "article_id" to articleId,
                "content" to content,
                "user_id" to userId,
                "title" to title,
                "description" to description,
                "username" to username
            )

            // Update the database
            db.collection("user_article")
                .document(articleId)
                .set(article)
                .addOnSuccessListener { documentReference ->
                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: $documentReference")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }


            // Add the article id to the list of written article of the writer user
            val writtenArticles = dbDocument.result.data!!["written_articles"] as MutableList<String>
            writtenArticles.add(articleId)
            db.collection("user")
                .document(userId)
                .update("written_articles", writtenArticles)
                .addOnSuccessListener { documentReference ->
                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: $documentReference")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }
            finish()
        }
    }
}
