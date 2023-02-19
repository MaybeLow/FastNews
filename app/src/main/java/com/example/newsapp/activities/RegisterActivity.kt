package com.example.newsapp.activities

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Activity that allows the user to register a new account.
 * The information is sent to the Firebase database
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Firestore
        db = Firebase.firestore

        // When the button is pressed, the information is
        // sent to the Firebase authenticator
        val registerButton: Button = findViewById(R.id.register_button)
        registerButton.setOnClickListener {
            val username = findViewById<View>(R.id.username_input) as TextView
            val email = findViewById<View>(R.id.email_input) as TextView
            val password = findViewById<View>(R.id.password_input) as TextView

            // Make sure the fields are not empty
            if (username.text.toString() != ""
                && email.text.toString() != ""
                && password.text.toString() != "") {

                createUser(username.text.toString(), email.text.toString(), password.text.toString())
            }
        }
    }

    /**
     * Create a new user profile, taking the information provided into account
     */
    private fun createUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(baseContext, "Authentication succeeded.",
                        Toast.LENGTH_SHORT).show()

                    // Set up the database for all the app features
                    val user = hashMapOf(
                        "username" to username,
                        "user_id" to auth.currentUser!!.uid,
                        "profile_picture" to "",
                        "description" to "",
                        "friend_list" to mutableListOf<String>(),
                        "requested_friends" to mutableListOf<String>(),
                        "pending_friends" to mutableListOf<String>(),
                        "search_history" to mutableListOf<String>(),
                        "written_articles" to mutableListOf<String>(),
                        "notifications" to mutableListOf<String>(),
                        "categories" to mutableListOf<String>(),
                        "countries" to mutableListOf<String>(),
                        "languages" to mutableListOf<String>(),
                        "beNotified" to false
                    )
                    db.collection("user")
                        .document(auth.currentUser!!.uid)
                        .set(user)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot added with ID: $documentReference")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                    finish()
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                    finish()
                }
            }

    }

    /**
     * On finish, sent the resulting statement
     */
    override fun finish() {
        if (auth.currentUser != null) {
            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        super.finish()
    }
}