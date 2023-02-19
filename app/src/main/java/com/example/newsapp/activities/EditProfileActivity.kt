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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

/**
 * Activity that allows users to change or edit their profile information
 */
class EditProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_profile)

        val username = findViewById<TextView>(R.id.edit_username)
        val description = findViewById<TextView>(R.id.edit_description)

        addImageButton()

        val button = findViewById<Button>(R.id.apply_changes_button)

        button.setOnClickListener {
            val db = Firebase.firestore
            val auth = Firebase.auth

            // Assign temp parameters in case the user does not type anything
            var updatedUsername = "temp" to ""
            var updatedDescription = "temp" to ""

            if (username.text.toString() != "") {
                updatedUsername = "username" to username.text.toString()
            }
            if (description.text.toString() != "") {
                updatedDescription = "description" to description.text.toString()
            }

            // Update profile parameters in the firebase firestore if the apply button is clicked
            val update = hashMapOf(
                updatedUsername,
                updatedDescription
            )
            db.collection("user")
                .document(auth.currentUser!!.uid)
                .update(update as Map<String, String>)
                .addOnSuccessListener { documentReference ->
                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: $documentReference")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }
            finish()
        }

    }

    /**
     * Method that allows the user to select a new image for their profile
     */
    private fun addImageButton() {
        val profilePicture = findViewById<ImageView>(R.id.edit_profile_picture)
        profilePicture.setOnClickListener {
            selectImage()
        }
    }

    /**
     * Methods that selects image from the user's mobile phone storage
     */
    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 100)
    }

    /**
     * On successful result, update user's profile picture and send it to the firebase storage
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val currentUser = Firebase.auth.currentUser!!.uid

        if (requestCode == 100 && data != null && data.data != null) {
            val imageUri = data.data
            val storage = Firebase.storage

            val storageRef = storage.reference.child("ProfileImages/$currentUser")
            if (imageUri != null) {
                storageRef.putFile(imageUri)

                val imageView = findViewById<ImageView>(R.id.edit_profile_picture)
                Picasso.get().load(imageUri).into(imageView)
            }
        }
    }
}
